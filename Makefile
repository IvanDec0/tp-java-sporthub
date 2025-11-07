.PHONY: db-start db-stop db-restart db-logs db-shell run-api

# Comandos para gestionar la base de datos MySQL
db-start:
	docker-compose -f ./docker-compose.yml up -d mysql

db-stop:
	docker-compose -f ./docker-compose.yml stop mysql

db-restart:
	docker-compose -f ./docker-compose.yml restart mysql

db-logs:
	docker-compose -f ./docker-compose.yml logs -f mysql

db-shell:
	docker-compose -f ./docker-compose.yml exec mysql mysql -u root -proot sportshub_db

# Comandos para ejecutar la API de SportsHub
run-api:
	./mvnw clean spring-boot:run

# Inicia todos los servicios necesarios para el entorno de desarrollo
services-start:
	docker-compose -f ./docker-compose.yml up -d

services-stop:
	docker-compose -f ./docker-compose.yml stop

services-restart:
	docker-compose -f ./docker-compose.yml restart

