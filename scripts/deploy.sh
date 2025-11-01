#!/bin/bash

# Building Maintenance System Deployment Script
# This script handles deployment for different environments

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="production"
PROFILE=""
SKIP_TESTS=false
SKIP_BUILD=false
CLEAN=false

# Functions
print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -e, --environment ENV    Set environment (dev, staging, production) [default: production]"
    echo "  -p, --profile PROFILE    Set Spring profile"
    echo "  -s, --skip-tests         Skip running tests"
    echo "  -b, --skip-build         Skip building the application"
    echo "  -c, --clean              Clean before building"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -e dev                Deploy to development environment"
    echo "  $0 -e production -c      Deploy to production with clean build"
    echo "  $0 -e staging -s         Deploy to staging skipping tests"
}

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -b|--skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|production)$ ]]; then
    log_error "Invalid environment: $ENVIRONMENT. Must be dev, staging, or production"
    exit 1
fi

# Set profile if not provided
if [[ -z "$PROFILE" ]]; then
    case $ENVIRONMENT in
        dev)
            PROFILE="dev"
            ;;
        staging)
            PROFILE="staging"
            ;;
        production)
            PROFILE="production"
            ;;
    esac
fi

log_info "Starting deployment to $ENVIRONMENT environment with profile: $PROFILE"

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check if Docker Compose is installed
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check if Maven is installed (for development builds)
    if [[ "$ENVIRONMENT" == "dev" ]] && ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Build the application
build_application() {
    if [[ "$SKIP_BUILD" == true ]]; then
        log_info "Skipping build as requested"
        return
    fi
    
    log_info "Building application..."
    
    # Clean if requested
    if [[ "$CLEAN" == true ]]; then
        log_info "Cleaning previous builds..."
        mvn clean
    fi
    
    # Build command
    BUILD_CMD="mvn clean package"
    
    if [[ "$SKIP_TESTS" == true ]]; then
        BUILD_CMD="$BUILD_CMD -DskipTests"
    fi
    
    if [[ "$PROFILE" == "production" ]]; then
        BUILD_CMD="$BUILD_CMD -Pproduction"
    fi
    
    log_info "Running: $BUILD_CMD"
    eval $BUILD_CMD
    
    if [[ $? -eq 0 ]]; then
        log_success "Application built successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Deploy with Docker Compose
deploy_with_compose() {
    log_info "Deploying with Docker Compose..."
    
    # Stop existing containers
    log_info "Stopping existing containers..."
    docker-compose -f docker-compose.yml down
    
    # Pull latest images
    log_info "Pulling latest images..."
    docker-compose pull
    
    # Start services
    log_info "Starting services..."
    
    # Set environment variables
    export SPRING_PROFILES_ACTIVE=$PROFILE
    
    # Start containers
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        docker-compose -f docker-compose.dev.yml up -d
    else
        docker-compose up -d
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "Services started successfully"
    else
        log_error "Failed to start services"
        exit 1
    fi
}

# Wait for services to be ready
wait_for_services() {
    log_info "Waiting for services to be ready..."
    
    # Wait for database
    log_info "Waiting for database to be ready..."
    for i in {1..30}; do
        if docker-compose exec mysql mysqladmin ping -h localhost --silent; then
            log_success "Database is ready"
            break
        fi
        log_info "Waiting for database... ($i/30)"
        sleep 10
    done
    
    # Wait for application
    log_info "Waiting for application to be ready..."
    for i in {1..30}; do
        if curl -f http://localhost:8080/actuator/health &>/dev/null; then
            log_success "Application is ready"
            break
        fi
        log_info "Waiting for application... ($i/30)"
        sleep 10
    done
}

# Show deployment info
show_deployment_info() {
    log_info "Deployment completed successfully!"
    echo ""
    echo "========================================="
    echo "  Building Maintenance System"
    echo "  Environment: $ENVIRONMENT"
    echo "  Profile: $PROFILE"
    echo "========================================="
    echo ""
    
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        echo "Application URLs:"
        echo "  - Main Application: http://localhost:8081"
        echo "  - MailHog UI: http://localhost:8026"
        echo "  - Database: localhost:3307"
        echo "  - Debug Port: 5005"
    else
        echo "Application URLs:"
        echo "  - Main Application: http://localhost:8080"
        echo "  - MailHog UI: http://localhost:8025"
        echo "  - Database: localhost:3306"
        
        if [[ "$ENVIRONMENT" == "production" ]]; then
            echo "  - Nginx (Production): http://localhost:80"
        fi
    fi
    
    echo ""
    echo "Default credentials:"
    echo "  - Username: admin"
    echo "  - Password: admin123"
    echo ""
    echo "To stop the services, run:"
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        echo "  docker-compose -f docker-compose.dev.yml down"
    else
        echo "  docker-compose down"
    fi
}

# Main execution
main() {
    check_prerequisites
    
    if [[ "$ENVIRONMENT" != "dev" ]]; then
        build_application
    fi
    
    deploy_with_compose
    wait_for_services
    show_deployment_info
}

# Run main function
main