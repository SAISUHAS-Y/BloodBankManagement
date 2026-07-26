#!/usr/bin/env bash
# Stop all containers
set -e

echo "Stopping all BloodBank microservice containers..."
docker-compose -f backend/docker-compose.yml down

echo "All containers stopped."
