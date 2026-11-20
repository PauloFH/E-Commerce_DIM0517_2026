<script setup lang="ts">
const { t } = useI18n()
useHead({ title: () => `${t('paginas.carrinho.titulo')} · kompraki` })
</script>

<template>
  <div class="pagina">
    <KBreadcrumb :itens="[{ texto: t('nav.inicio'), to: '/' }, { texto: t('paginas.carrinho.titulo') }]" />
    <h1 class="heading-lg">{{ t('paginas.carrinho.titulo') }}</h1>
    <p class="body">{{ t('paginas.carrinho.texto') }}</p>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }
</style>
