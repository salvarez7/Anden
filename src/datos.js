
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory();
  else root.TarifasDatos = factory();
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';
  return {
    TARIFAS: {
      economica: { nombre: 'Económica', diaria: 180000, trayecto: 25000, capacidad: 4 },
      estandar:  { nombre: 'Estándar',  diaria: 250000, trayecto: 35000, capacidad: 6 },
      premium:   { nombre: 'Premium',   diaria: 380000, trayecto: 55000, capacidad: 8 },
    },
    PASAJEROS_INCLUIDOS: 4,      // Se cobra un recargo de 15.000 desde el quinto pasajero
    RECARGO_PASAJERO_DIA: 15000,
    MARGEN: 0.15,
  };
});
