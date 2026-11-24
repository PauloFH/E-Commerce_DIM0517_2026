<script setup lang="ts">
const { t } = useI18n()
</script>

<template>
  <div>
    <a href="#conteudo" class="visually-hidden pular">{{ t('geral.pularConteudo') }}</a>
    <AppHeader />
    <div class="container area">
      <SellerSidebar />
      <main id="conteudo" class="principal">
        <slot />
      </main>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.area {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-8);
  padding-block: var(--spacing-8);
}

.principal { flex: 1; min-width: 0; }


@media (max-width: 767px) {
  .area { flex-direction: column; align-items: stretch; }
}
</style>
