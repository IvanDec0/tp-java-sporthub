# Sportshub

Aplicación Spring Boot con MySQL.

## Requisitos Previos

- Java 17 o superior
- Maven
- Docker y Docker Compose

## Configuración del Entorno

1. Clonar el repositorio:

```bash
git clone https://github.com/IvanDec0/tp-java-sporthub
cd tp-java-sporthub
```

## Construcción y Ejecución

### Opción 1: Usando Docker (Recomendado)

1. Construir la aplicación:

```bash
./mvnw clean package
```

2. Iniciar los servicios con Docker Compose:

```bash
docker-compose up --build
```

La aplicación estará disponible en:

- API: http://localhost:8080
- Base de datos MySQL: localhost:3306

### Opción 2: Ejecución Local

1. Asegúrate de tener MySQL ejecutándose localmente con las siguientes credenciales:

   - Host: localhost
   - Puerto: 3306
   - Base de datos: sportshub_db
   - Usuario: root
   - Contraseña: root

2. Construir y ejecutar la aplicación:

```bash
./mvnw spring-boot:run
```

## Detener la Aplicación

Para detener los servicios de Docker:

```bash
docker-compose down
```

Para eliminar los volúmenes de Docker (incluyendo los datos de la base de datos):

```bash
docker-compose down -v
```

## Estructura del Proyecto

```
tp-java-sporthub/
├── src/
│   ├── main/
│   │   ├── java/     # Código fuente Java
│   │   └── resources/ # Archivos de configuración
│   └── test/         # Pruebas
├── Dockerfile        # Configuración de Docker para la aplicación
└── docker-compose.yml # Configuración de los servicios Docker
```

## Variables de Entorno

La aplicación utiliza las siguientes variables de entorno:

- `SPRING_DATASOURCE_URL`: URL de conexión a la base de datos
- `SPRING_DATASOURCE_USERNAME`: Usuario de la base de datos
- `SPRING_DATASOURCE_PASSWORD`: Contraseña de la base de datos

Estas variables están configuradas en el archivo `docker-compose.yml` y pueden ser modificadas según sea necesario.
