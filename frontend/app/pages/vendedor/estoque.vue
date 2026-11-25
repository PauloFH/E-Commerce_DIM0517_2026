<script setup lang="ts">
definePageMeta({ layout: 'vendedor' })

const { t } = useI18n()
useHead({ title: () => `${t('paginas.estoque.titulo')} · kompraki` })
</script>

<template>
  <div class="pagina">
    <KBreadcrumb
      :itens="[
        { texto: t('vendedor.titulo'), to: '/vendedor' },
        { texto: t('paginas.estoque.titulo') },
      ]"
    />
    <h1 class="heading-lg">
      {{ t('paginas.estoque.titulo') }}
    </h1>
    <p class="body">
      {{ t('paginas.estoque.texto') }}
    </p>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }
</style>
