/**
 * Módulo de Cálculo de Aproximación de Tarifas — Andén
 * Historia 12 · RF-31 (cálculo) y RF-32 (aviso de estimación)
 * Funciona en el navegador (window.Tarifas) y en Node (require).
 * Los valores de referencia viven en datos.js.
 */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory(require('./datos'));
  else root.Tarifas = factory(root.TarifasDatos);
})(typeof self !== 'undefined' ? self : this, function (datos) {
  'use strict';

  const { TARIFAS, PASAJEROS_INCLUIDOS, RECARGO_PASAJERO_DIA, MARGEN } = datos;

  function validar({ dias, gama, pasajeros, trayectos }) {
    const errores = [];
    if (!TARIFAS[gama]) errores.push('Selecciona una gama de vehículo válida.');
    if (!Number.isInteger(dias) || dias < 1) errores.push('Los días deben ser un número entero mayor o igual a 1.');
    if (!Number.isInteger(pasajeros) || pasajeros < 1) errores.push('Indica al menos 1 pasajero.');
    else if (TARIFAS[gama] && pasajeros > TARIFAS[gama].capacidad)
      errores.push(`La gama ${TARIFAS[gama].nombre} admite máximo ${TARIFAS[gama].capacidad} pasajeros.`);
    if (!Number.isInteger(trayectos) || trayectos < 0) errores.push('Los trayectos deben ser un número entero mayor o igual a 0.');
    return errores;
  }

  return { TARIFAS, validar };
});
