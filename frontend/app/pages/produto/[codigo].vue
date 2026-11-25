<script setup lang="ts">
// só aceita código inteiro positivo; qualquer outro valor cai no 404
definePageMeta({ validate: route => /^[1-9]\d*$/.test(String(route.params.codigo)) })

const { t } = useI18n()
const route = useRoute()
const codigo = computed(() => String(route.params.codigo))
const titulo = computed(() => t('paginas.produto.seo', { codigo: codigo.value }))

useHead({ title: () => `${titulo.value} · kompraki` })
</script>

<template>
  <div class="pagina">
    <KBreadcrumb :itens="[{ texto: t('nav.inicio'), to: '/' }, { texto: titulo }]" />
    <h1 class="heading-lg">{{ titulo }}</h1>
    <p class="body">{{ t('paginas.produto.texto') }}</p>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }
</style>
