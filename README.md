# ReserveIt API

REST API do rezerwacji obiektów sportowych (korty, boiska).

## Stack
Java 21, Spring Boot, Spring Data JPA, PostgreSQL, Flyway, Docker

## Uruchomienie
1. `docker compose up -d`
2. Uruchom `ReserveitApiApplication` (lub `mvn spring-boot:run`)
3. API: http://localhost:8080/api/facilities

## Endpointy
- `POST /api/facilities`: dodaj obiekt
- `GET /api/facilities`: lista obiektów

## Plany
JWT i role, rezerwacje z wykrywaniem kolizji, testy (Testcontainers)