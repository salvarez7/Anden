# Andén — Historia 12: estimación de tarifa
Como turista quiero ver un valor aproximado del costo total del servicio al reservar,
para tener una referencia económica antes de confirmar .
 
## Aclaraciones
- El pipeline usa Node 20 y git. No hay dependencias externas.
- Un navegador: Chrome, Edge o Firefox (RNF08).
 
## Levantar el proyecto desde cero
npm ci (instala dependencias)
npm run build (verifica la sintaxis de src)
con: npm test (para correr las pruebas)
 
## Estructura (Contenido)
-index.html: Interfaz responsive con la paleta del prototipo
-datos.js: Capa de datos: gamas y tarifas de referencia (COP)
-tarifas.js: Lógica de cálculo, validaciones y aviso de estimación
`tarifas.test.js: Pruebas automatizadas
-build.js: Verificación de sintaxis ("compilación")
-Pipeline: `npm ci` → `npm run build` → `npm test` en cada push y pull request
 
## Pipeline de CI
GitHub ejecuta el pipeline en cada push y pull request. El estado actual se ve en el badge de arriba.
 
## Equipo y roles
Sofia (Base del proyecto, pipeline de CI y README) -  `ci/pipeline-inicial`
Santiago (Lógica de estimación datos y cálculo) - `feat/h12-logica`
Nikol ( Pruebas automatizadas y pantalla) - `pruebas/vista`
 
## Uso de IA
Se usó Claude como apoyo para generar un borrador del código, de las pruebas y del workflow.
El equipo revisó el código, ejecutó las pruebas, ajustó los valores de tarifa y validó que cumple los criterios de la historia.
 
 s
