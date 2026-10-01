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

  const AVISO_ESTIMACION =
    'Este valor es solo una estimación. El precio final se define al concluir el viaje. ' +
    'Si los trayectos resultan más largos de lo indicado, el ajuste se acuerda directamente con el conductor.';

  const redondearMil = (n) => Math.round(n / 1000) * 1000;

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

  function calcularEstimado(entrada) {
    const errores = validar(entrada);
    if (errores.length) return { ok: false, errores };

    const { dias, gama, pasajeros, trayectos } = entrada;
    const t = TARIFAS[gama];
    const base = dias * t.diaria;
    const porTrayectos = trayectos * t.trayecto;
    const extra = Math.max(0, pasajeros - PASAJEROS_INCLUIDOS) * RECARGO_PASAJERO_DIA * dias;
    const estimado = base + porTrayectos + extra;

    return {
      ok: true,
      estimado,
      minimo: redondearMil(estimado * (1 - MARGEN)),
      maximo: redondearMil(estimado * (1 + MARGEN)),
      desglose: { base, porTrayectos, extra },
      aviso: AVISO_ESTIMACION,
    };
  }

  const formatoCOP = (n) =>
    new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(n);

  return { TARIFAS, AVISO_ESTIMACION, calcularEstimado, formatoCOP };
});
