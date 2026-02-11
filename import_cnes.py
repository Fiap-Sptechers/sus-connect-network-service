import csv
import logging
import re
import io
from google.cloud.sql.connector import Connector
import sqlalchemy
from sqlalchemy import text

import os
from dotenv import load_dotenv

# Carrega varíaveis de ambiente
load_dotenv()

# Configuração de Logs
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Configurações do Banco de Dados Cloud SQL
INSTANCE_CONNECTION_NAME = os.getenv("INSTANCE_CONNECTION_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASS = os.getenv("DB_PASS")
DB_NAME = os.getenv("DB_NAME")

CSV_FILE = 'cnes_estabelecimentos.csv'
BATCH_SIZE = 500 

def getconn():
    connector = Connector()
    conn = connector.connect(
        INSTANCE_CONNECTION_NAME,
        "pg8000",
        user=DB_USER,
        password=DB_PASS,
        db=DB_NAME
    )
    return conn

pool = sqlalchemy.create_engine(
    "postgresql+pg8000://",
    creator=getconn,
    pool_size=10,
    max_overflow=5,
)

def clean_val(val):
    if val is None: return None
    return val.strip().replace('"', '')

def is_valid_lat_long(val):
    try:
        f = float(val)
        return -180 <= f <= 180 and f != 0.0
    except:
        return False

def setup_db(db_conn):
    logger.info("Verificando e ajustando integridade do banco...")
    try:
        # 1. Adicionar external_id para controle de sincronização
        db_conn.execute(text("ALTER TABLE health_units ADD COLUMN IF NOT EXISTS external_id VARCHAR(50)"))
        db_conn.execute(text("CREATE UNIQUE INDEX IF NOT EXISTS idx_health_units_external_id ON health_units(external_id)"))
        
        # 2. Remover UNIQUE do CNPJ (O CNES tem CNPJs repetidos para unidades/filiais diferentes)
        # O Nome da constraint varia, então tentamos as mais comuns
        db_conn.execute(text("ALTER TABLE health_units DROP CONSTRAINT IF EXISTS health_units_cnpj_key"))
        db_conn.execute(text("ALTER TABLE health_units DROP CONSTRAINT IF EXISTS uk_health_units_cnpj"))
        
        db_conn.commit()
    except Exception as e:
        logger.warning(f"Aviso estrutural: {e}")
        db_conn.rollback()

def import_data():
    try:
        with pool.connect() as db_conn:
            setup_db(db_conn)
            
            logger.info(f"Processando arquivo: {CSV_FILE}")
            with open(CSV_FILE, mode='r', encoding='utf-8') as f:
                header_line = f.readline()
                header = [h.replace('"', '') for h in header_line.strip().split(';')]
                reader = csv.DictReader(f, fieldnames=header, delimiter=';')
                
                batch_data = []
                count = 0
                total_new = 0
                
                for row in reader:
                    ext_id = clean_val(row.get('CO_UNIDADE'))
                    if not ext_id: continue
                    
                    name = clean_val(row.get('NO_FANTASIA')) or clean_val(row.get('NO_RAZAO_SOCIAL'))
                    if not name: continue

                    batch_data.append({
                        "ext_id": ext_id,
                        "name": name[:255],
                        "street": (clean_val(row.get('NO_LOGRADOURO')) or 'N/A')[:255],
                        "num": (clean_val(row.get('NU_ENDERECO')) or 'S/N')[:50],
                        "neigh": (clean_val(row.get('NO_BAIRRO')) or 'N/A')[:100],
                        "city": (clean_val(row.get('CO_IBGE')) or 'N/A')[:100],
                        "state": (clean_val(row.get('CO_UF')) or '??')[:2],
                        "zip": re.sub(r'\D', '', clean_val(row.get('CO_CEP')) or '00000000')[:20],
                        "lat": float(clean_val(row.get('NU_LATITUDE'))) if is_valid_lat_long(row.get('NU_LATITUDE')) else None,
                        "lon": float(clean_val(row.get('NU_LONGITUDE'))) if is_valid_lat_long(row.get('NU_LONGITUDE')) else None,
                        "cnpj": re.sub(r'\D', '', clean_val(row.get('NU_CNPJ')) or '') if len(re.sub(r'\D', '', clean_val(row.get('NU_CNPJ')) or '')) == 14 else None
                    })
                    
                    if len(batch_data) >= BATCH_SIZE:
                        total_new += process_batch(db_conn, batch_data)
                        count += len(batch_data)
                        batch_data = []
                        logger.info(f"Progresso: {count} processados | {total_new} novos inseridos")

                if batch_data:
                    total_new += process_batch(db_conn, batch_data)
                
                logger.info(f"CONCLUÍDO! Total de novas unidades: {total_new}")

    except Exception as e:
        logger.error(f"Erro fatal: {e}")

def process_batch(db_conn, data):
    new_in_batch = 0
    try:
        # Corrigindo a query de verificação para ser compatível com pg8000
        ids = [d['ext_id'] for d in data]
        
        # Filtra os que NÃO existem usando a técnica do external_id
        # PostgreSQL syntax para ANY com lista de strings
        query_check = text("SELECT external_id FROM health_units WHERE external_id = ANY(:ids)")
        existing = {r[0] for r in db_conn.execute(query_check, {"ids": ids})}
        
        to_insert = [d for d in data if d['ext_id'] not in existing]
        
        if not to_insert:
            return 0

        # Para cada novo, insere endereço e unidade (dentro de uma transação por lote)
        for item in to_insert:
            try:
                # 1. Endereço
                res_addr = db_conn.execute(text("""
                    INSERT INTO addresses (street, "number", neighborhood, city, state, zip_code, latitude, longitude, deleted, created_at)
                    VALUES (:street, :num, :neigh, :city, :state, :zip, :lat, :lon, false, NOW())
                    RETURNING id
                """), item)
                item['addr_id'] = res_addr.fetchone()[0]
                
                # 2. Unidade
                db_conn.execute(text("""
                    INSERT INTO health_units (name, cnpj, address_id, external_id, deleted, created_at)
                    VALUES (:name, :cnpj, :addr_id, :ext_id, false, NOW())
                """), item)
                
                new_in_batch += 1
            except Exception as row_error:
                logger.debug(f"Pulando linha por erro: {row_error}")
                continue

        db_conn.commit()
    except Exception as batch_error:
        db_conn.rollback()
        logger.error(f"Erro ao processar lote: {batch_error}")
        
    return new_in_batch

if __name__ == "__main__":
    import_data()
