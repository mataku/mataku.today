(function() {
  const LIGHT_COLOR = '#F2EDE5';
  const DARK_COLOR = '#222831';

  function updateThemeColor(theme) {
    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) {
      meta.content = theme === 'dark' ? DARK_COLOR : LIGHT_COLOR;
    }
  }

  function getEffectiveTheme() {
    const stored = localStorage.getItem('theme');
    if (stored) return stored;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  const theme = localStorage.getItem('theme');
  if (theme) {
    document.documentElement.setAttribute('data-theme', theme);
  }
  updateThemeColor(getEffectiveTheme());

  document.addEventListener('DOMContentLoaded', function() {
    requestAnimationFrame(function() {
      document.body.classList.add('theme-ready');
    });

    const toggle = document.getElementById('theme-toggle');
    if (!toggle) return;

    toggle.addEventListener('click', function() {
      const current = getEffectiveTheme();
      const next = current === 'dark' ? 'light' : 'dark';
      localStorage.setItem('theme', next);
      document.documentElement.setAttribute('data-theme', next);
      updateThemeColor(next);
    });
  });
})();
