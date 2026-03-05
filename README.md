## Backend del Proyecto 1 del curso de Análisis y Diseño de Sistemas 2

### Configuración del entorno de desarrollo

#### application.yaml

Propiedades necesarias para configurar la aplicación, incluyendo la conexión a la base de datos, configuración de JWT y otras propiedades personalizadas.

```yaml
spring:
  application:
    name: ayd2_p1_backend
# Database configuration
  datasource:
    url: jdbc:postgresql://host:port/database
    username: postgres user
    password: postgres password
# Logging configuration
logging:
  level:
    root: INFO
# JWT configuration
jwt:
  secret: base64-encoded-secret-key
  expiration: 86,400,000 ms # 1 hour in milliseconds
# Custom application properties
app:
  frontendHost: '*'
backend:
  host: http://localhost:8080
```