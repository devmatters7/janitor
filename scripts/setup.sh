#!/bin/bash

# Building Maintenance System Setup Script
# This script sets up the development environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
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

# Check if running on supported OS
check_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    else
        log_error "Unsupported operating system: $OSTYPE"
        exit 1
    fi
    log_info "Detected OS: $OS"
}

# Check and install prerequisites
install_prerequisites() {
    log_info "Installing prerequisites..."
    
    if [[ "$OS" == "linux" ]]; then
        # Check if running on Ubuntu/Debian
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y \
                curl \
                wget \
                git \
                unzip \
                build-essential \
                openjdk-17-jdk \
                maven \
                docker.io \
                docker-compose
        
        # Check if running on CentOS/RHEL
        elif command -v yum &> /dev/null; then
            sudo yum update -y
            sudo yum install -y \
                curl \
                wget \
                git \
                unzip \
                gcc \
                java-17-openjdk-devel \
                maven \
                docker \
                docker-compose
        else
            log_error "Unsupported Linux distribution"
            exit 1
        fi
    elif [[ "$OS" == "macos" ]]; then
        # Check if Homebrew is installed
        if ! command -v brew &> /dev/null; then
            log_info "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        fi
        
        log_info "Installing packages with Homebrew..."
        brew install \
            git \
            wget \
            curl \
            maven \
            docker \
            docker-compose
        
        # Install Java
        brew install openjdk@17
        sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    fi
    
    log_success "Prerequisites installed"
}

# Setup Docker
setup_docker() {
    log_info "Setting up Docker..."
    
    if [[ "$OS" == "linux" ]]; then
        # Start Docker service
        sudo systemctl start docker
        sudo systemctl enable docker
        
        # Add current user to docker group
        sudo usermod -aG docker $USER
        
        log_warning "Please log out and log back in for Docker group changes to take effect"
    fi
    
    # Test Docker
    if docker --version &> /dev/null; then
        log_success "Docker is ready"
    else
        log_error "Docker setup failed"
        exit 1
    fi
}

# Create necessary directories
create_directories() {
    log_info "Creating necessary directories..."
    
    mkdir -p uploads
    mkdir -p logs
    mkdir -p docker/mysql/init
    mkdir -p docker/mysql/conf
    mkdir -p docker/nginx/conf.d
    mkdir -p docker/nginx/ssl
    
    log_success "Directories created"
}

# Create sample configuration files
create_config_files() {
    log_info "Creating sample configuration files..."
    
    # MySQL configuration
    cat > docker/mysql/conf/my.cnf << EOF
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_connections=200
innodb_buffer_pool_size=1G
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=1
innodb_flush_method=O_DIRECT
query_cache_size=0
query_cache_type=0
tmp_table_size=64M
max_heap_table_size=64M
thread_cache_size=100
table_open_cache=2000
EOF

    # Nginx configuration
    cat > docker/nginx/conf.d/default.conf << EOF
upstream app {
    server app:8080;
}

server {
    listen 80;
    server_name localhost;
    
    location / {
        proxy_pass http://app;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
    
    location /actuator/health {
        access_log off;
        proxy_pass http://app/actuator/health;
    }
}
EOF

    log_success "Configuration files created"
}

# Setup Git hooks (optional)
setup_git_hooks() {
    log_info "Setting up Git hooks..."
    
    if [[ -d .git ]]; then
        # Pre-commit hook
        cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Pre-commit hook for Building Maintenance System

# Run tests
if ! mvn test; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code style (if checkstyle is configured)
if ! mvn checkstyle:check; then
    echo "Code style check failed. Commit aborted."
    exit 1
fi
EOF
        chmod +x .git/hooks/pre-commit
        
        log_success "Git hooks configured"
    else
        log_warning "Not a Git repository, skipping Git hooks setup"
    fi
}

# Create environment file
create_env_file() {
    log_info "Creating environment file..."
    
    cat > .env << EOF
# Building Maintenance System Environment Configuration
# Copy this file to .env and modify values as needed

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=maintenance_system
DB_USERNAME=maint_user
DB_PASSWORD=maint_password

# Security Configuration
JWT_SECRET=your-secret-key-here-change-this-in-production
JWT_EXPIRATION=86400000

# Mail Configuration
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=

# Application Configuration
SERVER_PORT=8080
CONTEXT_PATH=/
VAADIN_PRODUCTION_MODE=false
LOG_LEVEL=INFO

# File Storage
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760
ALLOWED_EXTENSIONS=jpg,jpeg,png,gif,pdf,txt,doc,docx

# System Configuration
AUTO_CLOSE_DAYS=7
EMAIL_ENABLED=true
EMAIL_FROM=noreply@maintenance.system
DEFAULT_PAGE_SIZE=20
MAX_PAGE_SIZE=100
EOF

    log_success "Environment file created"
}

# Main setup function
main() {
    echo "==================================="
    echo "  Building Maintenance System"
    echo "  Development Environment Setup"
    echo "==================================="
    echo ""
    
    check_os
    install_prerequisites
    setup_docker
    create_directories
    create_config_files
    create_env_file
    setup_git_hooks
    
    echo ""
    echo "==================================="
    echo "  Setup Complete!"
    echo "==================================="
    echo ""
    echo "Next steps:"
    echo "1. Review and modify .env file with your configuration"
    echo "2. For development: ./scripts/deploy.sh -e dev"
    echo "3. For production: ./scripts/deploy.sh -e production"
    echo ""
    echo "Default credentials after first run:"
    echo "  Username: admin"
    echo "  Password: admin123"
    echo ""
    echo "Application URLs:"
    echo "  Development: http://localhost:8081"
    echo "  Production: http://localhost:8080"
    echo "  MailHog UI: http://localhost:8025"
}

# Run main function
main