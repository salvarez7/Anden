
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory(require('./datos'));
  else root.Tarifas = factory(root.TarifasDatos);
})(typeof self !== 'undefined' ? self : this, function (datos) {
  'use strict';

  const { TARIFAS, PASAJEROS_INCLUIDOS, RECARGO_PASAJERO_DIA, MARGEN } = datos;

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
    };
  }

  return { TARIFAS, validar, calcularEstimado };
});
