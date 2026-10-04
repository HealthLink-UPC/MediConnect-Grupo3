
const toggle = document.querySelector('.nav-toggle');
const menu = document.getElementById('menu');

toggle.addEventListener('click', () => {
  const open = menu.classList.toggle('is-open');
  toggle.setAttribute('aria-expanded', String(open));
});

menu.querySelectorAll('a').forEach(link =>
  link.addEventListener('click', () => {
    menu.classList.remove('is-open');
    toggle.setAttribute('aria-expanded', 'false');
  })
);


const links = [...menu.querySelectorAll('a')];
const sections = links.map(a => document.querySelector(a.getAttribute('href')));

const observer = new IntersectionObserver(entries => {
  entries.forEach(entry => {
    if (!entry.isIntersecting) return;
    links.forEach(a => a.classList.toggle('is-active', a.getAttribute('href') === `#${entry.target.id}`));
  });
}, { rootMargin: '-40% 0px -55% 0px' });

sections.forEach(s => s && observer.observe(s));

const form = document.getElementById('contact-form');
const status = form.querySelector('.form__status');

form.addEventListener('submit', e => {
  e.preventDefault();
  let valid = true;

  form.querySelectorAll('input, textarea').forEach(el => {
    const ok = el.value.trim() !== '' && (el.type !== 'email' || /^\S+@\S+\.\S+$/.test(el.value));
    el.closest('.field').classList.toggle('has-error', !ok);
    if (!ok) valid = false;
  });

  status.className = 'form__status ' + (valid ? 'is-ok' : 'is-error');
  status.textContent = valid
    ? '¡Gracias! Tu mensaje fue enviado.'
    : 'Revisa los campos marcados.';

  if (valid) form.reset();
});
