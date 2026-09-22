# Lovely Girl — Sistema de Gestión

Sistema de gestión de ventas e inventario para Lovely Girl (tienda de productos faciales,
corporales y capilares). Arquitectura: **Angular** (frontend) + **Spring Boot hexagonal**
(backend) + **Oracle** (base de datos).

Este repositorio se está construyendo funcionalidad por funcionalidad siguiendo el SyRS del
proyecto. La primera en implementarse es **F-01: Autenticación y gestión de roles**.

## Estructura

```
backend/ecommerce/   Backend Spring Boot (arquitectura hexagonal: domain / application / infrastructure)
frontend/            Frontend Angular (standalone components)
docker-compose.yml   Contenedor de Oracle para desarrollo local
```

## Requisitos

- JDK 21
- Node.js 22+ y Angular CLI (`npm install -g @angular/cli`)
- Docker Desktop

## Cómo levantar el entorno de desarrollo

1. **Base de datos (Oracle):**
   ```bash
   docker compose up -d
   ```
   Espera a que el contenedor `lovely-girl-oracle` esté `healthy` (la primera vez tarda 1-2
   minutos en inicializar). El backend usa el pluggable database `FREEPDB1` con el usuario de
   aplicación `lovely_girl` (ver `docker-compose.yml` / `application.properties`).

2. **Backend:**
   ```bash
   cd backend/ecommerce
   ./gradlew bootRun
   ```
   Flyway crea el esquema automáticamente al arrancar (`src/main/resources/db/migration`).
   La API queda disponible en `http://localhost:8080/api`.

3. **Frontend:**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   La app queda disponible en `http://localhost:4200`.

## Cuentas de demostración (F-01)

`DemoDataLoader` crea un usuario por cada rol al arrancar el backend (contraseña
`LovelyGirl2026*` para las cuatro):

| Rol                    | Correo                      |
|-------------------------|------------------------------|
| Cliente                 | cliente@lovelygirl.com       |
| Asesora de Ventas        | asesora@lovelygirl.com       |
| Encargada de Inventario | inventario@lovelygirl.com    |
| Administradora          | admin@lovelygirl.com         |

Solo la cuenta con rol **Administradora** puede crear nuevas cuentas de personal interno
(Asesora de Ventas, Encargada de Inventario, otra Administradora) desde el panel
`/admin/usuarios`. El registro público (`/registro`) siempre crea un Cliente.
