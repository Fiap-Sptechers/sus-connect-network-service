# Sus Connect - Network Service

## 🏥 Sobre o Projeto
O **Sus Connect** é uma plataforma de orquestração logística para o Sistema Único de Saúde (SUS), focada na eficiência operacional de unidades de pronto atendimento. O sistema atua como uma camada inteligente acima das unidades de saúde, analisando a carga de trabalho, a performance histórica e a localização geográfica para direcionar o fluxo de pacientes de forma estratégica.

O **Network Service** é responsável pela governança da rede e segurança, atuando como o *Identity Provider* do ecossistema e gerenciando o cadastro base de unidades, profissionais e escalas.

## 🛠️ Tecnologias
- **Java 21**
- **Spring Boot 3.4.x**
- **PostgreSQL** (Banco de dados relacional)
- **Hibernate/JPA**
- **Flyway** (Migração de banco de dados)
- **Spring Security + JWT** (Autenticação e Autorização)
- **Lombok** (Produtividade)
- **Nominatim API** (Geocodificação OpenStreetMap via RestTemplate)
- **Observabilidade:** Micrometer, Prometheus, Grafana e Loki

## 📂 Estrutura do Projeto
O projeto segue uma estrutura organizada por módulos funcionais e responsabilidades técnicas:

- **`src/main/java/com/fiap/sus/network/core/`**: Configurações centrais do Spring, filtros de segurança e propriedades da aplicação.
- **`src/main/java/com/fiap/sus/network/shared/`**: Recursos compartilhados, como entidades base, DTOs globais, exceções customizadas e utilitários (ex: cálculos de distância).
- **`src/main/java/com/fiap/sus/network/modules/`**:
  - **`user/`**: Gestão de usuários, papéis (Roles) e autenticação.
  - **`health_unit/`**: Cadastro de Unidades de Saúde, endereços e integração com geocodificadores.
  - **`doctor/`**: Gestão de profissionais médicos.
  - **`specialty/`**: Catálogo de especialidades médicas.
  - **`shift/`**: Gestão de plantões, escalas e monitoramento de pacientes em espera.

## 🚀 Como Executar

### Pré-requisitos
- Docker e Docker Compose
- Maven 3.9+
- JDK 21

### 1. Subir Infraestrutura (Banco + Observabilidade)
O projeto utiliza Docker Compose para gerenciar as dependências de infraestrutura.
```bash
docker-compose up -d
```
Este comando iniciará:
- **PostgreSQL**: Porta `5432` (Banco: `sus_connect_network`)
- **Prometheus**: Porta `9090` (Métricas)
- **Grafana**: Porta `3000` (Painéis de monitoramento - admin/admin)
- **Loki**: Porta `3100` (Agregação de logs)

### 2. Configuração do Banco
Para conexões via clientes externos (DBeaver, pgAdmin), utilize a string:
`postgresql://postgres:password@localhost:5432/sus_connect_network`

As migrações do banco de dados são executadas automaticamente pelo **Flyway** ao iniciar a aplicação.

### 3. Executar o Backend
```bash
mvn clean spring-boot:run
```

## 📍 Busca por Raio e Geocodificação
O serviço utiliza integração com o **Nominatim** para converter endereços capturados no cadastro de unidades em coordenadas Geográficas (Latitude e Longitude).

A busca de unidades próximas (`GET /units/nearby`) é otimizada em dois níveis:
1. **Database Level:** Filtro por **Bounding Box** (caixa delimitadora) para reduzir o conjunto de dados.
2. **Application Level:** Filtro fino utilizando a **Fórmula de Haversine** para precisão milimétrica dentro do raio solicitado.

## 📊 Observabilidade
A aplicação exporta métricas nativas via Actuator e Micrometer:
- Monitoramento de conexões ao banco.
- Latência de requisições HTTP.
- Saúde do sistema (Health Checks).

Para visualizar o Dashboard pronto, acesse o Grafana e importe o arquivo `grafana_dashboard.json`.

## 📄 API e Documentação
- **Postman:** Importe o arquivo `SusConnect_Network_Service.postman_collection.json` para testar os fluxos de autenticação, cadastro e consultas.
- **Logs:** Logs estruturados com `traceId` para facilitar o rastreamento de fluxos distribuídos.

---
Desenvolvido por **Fiap-Sptechers** como parte do projeto integrador de Saúde Pública.