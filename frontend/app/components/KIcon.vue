<script setup lang="ts">
const caminhos = {
  busca: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
  carrinho: '<path d="M3 4h2l2.4 11.2a1 1 0 0 0 1 .8h8.9a1 1 0 0 0 1-.8L20 8H6.2"/><circle cx="9" cy="20" r="1.4"/><circle cx="17" cy="20" r="1.4"/>',
  menu: '<path d="M4 7h16M4 12h16M4 17h16"/>',
  idioma: '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9c-2.5-2.6-3.8-5.6-3.8-9S9.5 5.6 12 3Z"/>',
  loja: '<path d="M4 9.5 5.5 4h13L20 9.5M4 9.5V20h16V9.5M4 9.5a2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0M9.5 20v-5h5v5"/>',
  estoque: '<path d="M3 7.5 12 3l9 4.5v9L12 21l-9-4.5v-9Z"/><path d="M3 7.5 12 12l9-4.5M12 12v9"/>',
  mais: '<path d="M12 5v14M5 12h14"/>',
  inicio: '<path d="M4 11 12 4l8 7v9h-5v-6H9v6H4v-9Z"/>',
  fechar: '<path d="M6 6l12 12M18 6 6 18"/>'
}

defineProps<{ nome: keyof typeof caminhos, tamanho?: number }>()
</script>

<template>
  <!-- eslint-disable vue/no-v-html -->
  <svg
    :width="tamanho ?? 24"
    :height="tamanho ?? 24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
    v-html="caminhos[nome]"
  />
  <!-- eslint-enable vue/no-v-html -->
</template>
