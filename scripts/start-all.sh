#!/bin/bash
set -a
source .env
set +a

# Start backend
cd apps/backend
./mvnw spring-boot:run -Dspring-boot.run.environmentVariables="DATABASE_URL=$DATABASE_URL,POSTGRES_USER=$POSTGRES_USER,POSTGRES_PASSWORD=$POSTGRES_PASSWORD,OPENAI_API_KEY=$OPENAI_API_KEY,JWT_SECRET=$JWT_SECRET" &
BACKEND_PID=$!

# Wait for backend to be ready
echo "Waiting for backend to start..."
sleep 15
until curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; do
    sleep 5
done
echo "Backend is ready!"

# Start frontend
cd ../frontend
npm run start &
FRONTEND_PID=$!

# Wait for frontend to be ready
echo "Waiting for frontend to start..."
sleep 15
until curl -s http://localhost:4200 > /dev/null 2>&1; do
    sleep 5
done
echo "Frontend is ready!"

echo ""
echo "==================================="
echo "  Services started successfully!"
echo "==================================="
echo ""
echo "  Backend:  http://localhost:8080"
echo "  Frontend: http://localhost:4200"
echo ""
echo "  Press Ctrl+C to stop all services"
echo ""

# Wait for Ctrl+C
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT
wait
