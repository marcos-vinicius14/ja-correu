#!/bin/bash
set -a
source .env
set +a
cd apps/backend
./mvnw spring-boot:run -Dspring-boot.run.environmentVariables="DATABASE_URL=$DATABASE_URL,POSTGRES_USER=$POSTGRES_USER,POSTGRES_PASSWORD=$POSTGRES_PASSWORD,OPENAI_API_KEY=$OPENAI_API_KEY,JWT_SECRET=$JWT_SECRET"
