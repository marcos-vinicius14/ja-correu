# Ja Correu - Monorepo

## Estrutura do Projeto

```
ja-correu/
├── apps/
│   ├── backend/          # Spring Boot API (Java 21)
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── compose.yaml
│   └── frontend/         # Angular 20 (TypeScript)
│       ├── src/
│       ├── angular.json
│       └── package.json
├── scripts/              # Scripts de inicialização
│   ├── backend-run.sh    # Script para rodar backend
│   └── start-all.sh      # Script para rodar tudo
├── package.json          # Root config
├── pnpm-workspace.yaml   # pnpm workspaces config
└── README.md
```

## Pré-requisitos

- Node.js >= 22
- pnpm >= 9
- Java 21
- Docker (para PostgreSQL)

## Quick Start

```bash
# 1. Iniciar o banco de dados
docker compose -f apps/backend/compose.yaml up -d db

# 2. Iniciar frontend e backend
pnpm run start
```

## Comandos Disponíveis

### Development

```bash
# Iniciar frontend e backend simultaneamente (recomendado)
pnpm run start

# Iniciar apenas backend
pnpm run backend:run

# Iniciar apenas frontend
pnpm run frontend:dev
```

### Build

```bash
# Build do frontend
pnpm run frontend:build

# Build do backend
pnpm run backend:build
```

### Testes

```bash
# Testes do frontend
pnpm run frontend:test
```

## URLs de Acesso

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080
- **Actuator Health:** http://localhost:8080/actuator/health

## Configuração do Proxy

O frontend está configurado para redirecionar chamadas `/api` e `/actuator` para o backend em `localhost:8080`.

## Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=jacorreu_db
DATABASE_URL=jdbc:postgresql://localhost:5432/jacorreu_db
JWT_SECRET=your_jwt_secret
OPENAI_API_KEY=your_openai_key
```

## Troubleshooting

### Backend não inicia
- Verifique se o PostgreSQL está rodando: `docker compose -f apps/backend/compose.yaml ps`
- Verifique as variáveis de ambiente no `.env`

### Frontend não inicia
- Verifique se o Node.js está instalado: `node --version`
- Instale as dependências: `cd apps/frontend && npm install`
