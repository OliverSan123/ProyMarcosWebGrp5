# Backend Mesa de Ayuda TI

API REST con Spring Boot, Java 21, Spring Security, JWT y JPA. No utiliza Thymeleaf.

## Ejecutar localmente

Se necesita Java 21 y Maven instalado. Desde esta carpeta:

```powershell
$env:INITIAL_ADMIN_PASSWORD = "elige-una-clave-local"
mvn spring-boot:run
```

La primera ejecución crea las prioridades y la matriz base. Si se configura `INITIAL_ADMIN_PASSWORD`, también crea el usuario inicial `admin`; cambia el nombre o correo con `INITIAL_ADMIN_USERNAME` y `INITIAL_ADMIN_EMAIL`. No se crea un administrador con una contraseña predeterminada.

Para probar la web integrada, inicia también Live Server desde el `index.html` de la raíz del repositorio. Debe usar `http://localhost:5500` o `http://127.0.0.1:5500`; el frontend ya usa `http://localhost:8080` como dirección local de la API.

Por defecto se usa una base H2 local en `./data/mesaayudati`. Para MySQL, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Antes de desplegar, configura además `JWT_SECRET` con una clave aleatoria de al menos 32 bytes.

## Autenticación

`POST /api/auth/login` recibe:

```json
{"username":"admin","password":"tu-clave"}
```

En las rutas protegidas envía el token recibido en la cabecera `Authorization: Bearer <token>`. Los roles son `ADMINISTRADOR`, `COORDINADOR`, `TECNICO` y `SOLICITANTE`.

## Rutas principales

- `GET /api/catalogos/areas`, `/categorias`, `/prioridades` y `/matriz-prioridad`: consulta de catálogos autenticada.
- `GET /api/usuarios/tecnicos`: directorio mínimo de técnicos habilitados para filtros y asignación.
- `POST`, `PUT /{id}` y `DELETE /{id}` bajo `/api/usuarios` y `/api/catalogos/...`: administración restringida al administrador.
- `POST /api/tickets`: crea un ticket como solicitante; la prioridad y fecha objetivo se calculan en el backend.
- `GET /api/tickets?tecnicoId=&areaId=&prioridadId=&categoriaId=&estado=`: filtros opcionales.
- `PUT /api/tickets/{id}/asignacion`, `/estado`, `/reapertura`, `/cierre` y `/sla`.
- `POST /api/tickets/{id}/comentarios` y `GET /api/tickets/{id}/historial`.

El dashboard carga catálogos y tickets reales, aplica los filtros en el servidor y envía altas, asignaciones, cambios de estado, comentarios, cierres y reaperturas a estas rutas. Ya no usa la lista local de tickets de demostración.

La matriz inicial conserva la regla que ya mostraba el frontend: BAJA para BAJO-BAJO, BAJO-MEDIO y MEDIO-BAJO; MEDIA para BAJO-ALTO, MEDIO-MEDIO y ALTO-BAJO; ALTA para MEDIO-ALTO y ALTO-MEDIO; CRITICA para ALTO-ALTO. El administrador puede ajustar reglas y horas SLA desde los catálogos.

## Probar el flujo completo

1. Abre Live Server en la raíz e inicia sesión como `admin` con la contraseña inicial configurada.
2. En la sección Administración, crea un área y una categoría. Prioridades y matriz ya tienen valores base editables.
3. Desde el formulario Usuarios, crea cuentas con roles `COORDINADOR`, `TECNICO` y `SOLICITANTE`; cada una debe tener área.
4. Cierra sesión, entra como `SOLICITANTE` y registra un ticket. La prioridad y el SLA se calculan con la matriz del servidor.
5. Entra como `COORDINADOR` y asigna el ticket al usuario técnico.
6. Entra como ese `TECNICO` para cambiar el estado, añadir comentarios y resolverlo. El coordinador o el técnico asignado puede cerrarlo.
7. Vuelve como el solicitante creador para consultar el historial, comentar y reabrir el ticket cerrado/resuelto con un motivo.

También puedes inspeccionar cualquier llamada con Thunder Client: inicia sesión en `POST /api/auth/login` y envía el `accessToken` en `Authorization: Bearer <token>` al llamar rutas protegidas.

GitHub Pages no ejecuta Java. Además, una página publicada por HTTPS no puede usar `http://localhost:8080` como backend de los visitantes: localhost sería la computadora de cada visitante y el navegador bloquearía la petición insegura. Para una presentación pública, despliega el backend con HTTPS y cambia `API_BASE_URL` en `Frontend/App/config.js` a esa URL pública; configura también ese origen en CORS.

GitHub Pages publica el frontend estático, no ejecuta esta API Java. Para una URL pública del backend hace falta desplegarlo en un servicio que ejecute Java y configurar su base de datos y secretos.