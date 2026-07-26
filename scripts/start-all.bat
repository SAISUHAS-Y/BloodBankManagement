@echo off
echo Building Maven artifacts...
mvn -f backend\pom.xml clean package -DskipTests

echo Starting full microservice stack...
docker-compose -f backend\docker-compose.yml up -d --force-recreate

echo All services started! Access Swagger UI at http://localhost:8080/swagger-ui.html
