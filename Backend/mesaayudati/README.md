# Mesa de Ayuda TI

API REST desarrollada con Spring Boot y Java 21. Permite al frontend autenticar usuarios y gestionar catálogos y tickets.

## Requisitos

- Java 21
- PowerShell
- Live Server en Visual Studio Code para abrir el frontend

Comprueba la versión de Java con:

```powershell
java -version
```

## Iniciar el sistema

1. Abre PowerShell en la carpeta raíz del repositorio y entra al backend:

	```powershell
	cd .\Backend\mesaayudati
	```

2. En el primer inicio, configura la contraseña de las cuentas de prueba y arranca Spring Boot:

	```powershell
	$env:INITIAL_TEST_USERS_PASSWORD = "NuevaClave123"
	.\mvnw.cmd spring-boot:run
	```

	Spring inicia la API en `http://localhost:8080`. Mantén esta terminal abierta mientras utilizas el sistema.

3. Abre el archivo `index.html` de la raíz del repositorio con Live Server. La dirección habitual es `http://localhost:5500`.

El backend y Live Server son procesos separados; ambos deben estar activos para probar la aplicación web. Para detener Spring Boot, pulsa `Ctrl+C` en su terminal.

En los siguientes inicios, arranca Spring con el comando anterior sin volver a definir la contraseña. Si reutilizas la misma terminal, ejecuta primero `Remove-Item Env:INITIAL_TEST_USERS_PASSWORD`.

## Credenciales de prueba

Las cuatro cuentas utilizan la misma contraseña local:

| Usuario | Rol | Contraseña |
| --- | --- | --- |
| `admin.prueba` | Administrador | `NuevaClave123` |
| `coordinador.prueba` | Coordinador | `NuevaClave123` |
| `tecnico.prueba` | Técnico | `NuevaClave123` |
| `solicitante.prueba` | Solicitante | `NuevaClave123` |

Estas credenciales son para pruebas locales; no deben utilizarse en producción. Para cambiar la contraseña inicial, modifica `INITIAL_TEST_USERS_PASSWORD` antes del primer inicio.

## Prueba del flujo

1. Inicia sesión como `admin.prueba`.
2. Crea un área y una categoría en Administración. Las prioridades y sus reglas ya están precargadas.
3. Crea usuarios para coordinador, técnico y solicitante, y asigna un área a cada uno.
4. Inicia sesión como el solicitante creado y registra un ticket.
5. Inicia sesión como el coordinador y asigna el ticket al técnico.
6. Inicia sesión como el técnico para actualizar el estado y añadir comentarios.
7. Regresa al usuario solicitante para consultar el historial o reabrir el ticket.

El backend calcula la prioridad y el SLA según el impacto y la urgencia del ticket.

## Datos y despliegue

En local, la aplicación utiliza una base de datos H2 persistente en `Backend/mesaayudati/data/mesaayudati`. Los datos se conservan al detener y volver a iniciar Spring Boot.

GitHub Pages publica archivos estáticos y no ejecuta esta API Java. Para publicar el sistema completo, despliega el backend en un servicio con HTTPS, configura una base de datos y actualiza `API_BASE_URL` en `Frontend/App/config.js`. No publiques las credenciales de prueba ni uses `localhost` como dirección del backend público.