# Building Maintenance Ticket System

A comprehensive, modern web application for managing building maintenance tickets, built with Spring Boot, Vaadin, and MySQL, deployed using Docker.

## 🏗️ System Overview

This system provides a complete solution for managing building maintenance requests with the following key features:

- **Multi-role Access Control**: Admin, Technician, and Tenant roles with different permissions
- **Comprehensive Ticket Management**: Create, assign, track, and resolve maintenance tickets
- **Real-time Notifications**: Email notifications for status changes and updates
- **Advanced Reporting**: Dashboard analytics and detailed reports
- **File Management**: Upload and manage attachments for tickets
- **Mobile Responsive**: Full mobile and tablet support

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.x**: Modern Java application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database interaction and ORM
- **Hibernate**: JPA implementation
- **Flyway**: Database migration management
- **JWT**: Stateless authentication tokens

### Frontend
- **Vaadin 24.x**: Modern Java web framework for building UIs
- **Responsive Design**: Mobile-first approach
- **Real-time Updates**: WebSocket-based push notifications

### Database
- **MySQL 8.0**: Primary relational database
- **Connection Pooling**: HikariCP for efficient connections
- **Caching**: Second-level Hibernate cache with Ehcache

### Infrastructure
- **Docker**: Containerization for consistent deployment
- **Docker Compose**: Multi-container orchestration
- **Nginx**: Reverse proxy and load balancing (production)
- **MailHog**: Email testing and development

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.9** or higher
- **Docker** and **Docker Compose**
- **MySQL 8.0** (if running locally without Docker)
- **Node.js 18+** (for Vaadin frontend compilation)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/building-maintenance-system.git
cd building-maintenance-system
```

### 2. Setup Development Environment

```bash
# Run the setup script
./scripts/setup.sh

# Or manually install prerequisites
# Java 17, Maven 3.9+, Docker, Docker Compose
```

### 3. Configure Environment

```bash
# Copy and modify the environment file
cp .env.example .env
# Edit .env with your configuration
```

### 4. Deploy the Application

```bash
# For development environment
./scripts/deploy.sh -e dev

# For production environment
./scripts/deploy.sh -e production

# With custom options
./scripts/deploy.sh -e production -c --skip-tests
```

## 🔧 Configuration

### Environment Variables

The application uses the following environment variables:

#### Database Configuration
```bash
DB_HOST=localhost              # Database host
DB_PORT=3306                   # Database port
DB_NAME=maintenance_system     # Database name
DB_USERNAME=maint_user         # Database username
DB_PASSWORD=maint_password     # Database password
```

#### Security Configuration
```bash
JWT_SECRET=your-secret-key     # JWT signing secret
JWT_EXPIRATION=86400000        # Token expiration (24 hours)
```

#### Mail Configuration
```bash
MAIL_HOST=localhost            # SMTP host
MAIL_PORT=1025                 # SMTP port
MAIL_USERNAME=                 # SMTP username
MAIL_PASSWORD=                 # SMTP password
```

#### Application Configuration
```bash
SERVER_PORT=8080               # Application port
CONTEXT_PATH=/                 # Application context path
VAADIN_PRODUCTION_MODE=false   # Vaadin production mode
LOG_LEVEL=INFO                 # Logging level
```

### Spring Profiles

The application supports different Spring profiles:

- **dev**: Development environment with debugging enabled
- **staging**: Staging environment for testing
- **production**: Production environment with optimizations

## 📁 Project Structure

```
building-maintenance-system/
├── src/
│   ├── main/
│   │   ├── java/com/maintenance/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── enums/            # Enumeration classes
│   │   │   ├── exception/        # Exception handling
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── security/         # Security configuration
│   │   │   ├── service/          # Business logic services
│   │   │   └── ui/               # Vaadin UI components
│   │   └── resources/
│   │       ├── application.yml   # Main configuration
│   │       └── db/migration/     # Flyway migrations
│   └── test/                     # Test classes
├── docker/                       # Docker configuration
├── scripts/                      # Deployment and utility scripts
├── docker-compose.yml            # Production compose file
├── docker-compose.dev.yml        # Development compose file
├── Dockerfile                    # Application Dockerfile
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

## 🎯 User Roles and Permissions

### Admin
- Full system access
- User management
- System configuration
- All ticket operations
- Building and room management

### Technician
- View assigned tickets
- Update ticket status
- Add comments and notes
- Upload attachments
- View building information

### Tenant
- Create new tickets
- View own tickets
- Add comments to own tickets
- Upload attachments to own tickets
- View ticket status

## 📊 Dashboard Features

### Admin Dashboard
- System statistics overview
- Ticket status distribution
- Priority breakdown
- Overdue tickets monitoring
- User activity metrics
- Building performance analytics

### Technician Dashboard
- Assigned tickets overview
- Personal workload statistics
- Quick action buttons
- Recent activity feed
- Performance metrics

### Tenant Dashboard
- Personal ticket statistics
- Quick ticket creation
- Ticket history
- Status tracking

## 🔍 API Documentation

The application provides comprehensive API documentation through OpenAPI/Swagger:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Authentication

The API uses JWT Bearer token authentication:

```bash
# Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/tickets \
  -H "Authorization: Bearer <your-jwt-token>"
```

## 🐳 Docker Deployment

### Development Environment

```bash
# Start development environment
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop environment
docker-compose -f docker-compose.dev.yml down
```

### Production Environment

```bash
# Start production environment
docker-compose up -d

# Scale application instances
docker-compose up -d --scale app=3

# View logs
docker-compose logs -f app
```

## 🔧 Development

### Running Locally

```bash
# Install dependencies
mvn clean install

# Run with Spring Boot Maven plugin
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=UserServiceTest
```

### Building for Production

```bash
# Build with production profile
mvn clean package -Pproduction

# Build without tests
mvn clean package -DskipTests -Pproduction
```

## 📈 Monitoring and Logging

### Health Checks

The application provides health check endpoints:

- **Health**: http://localhost:8080/actuator/health
- **Info**: http://localhost:8080/actuator/info
- **Metrics**: http://localhost:8080/actuator/metrics

### Logging

Logs are written to:
- Console output
- File: `logs/building-maintenance-system.log`
- Structured JSON format for log aggregation

### Performance Monitoring

Optional monitoring stack with Prometheus and Grafana:

```bash
# Start with monitoring profile
docker-compose --profile monitoring up -d

# Access Grafana
curl http://localhost:3000 (admin/admin123)
```

## 🔐 Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Role-based Access Control**: Granular permissions
- **Password Security**: BCrypt hashing with salt
- **CSRF Protection**: Cross-site request forgery protection
- **CORS Configuration**: Cross-origin resource sharing
- **Input Validation**: Comprehensive validation framework
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Content security policy headers

## 📱 Mobile Support

The application is fully responsive and supports:

- **Mobile Devices**: Phones and tablets
- **Touch Gestures**: Swipe, tap, pinch-to-zoom
- **Offline Capability**: Limited offline functionality
- **Progressive Web App**: Install as mobile app

## 🔄 Continuous Integration

Example GitHub Actions workflow:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run tests
      run: mvn clean test
    - name: Build application
      run: mvn clean package -Pproduction
    - name: Build Docker image
      run: docker build -t maintenance-system .
```

## 🚀 Production Deployment

### Prerequisites
- Docker and Docker Compose
- Domain name and SSL certificate
- Production database
- Email service provider

### Deployment Steps

1. **Prepare Environment**
   ```bash
   # Copy production environment file
   cp .env.production .env
   
   # Update configuration
   # Edit .env with production values
   ```

2. **Deploy Application**
   ```bash
   # Deploy to production
   ./scripts/deploy.sh -e production -c
   
   # Or manually
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
   ```

3. **Configure SSL**
   ```bash
   # Place SSL certificates
   cp your-certificate.crt docker/nginx/ssl/
   cp your-private.key docker/nginx/ssl/
   ```

4. **Update DNS**: Point domain to server IP

## 🛠️ Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   lsof -i :8080
   
   # Kill the process or change port in .env
   ```

2. **Database Connection Failed**
   ```bash
   # Check database container
   docker-compose logs mysql
   
   # Verify credentials in .env
   ```

3. **Vaadin Compilation Errors**
   ```bash
   # Clear node_modules
   rm -rf node_modules
   
   # Reinstall dependencies
   mvn clean install
   ```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG

# Start with debug options
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section
- Review the API documentation
- Contact the development team

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) for the excellent framework
- [Vaadin](https://vaadin.com/) for the modern web framework
- [Docker](https://docker.com/) for containerization
- [MySQL](https://mysql.com/) for the reliable database
- All contributors and open-source libraries used in this project