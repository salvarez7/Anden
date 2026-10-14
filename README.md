# Andén — Index (Hasta el spring 3 terminado)

## Instrucciones 
-ir a la rama index-y-panel/
-hacer el git clone ahi
-Ya clonada, entrar en Anden\demo\src\main\java\com\example\demo y ejecutar DemoApplication.java
-Esperar que ejecute y abrir http://localhost:8080/ en la web
-El proyecto comenzará en el index y podrás moverte por toda la web


 
## Aclaraciones
- El pipeline usa Node 20 y git. No hay dependencias externas.
- Un navegador: Chrome, Edge o Firefox (RNF08).

 
## Estructura (Contenido)
-Templates: Los archivos HTML con el front
-Config: Guarda los archivos de configuración del sistema.   
-Controller: Reciben las peticiones HTTP del frontend y devuelve las respuestas en JSON.   
-demo: Es la carpeta raíz generado por defecto por Spring Initializr, contiene el método @SpringBootApplication para arrancar el proyecto.   
-Model: Define los objetos de datos y entidades  que representan las tablas de la BD en mongoDb .   
-Repository: Contiene las interfaces que heredan de JpaRepository. Se encargan de la comunicación directa con MySQL usando consultas SQL/HQL para guardar, buscar o eliminar datos via JDBC. -Service: Contiene la lógica de negocio principal de la aplicación (cálculo de tarifas, reglas de validación y comunicación con APIs externas como WhatsApp)  

## Equipo y roles
Santiago: SP1 : Formulario de inicio de sesión · Autenticación con contraseña · Validar credenciales con la BD · Redirección según tipo de usuario
          SP2 : Consulta de conductores disponibles · Mostrar foto de conductor y vehículo · Mostrar gama, experiencia, zonas y disponibilidad
          SP3 : Carga y actualización de fotografías · Vista de detalle del conductor · Mostrar experiencia, zonas y disponibilidad · Mostrar fotografías del vehículo

`
Sofia : SP1 : Formulario de registro (turista y conductor) · Validación de correo · Validación de teléfono · Perfil Turista
        SP2 : Consultar vehículos según filtros · Mensaje cuando no hay resultados
        SP3 : Consultar conductores disponibles (reserva) · Validar disponibilidad en las fechas elegidas · Programar recordatorio 3 días antes


Nikol : SP1 : Almacenamiento de datos al registrarse · Validación de mayoría de edad · Validación de 2 años de licencia · Mensaje cuando el conductor no cumple los requisitos
        SP2 : Validar disponibilidad del vehículo · Sugerir vehículos similares
        SP3 : Formulario de reserva · Selección de fechas · Botón para iniciar la reserva · Enlace de contacto por WhatsApp







 
## Uso de IA
Se usó Claude como apoyo para generar un borrador del código, de las pruebas y del workflow.

 
 
