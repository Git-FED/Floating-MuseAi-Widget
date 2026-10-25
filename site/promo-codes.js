document.querySelectorAll('.copy-code').forEach((button) => {
  button.addEventListener('click', async () => {
    const code = button.dataset.code || '';
    try {
      await navigator.clipboard.writeText(code);
      const original = button.textContent;
      button.textContent = 'Copied';
      setTimeout(() => { button.textContent = original; }, 1400);
    } catch {
      button.textContent = code;
      setTimeout(() => { button.textContent = 'Copy code'; }, 1400);
    }
  });
});
