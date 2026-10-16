# Lovely Girl — Sistema de Gestión

Sistema de gestión de ventas e inventario para Lovely Girl (tienda de productos faciales,
corporales y capilares). Arquitectura: **Angular** (frontend) + **Spring Boot hexagonal**
(backend) + **Oracle** (base de datos).

Este repositorio se está construyendo funcionalidad por funcionalidad siguiendo el SyRS del
proyecto. Ya están implementadas **F-01: Autenticación y gestión de roles** y **F-02: Gestión
del catálogo de productos**.

## Estructura

```
backend/ecommerce/   Backend Spring Boot (arquitectura hexagonal: domain / application / infrastructure)
frontend/            Frontend Angular (standalone components)
docker-compose.yml   Oracle y Mailpit (correo de pruebas) para desarrollo local
```

## Requisitos

- JDK 21 (Gradle no corre con JDK 11: si `JAVA_HOME` apunta a otra versión, cámbialo antes de `bootRun`)
- Node.js 22+ y Angular CLI (`npm install -g @angular/cli`)
- Docker Desktop

## Cómo levantar el entorno de desarrollo

1. **Base de datos y correo:**
   ```bash
   docker compose up -d
   ```
   Espera a que el contenedor `lovely-girl-oracle` esté `healthy` (la primera vez tarda 1-2
   minutos en inicializar). El backend usa el pluggable database `FREEPDB1` con el usuario de
   aplicación `lovely_girl` (ver `docker-compose.yml` / `application.properties`).

   El contenedor `lovely-girl-mailpit` recibe los correos que envía el sistema (recuperación de
   contraseña) sin enviarlos de verdad. Se leen en **http://localhost:8025**.

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

## Pruebas

```bash
cd backend/ecommerce
./gradlew test    # EcommerceApplicationTests necesita Oracle levantado

cd frontend
npx ng test --watch=false
```

## F-01: Autenticación y gestión de roles

| Requisito | Cómo se cumple |
|-----------|----------------|
| RF-01.1 Registro de clientas | `/registro` pide nombre, correo, teléfono y contraseña (8 a 20 caracteres, con letra, número y carácter especial; el ojo permite verla mientras se escribe) y la aceptación de la política de datos (Ley 1581 de 2012), que queda registrada con su fecha. El rol Cliente se asigna siempre. |
| RF-01.2 Gestión del personal | La Administradora crea, edita y desactiva cuentas en `/admin/usuarios`. Las cuentas no se eliminan. No se puede desactivar ni quitar el rol a la única Administradora activa. |
| RF-01.3 Inicio de sesión por rol | Cliente → catálogo (`/`), Administradora → dashboard (`/dashboard`), Asesora → pedidos (`/pedidos`), Encargada de Inventario → inventario (`/inventario`). Mensaje genérico "Correo o contraseña incorrectos." |
| RF-01.4 Bloqueo temporal | 5 intentos fallidos seguidos bloquean la cuenta 15 minutos; un ingreso exitoso reinicia el contador. |
| RF-01.5 Contraseñas cifradas | bcrypt con costo 12 (configurable con `BCRYPT_COSTO`, nunca menor a 10: la aplicación no arranca). Las contraseñas no se devuelven, no se envían por correo y no aparecen en los logs. |
| RF-01.6 Cierre por inactividad | La sesión se guarda en el servidor y vence tras 30 minutos sin actividad. Dos minutos antes aparece el aviso con "Seguir conectado". |
| RF-01.7 Cierre de sesión | "Cerrar sesión" anula la sesión en el servidor: el token deja de servir y el botón "atrás" no vuelve a pantallas protegidas. |
| RF-01.8 Recuperación y cambio de contraseña | "¿Olvidaste tu contraseña?" envía un enlace de un solo uso válido 30 minutos. En `/perfil` se cambia la contraseña escribiendo la actual. |
| RNF-06 Acceso por rol | Las rutas del frontend tienen guards por rol y la API exige el rol con `@PreAuthorize`. |

### Cuentas de demostración

`DemoDataLoader` crea una cuenta por rol al arrancar el backend (contraseña
`LovelyGirl2026*` para las cuatro). Se desactiva con `DEMO_HABILITADO=false`.

| Rol                     | Correo                       |
|-------------------------|------------------------------|
| Cliente                 | cliente@lovelygirl.com       |
| Asesora de Ventas       | asesora@lovelygirl.com       |
| Encargada de Inventario | inventario@lovelygirl.com    |
| Administradora          | admin@lovelygirl.com         |

### Variables de entorno del backend

| Variable | Valor por defecto | Uso |
|----------|-------------------|-----|
| `JWT_SECRET` | (valor de desarrollo) | Clave para firmar los tokens. **Cámbiala fuera de desarrollo.** |
| `BCRYPT_COSTO` | `12` | Factor de costo de bcrypt (mínimo 10). |
| `SESION_MINUTOS_INACTIVIDAD` | `30` | Minutos sin actividad tras los que vence la sesión. |
| `FRONTEND_URL` | `http://localhost:4200` | Base del enlace de recuperación de contraseña. |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | Servidor SMTP (Mailpit en desarrollo). |
| `DEMO_HABILITADO` | `true` | Crea las cuentas de demostración al arrancar. |
