# Mesa de Ayuda TI

API REST en Spring Boot 4 y Java 21 para autenticar usuarios y gestionar catálogos y tickets. La persistencia de la aplicación usa MySQL mediante Spring Data JPA e Hibernate. H2 se utiliza únicamente en las pruebas automatizadas.

## Requisitos

- JDK 21 o compatible
- MySQL Server activo
- La base `mesaayudati`, creada con `../../database/crear-base-mysql.sql`
- PowerShell y Live Server para abrir el frontend

## Iniciar el sistema

Desde la raíz del repositorio, ejecuta:

```powershell
cd .\Backend\mesaayudati
$env:DB_URL = "jdbc:mysql://localhost:3306/mesaayudati"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CLAVE_MYSQL"
$env:INITIAL_TEST_USERS_PASSWORD = "UnaClaveLocalSegura"
.\mvnw.cmd spring-boot:run
```

Spring crea o actualiza las tablas en MySQL al iniciar. La API queda en `http://localhost:8080`. Configura esas variables con los datos de tu instalación; no guardes contraseñas reales en el repositorio.

Abre `index.html` desde Live Server, normalmente en `http://localhost:5500`. El backend y Live Server deben permanecer activos mientras utilizas la aplicación.

Las cuentas de demostración (`admin.prueba`, `coordinador.prueba`, `tecnico.prueba` y `solicitante.prueba`) se crean o actualizan cuando se define `INITIAL_TEST_USERS_PASSWORD`. Esa variable solo se necesita para inicializarlas o sincronizar su contraseña.

## Flujo principal

1. Inicia sesión como administrador y crea un área, una categoría y los usuarios necesarios.
2. Inicia sesión como solicitante y registra un ticket.
3. El coordinador asigna un técnico.
4. El técnico actualiza el estado y registra comentarios.
5. El solicitante consulta el historial y puede reabrir un ticket resuelto o cerrado con un motivo.

El servidor valida los DTO con Bean Validation (`@Valid`), comprueba las relaciones antes de guardar y devuelve errores JSON controlados. Hibernate persiste las entidades y sus claves foráneas en MySQL.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria aislada; la aplicación ejecutada normalmente solo usa MySQL. La configuración detallada está en `../../docs/GUIA_EJECUCION.md`.

GitHub Pages solo publica el frontend. Para desplegar el sistema completo se requiere un backend Java, una base MySQL, variables de entorno seguras y actualizar `API_BASE_URL` en `Frontend/App/config.js`.
