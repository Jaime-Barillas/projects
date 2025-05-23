"use strict";

document.addEventListener('scroll', (_) => {
  const max = document.documentElement.scrollHeight;
  const at = document.documentElement.scrollTop;
  if (at > 0.5 * max) {
    const top_link = document.body.querySelector('.top-link');
    top_link.style.opacity = '1';
  } else {
    const top_link = document.body.querySelector('.top-link');
    top_link.style.opacity = '0';
  }

});
