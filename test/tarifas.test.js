const test = require('node:test');
const assert = require('node:assert/strict');
const { calcularEstimado, AVISO_ESTIMACION } = require('../src/tarifas');

test('RF-31: calcula según días, gama y trayectos', () => {
  const r = calcularEstimado({ dias: 3, gama: 'estandar', pasajeros: 2, trayectos: 6 });
  assert.equal(r.ok, true);
  assert.equal(r.estimado, 960000); // 3×250.000 + 6×35.000
});

test('RF-31: cobra recargo desde el 5.º pasajero', () => {
  const r = calcularEstimado({ dias: 2, gama: 'estandar', pasajeros: 5, trayectos: 4 });
  assert.equal(r.estimado, 670000); // 500.000 + 140.000 + 1×15.000×2
});

test('Dado que el turista reserva, cuando calcula el estimado, entonces se aclara que es una estimación y que los trayectos extra se acuerdan con el conductor (RF-32)', () => {
  const r = calcularEstimado({ dias: 3, gama: 'estandar', pasajeros: 2, trayectos: 6 });
  assert.equal(r.aviso, AVISO_ESTIMACION);
  assert.match(r.aviso, /estimación/);
  assert.match(r.aviso, /conductor/);
  assert.equal(r.minimo, 816000);
  assert.equal(r.maximo, 1104000);
});

test('rechaza pasajeros por encima de la capacidad de la gama', () => {
  const r = calcularEstimado({ dias: 1, gama: 'economica', pasajeros: 5, trayectos: 1 });
  assert.equal(r.ok, false);
  assert.match(r.errores[0], /máximo 4/);
});

test('rechaza días inválidos y gama desconocida', () => {
  const r = calcularEstimado({ dias: 0, gama: 'lujo', pasajeros: 1, trayectos: 0 });
  assert.equal(r.ok, false);
  assert.equal(r.errores.length, 2);
});

test('acepta cero trayectos (solo tarifa diaria)', () => {
  const r = calcularEstimado({ dias: 1, gama: 'economica', pasajeros: 1, trayectos: 0 });
  assert.equal(r.estimado, 180000);
});
