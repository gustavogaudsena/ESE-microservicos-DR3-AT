# DR3 AT - Microsserviços e DevOps com Spring Boot e Spring Cloud

Projeto Acadêmico - Instituto Infnet
Disciplina: Microsserviços e DevOps com Spring Boot e Spring Cloud [25E4_3]


## Visão Geral

Este projeto implementa uma arquitetura de microsserviços utilizando Spring Boot, Spring WebFlux (programação reativa), PostgreSQL e Kubernetes para orquestração.

### Arquitetura

```
┌─────────────────┐
│   library       │  (Port 8080)
│   Service       │  - Gerencia empréstimos de livros
│                 │  - Consome créditos do usuário
└────────┬────────┘
         │
         │ HTTP REST
         │
         ▼
┌─────────────────┐
│  userCredits    │  (Port 8082)
│   Service       │  - Gerencia créditos de usuários
│                 │  - Debita créditos
└────────┬────────┘
         │
         │
         ▼
┌─────────────────┐
│   PostgreSQL    │  (Port 5432)
│   Database      │  - Banco compartilhado
│                 │  - Schemas: library, credits
└─────────────────┘
```

## Estrutura do Repositório

```
AT/
├── build-images.sh              # Script para build das imagens Docker
├── deploy.sh                    # Script para deploy no K8s
├── delete.sh                    # Script para deletar recursos do K8s
├── userCredits/                 # Microsserviço de créditos
│   ├── src/
│   ├── docker/
│   │   └── Dockerfile
│   └── pom.xml
├── library/                     # Microsserviço de biblioteca
│   ├── src/
│   ├── docker/
│   │   └── Dockerfile
│   └── pom.xml
└── k8s/                         # Configurações Kubernetes
    ├── namespace.yaml
    ├── postgres/
    ├── usercredits/
    ├── library/
    └── README.md
```

## Tecnologias Utilizadas

### Backend
- **Java 21**
- **Spring Boot 3.5.8**
- **Spring WebFlux** (Programação Reativa)
- **Spring Data R2DBC** (Acesso reativo ao banco)
- **PostgreSQL** (Banco de dados)
- **Flyway** (Migrations)
- **Lombok** (Redução de boilerplate)

### Testes
- **JUnit 5**
- **Reactor Test** (Testes reativos)
- **Testcontainers** (Testes de integração)
- **JaCoCo** (Cobertura de código)

### DevOps
- **Docker** (Containerização)
- **Kubernetes** (Orquestração)
- **Maven** (Build)

## Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker
- Kubernetes (minikube, Docker Desktop, ou kind)
- kubectl

## Como Executar

### 1. Executar Localmente (Desenvolvimento)

Cada serviço usa H2 em memória por padrão para desenvolvimento local.

#### userCredits
```bash
cd userCredits
./mvnw spring-boot:run
# Acesse: http://localhost:8082
```

#### library
```bash
cd library
./mvnw spring-boot:run
# Acesse: http://localhost:8080
```

### 2. Executar no Kubernetes

```bash
# 0. Configurar docker (se aplicável)
eval $(minikube docker-env)

# 1. Permissões

chmod +x build-images.sh
chmod +x deploy.sh
chmod +x delete.sh

# 2. Build das imagens
./build-images.sh

# 3. Deploy no Kubernetes
./deploy.sh

# 4. Verificar status
kubectl get pods -n dr3at
kubectl get services -n dr3at

# 5 Acessar o serviço 
# 5.1 Utilizando minikube
minikube tunnel

# 5.2 Utilizando port-forward
kubectl port-forward -n dr3at service/library-service 8080:8080
kubectl port-forward -n dr3at service/usercredits-service 8082:8082

# 6. Deletar recursos (quando necessário)
./delete.sh

# 7. Resetar configuração do docker (se aplicável)
eval $(minikube docker-env -u)
```

## Executar Testes

### Testes Unitários
```bash
# userCredits
cd userCredits
./mvnw test

# library
cd library
./mvnw test
```

### Testes com Cobertura (JaCoCo)
```bash
# userCredits
cd userCredits
./mvnw clean test
# Relatório em: target/site/jacoco/index.html

# library
cd library
./mvnw clean test
# Relatório em: target/site/jacoco/index.html
```

## Endpoints Principais

### library Service (8080)

```bash
# Listar todos os empréstimos do usuário
GET http://localhost:8080/loans/{userId}

# Criar novo empréstimo
POST http://localhost:8080/loans
Content-Type: application/json
{
	"userId": "cf30179e-8a13-42de-b855-d345b8e29e55",
	"bookId": "0a425106-86b3-4f55-a655-84a4b1c1c069"
}
```

### userCredits Service (8082)

```bash
# Buscar créditos do usuário
GET http://localhost:8082/credits/{userId}

# Adicionar créditos para usuário
POST http://localhost:8082/credits
Content-Type: application/json
{
  "userId": "cf30179e-8a13-42de-b855-d345b8e29e55",
  "amount": 10
}

# Consome um crédito do usuário
PUT http://localhost:8082/credits/{userId}
Content-Type: application/json
```

## Configuração de Ambiente

### Variáveis de Ambiente (Kubernetes)

As aplicações são configuradas via variáveis de ambiente no Kubernetes:

**PostgreSQL:**
- `POSTGRES_DB`: appdb
- `POSTGRES_USER`: appuser
- `POSTGRES_PASSWORD`: postgres123

**Spring Boot:**
- `SPRING_R2DBC_URL`: r2dbc:postgresql://postgres-service:5432/appdb
- `SPRING_R2DBC_USERNAME`: appuser
- `SPRING_R2DBC_PASSWORD`: postgres123
- `SPRING_FLYWAY_ENABLED`: true
- `SERVER_PORT`: 8080 ou 8082

**Comunicação entre serviços:**
- `CREDIT_SERVICE_URL`: http://usercredits-service:8082

## Autor

Gustavo Gaudereto Sena