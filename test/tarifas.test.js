const test = require('node:test');
const assert = require('node:assert/strict');
const { calcularEstimado, AVISO_ESTIMACION } = require('../src/tarifas');

test('RF-31: calcula según días, gama y trayectos', () => {
  const r = calcularEstimado({ dias: 3, gama: 'estandar', pasajeros: 2, trayectos: 6 });
  assert.equal(r.ok, true);
  assert.equal(r.estimado, 960000); // 3×250.000 + 6×35.000
});
