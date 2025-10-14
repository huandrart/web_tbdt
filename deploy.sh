#!/bin/bash

# Electronic Store Deployment Script
# This script helps deploy the application to production

set -e

echo "🚀 Starting Electronic Store Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_warning "Maven is not installed. Using Maven wrapper instead."
    MVN_CMD="./mvnw"
else
    MVN_CMD="mvn"
fi

print_status "Building application..."

# Build the application
$MVN_CMD clean package -DskipTests

if [ $? -eq 0 ]; then
    print_status "Application built successfully!"
else
    print_error "Build failed. Please check the errors above."
    exit 1
fi

print_status "Starting services with Docker Compose..."

# Start services
docker-compose up -d

if [ $? -eq 0 ]; then
    print_status "Services started successfully!"
    print_status "Application is available at: http://localhost:8080"
    print_status "Admin panel: http://localhost:8080/admin"
    print_status "Database: localhost:3306"
    
    echo ""
    print_status "Default admin credentials:"
    echo "  Email: admin@electronicstore.com"
    echo "  Password: admin123"
    
    echo ""
    print_status "To view logs:"
    echo "  docker-compose logs -f app"
    
    echo ""
    print_status "To stop services:"
    echo "  docker-compose down"
    
else
    print_error "Failed to start services. Please check the logs."
    exit 1
fi

print_status "Deployment completed successfully! 🎉"
