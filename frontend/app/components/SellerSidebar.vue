<script setup lang="ts">
const { t } = useI18n()

const itens = [
  { to: '/vendedor', icone: 'inicio', chave: 'visaoGeral' },
  { to: '/vendedor/produtos/novo', icone: 'mais', chave: 'novoProduto' },
  { to: '/vendedor/estoque', icone: 'estoque', chave: 'gerenciarEstoque' }
] as const
</script>

<template>
  <nav class="menu" :aria-label="t('vendedor.menu')">
    <div class="cabecalho">
      <p class="heading-sm">{{ t('vendedor.titulo') }}</p>
      <p class="caption legenda">{{ t('vendedor.legenda') }}</p>
    </div>

    <ul>
      <li v-for="item in itens" :key="item.to">
        <NuxtLink :to="item.to" class="item">
          <KIcon :nome="item.icone" :tamanho="20" />
          {{ t(`vendedor.${item.chave}`) }}
        </NuxtLink>
      </li>
    </ul>

    <hr>
    <NuxtLink to="/" class="item">
      <KIcon nome="loja" :tamanho="20" />
      {{ t('vendedor.voltarLoja') }}
    </NuxtLink>
  </nav>
</template>

<style scoped>
.menu {
  width: 264px;
  flex: none;
  padding: 16px 12px;
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

.cabecalho { padding: 0 var(--spacing-3) var(--spacing-3); }
.legenda { color: var(--color-text-secondary); }

.item {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  min-height: 44px;
  padding: 0 var(--spacing-3);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.item:hover { background: var(--color-bg-info); }
.item[aria-current='page'] { background: var(--color-bg-info); color: var(--color-text-info); }

hr {
  margin: var(--spacing-2) var(--spacing-3);
  border: 0;
  border-top: 1px solid var(--color-border-default);
}

@media (max-width: 767px) {
  .menu { width: 100%; }
}
</style>
