# Ejecución y presentación

## Requisitos

- JDK 21 o compatible y PowerShell.
- MySQL Server activo y la base `mesaayudati` creada con `database/crear-base-mysql.sql`.
- Visual Studio Code con Live Server para el frontend.
- Internet en la primera compilación de Maven y para Bootstrap desde CDN.

## Iniciar con MySQL

Desde MySQL Workbench o un cliente MySQL, ejecuta una vez `database/crear-base-mysql.sql`. Luego, desde la raíz del repositorio:

```powershell
cd .\Backend\mesaayudati
$env:DB_URL = "jdbc:mysql://localhost:3306/mesaayudati"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CLAVE_MYSQL"
$env:INITIAL_TEST_USERS_PASSWORD = "NuevaClave123"
.\mvnw.cmd spring-boot:run
```

La URL, el usuario y la contraseña deben corresponder a la instalación local. `DB_PASSWORD` no tiene una clave por defecto: configura la tuya en la terminal y no la guardes en Git. Hibernate usa `ddl-auto=update` para crear o actualizar las tablas al iniciar. Los datos de H2 anteriores no se migran a MySQL.

La API escucha en `http://localhost:8080`. Abre `index.html` en la raíz con Live Server, normalmente en `http://localhost:5500` o `http://127.0.0.1:5500`. Mantén ambos procesos abiertos durante la demostración.

Las cuentas de demostración son `admin.prueba`, `coordinador.prueba`, `tecnico.prueba` y `solicitante.prueba`. Se crean o sincronizan cuando defines `INITIAL_TEST_USERS_PASSWORD`; el valor elegido será la clave de esas cuentas. Para los siguientes inicios, puedes quitarla si ya existen:

```powershell
Remove-Item Env:INITIAL_TEST_USERS_PASSWORD -ErrorAction SilentlyContinue
.\mvnw.cmd spring-boot:run
```

## Ejecutar pruebas y construir

```powershell
cd .\Backend\mesaayudati
.\mvnw.cmd test
.\mvnw.cmd package
```

Las pruebas automatizadas usan H2 en memoria y no requieren MySQL; la aplicación normal usa exclusivamente MySQL. Los reportes quedan en `target/surefire-reports` y el JAR ejecutable en `target/mesaayudati-0.0.1-SNAPSHOT.jar`.

## Recorrido recomendado

1. Administrador: crear un área y una categoría; revisar prioridades y matriz; crear usuarios.
2. Solicitante: crear un ticket y comprobar prioridad y SLA calculados.
3. Coordinador: asignar el ticket a un técnico.
4. Técnico: actualizar el estado permitido y añadir un comentario.
5. Coordinador o técnico asignado: cerrar el ticket ya resuelto.
6. Solicitante: consultar el historial y reabrir con un motivo.
7. Administrador: probar campos vacíos, IDs inválidos y registros duplicados; verificar los errores JSON.

## Despliegue y límites

GitHub Pages solo sirve el frontend. Para desplegar el sistema completo se necesita un servidor Java y una base de datos. Configura `JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, el origen CORS y `API_BASE_URL` para el backend. No publiques credenciales locales. La conexión con MySQL debe validarse en el equipo donde se realizará la exposición.
