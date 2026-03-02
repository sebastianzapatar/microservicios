#!/bin/bash

echo "Starting Config Server (serversc)..."
osascript -e 'tell application "Terminal" to do script "cd /Users/sebastianzapata/Documents/implementacion/serversc && ./mvnw spring-boot:run"'

echo "Waiting for Config Server to start (15 seconds)..."
sleep 15

echo "Starting Eureka Server..."
osascript -e 'tell application "Terminal" to do script "cd /Users/sebastianzapata/Documents/implementacion/eureka && ./mvnw spring-boot:run"'

echo "Waiting for Eureka Server to start (15 seconds)..."
sleep 15

echo "Starting Product Service..."
osascript -e 'tell application "Terminal" to do script "cd /Users/sebastianzapata/Documents/implementacion/product-service && ./mvnw spring-boot:run"'

echo "Starting Order Service..."
osascript -e 'tell application "Terminal" to do script "cd /Users/sebastianzapata/Documents/implementacion/order-service && ./mvnw spring-boot:run"'

echo "Waiting for microservices to start (15 seconds)..."
sleep 15

echo "Starting API Gateway..."
osascript -e 'tell application "Terminal" to do script "cd /Users/sebastianzapata/Documents/implementacion/apigetaway && ./mvnw spring-boot:run"'

echo "All apps have been launched in separate Terminal windows!"
