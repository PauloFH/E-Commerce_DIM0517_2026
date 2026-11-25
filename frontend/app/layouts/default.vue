<script setup lang="ts">
const { t } = useI18n()
</script>

<template>
  <div>
    <a
      href="#conteudo"
      class="visually-hidden pular"
    >{{ t('geral.pularConteudo') }}</a>
    <AppHeader />
    <main
      id="conteudo"
      class="container principal"
    >
      <slot />
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
.principal {
  padding-block: var(--spacing-8) var(--spacing-12);
}
</style>
