# Backend Mesa de Ayuda TI

API REST con Spring Boot, Java 21, Spring Security, JWT y JPA. No utiliza Thymeleaf.

## Ejecutar localmente

Se necesita Java 21 y Maven instalado. Desde esta carpeta:

```powershell
$env:INITIAL_ADMIN_PASSWORD = "elige-una-clave-local"
mvn spring-boot:run
```

La primera ejecución crea las prioridades y la matriz base. Si se configura `INITIAL_ADMIN_PASSWORD`, también crea el usuario inicial `admin`; cambia el nombre o correo con `INITIAL_ADMIN_USERNAME` y `INITIAL_ADMIN_EMAIL`. No se crea un administrador con una contraseña predeterminada.

Por defecto se usa una base H2 local en `./data/mesaayudati`. Para MySQL, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Antes de desplegar, configura además `JWT_SECRET` con una clave aleatoria de al menos 32 bytes.

## Autenticación

`POST /api/auth/login` recibe:

```json
{"username":"admin","password":"tu-clave"}
```

En las rutas protegidas envía el token recibido en la cabecera `Authorization: Bearer <token>`. Los roles son `ADMINISTRADOR`, `COORDINADOR`, `TECNICO` y `SOLICITANTE`.

## Rutas principales

- `GET /api/catalogos/areas`, `/categorias`, `/prioridades` y `/matriz-prioridad`: consulta de catálogos autenticada.
- `POST`, `PUT /{id}` y `DELETE /{id}` bajo `/api/usuarios` y `/api/catalogos/...`: administración restringida al administrador.
- `POST /api/tickets`: crea un ticket como solicitante; la prioridad y fecha objetivo se calculan en el backend.
- `GET /api/tickets?tecnicoId=&areaId=&prioridadId=&categoriaId=&estado=`: filtros opcionales.
- `PUT /api/tickets/{id}/asignacion`, `/estado`, `/reapertura`, `/cierre` y `/sla`.
- `POST /api/tickets/{id}/comentarios` y `GET /api/tickets/{id}/historial`.

La matriz inicial asigna BAJA a impacto/urgencia BAJO-BAJO, MEDIA a BAJO-MEDIO, BAJO-ALTO y MEDIO-BAJO, ALTA a MEDIO-MEDIO, MEDIO-ALTO y ALTO-BAJO, y CRITICA a ALTO-MEDIO y ALTO-ALTO. El administrador puede ajustar reglas y horas SLA desde los catálogos.

GitHub Pages publica el frontend estático, no ejecuta esta API Java. Para una URL pública del backend hace falta desplegarlo en un servicio que ejecute Java y configurar su base de datos y secretos.