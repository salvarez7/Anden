
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const dir = 'src';
const archivos = fs.existsSync(dir) ? fs.readdirSync(dir).filter((f) => f.endsWith('.js')) : [];
for (const f of archivos) {
  execFileSync(process.execPath, ['--check', path.join(dir, f)], { stdio: 'inherit' });
  console.log('OK', path.join(dir, f));
}
console.log(`Build correcto: ${archivos.length} archivo(s) verificados`);
