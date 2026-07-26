#!/usr/bin/env bash
# Reset MySQL database volume and re-run Flyway migrations
set -e

echo "Stopping services and removing database volumes..."
docker-compose -f backend/docker-compose.yml down -v

echo "Rebuilding and restarting MySQL database..."
docker-compose -f backend/docker-compose.yml up -d mysql-db

echo "Waiting for MySQL database to become healthy..."
until docker exec mysql-db mysqladmin ping -h localhost -u root -prootpassword --silent; do
    echo -n "."
    sleep 2
done

echo ""
echo "Database volume reset complete! Restarting services to re-trigger Flyway migrations..."
docker-compose -f backend/docker-compose.yml up -d --force-recreate
