import csv
import psycopg2
import logging

# Configuração de Logs
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Configurações do Banco de Dados
DB_CONFIG = {
    "dbname": "sus_connect_network",
    "user": "postgres",
    "password": "password",
    "host": "localhost",
    "port": "5432"
}

CSV_FILE = 'cnes_estabelecimentos.csv'

def import_data():
    conn = None
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        logger.info("Conectado ao banco de dados com sucesso.")

        with open(CSV_FILE, mode='r', encoding='utf-8') as f:
            # O separador parece ser ponto e vírgula baseado no 'head'
            reader = csv.DictReader(f, delimiter=';')
            
            count = 0
            for row in reader:
                try:
                    # 1. Inserir Endereço
                    # Campos mapeados: NO_LOGRADOURO -> street, NU_ENDERECO -> number, NO_BAIRRO -> neighborhood, 
                    # CO_IBGE -> (city logic?), CO_UF -> state (CO_UF é código, mas o CSV tem NO_BAIRRO etc)
                    # No head vimos que CO_UF é código, mas vamos usar o que temos.
                    
                    street = row.get('NO_LOGRADOURO', 'N/A')
                    number = row.get('NU_ENDERECO', 'S/N')
                    neighborhood = row.get('NO_BAIRRO', 'N/A')
                    city = row.get('CO_IBGE', 'N/A') # Idealmente buscaríamos o nome da cidade pelo IBGE
                    state = row.get('CO_UF', '??')
                    zip_code = row.get('CO_CEP', '00000000')
                    lat = row.get('NU_LATITUDE')
                    lon = row.get('NU_LONGITUDE')

                    cur.execute("""
                        INSERT INTO addresses (street, "number", neighborhood, city, state, zip_code, latitude, longitude, deleted, created_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, false, NOW())
                        RETURNING id
                    """, (street, number, neighborhood, city, state, zip_code, 
                          float(lat) if lat and lat != '' else None, 
                          float(lon) if lon and lon != '' else None))
                    
                    address_id = cur.fetchone()[0]

                    # 2. Inserir Unidade de Saúde
                    name = row.get('NO_FANTASIA') or row.get('NO_RAZAO_SOCIAL')
                    cnpj = row.get('NU_CNPJ')
                    
                    cur.execute("""
                        INSERT INTO health_units (name, cnpj, address_id, deleted, created_at)
                        VALUES (%s, %s, %s, false, NOW())
                        RETURNING id
                    """, (name, cnpj if cnpj and cnpj != '' else None, address_id))
                    
                    unit_id = cur.fetchone()[0]

                    # 3. Inserir Contatos (Telefone e Email)
                    raw_phone = row.get('NU_TELEFONE')
                    if raw_phone and raw_phone != '':
                        import re
                        phone = re.sub(r'\D', '', raw_phone)
                        if phone:
                            cur.execute("""
                                INSERT INTO health_unit_contacts (type, value, description, unit_id, deleted, created_at)
                                VALUES ('PHONE', %s, 'Telefone CNES', %s, false, NOW())
                            """, (phone, unit_id))

                    email = row.get('NO_EMAIL')
                    if email and email != '':
                        cur.execute("""
                            INSERT INTO health_unit_contacts (type, value, description, unit_id, deleted, created_at)
                            VALUES ('EMAIL', %s, 'Email CNES', %s, false, NOW())
                        """, (email, unit_id))

                    count += 1
                    if count % 100 == 0:
                        conn.commit()
                        logger.info(f"Importados {count} registros...")

                except Exception as e:
                    logger.error(f"Erro ao processar linha {reader.line_num}: {e}")
                    conn.rollback()

            conn.commit()
            logger.info(f"Importação concluída! Total: {count} unidades.")

    except Exception as e:
        logger.error(f"Erro fatal na importação: {e}")
    finally:
        if conn:
            cur.close()
            conn.close()

if __name__ == "__main__":
    import_data()
