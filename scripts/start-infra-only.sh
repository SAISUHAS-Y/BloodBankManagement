#!/usr/bin/env bash
# Start Infrastructure and Platform Services only (for native service development)
set -e

echo "Starting Infrastructure (MySQL, Redis, RabbitMQ, Zipkin) and Platform (Config, Eureka, Gateway)..."
docker-compose -f backend/docker-compose.yml up -d mysql-db redis-cache rabbitmq-broker zipkin-tracing config-server discovery-server api-gateway

echo "Infrastructure services are up. You can now run individual microservices natively in your IDE!"
