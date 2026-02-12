import blog from './build/compileSync/js/main/productionExecutable/kotlin/mataku-today-worker.js';

export default {
  fetch(request, env, ctx) {
    return blog.fetch(request, env);
  }
};
