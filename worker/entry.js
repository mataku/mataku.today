import { fetch } from './build/compileSync/js/main/productionExecutable/kotlin/mataku-today-worker.mjs';

export default {
  fetch(request, env, ctx) {
    return fetch(request, env);
  }
};
