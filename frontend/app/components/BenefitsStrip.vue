<script setup lang="ts">
const { t } = useI18n()

const itens = [
  { icone: 'busca', chave: 'busca' },
  { icone: 'carrinho', chave: 'compra' },
  { icone: 'loja', chave: 'venda' },
] as const
</script>

<template>
  <ul class="faixa">
    <li
      v-for="item in itens"
      :key="item.chave"
      class="item"
    >
      <KIcon
        :nome="item.icone"
        class="icone"
      />
      <div>
        <p class="body titulo">
          {{ t(`beneficios.${item.chave}.titulo`) }}
        </p>
        <p class="caption legenda">
          {{ t(`beneficios.${item.chave}.legenda`) }}
        </p>
      </div>
    </li>
  </ul>
</template>

<style scoped>
.faixa {
  display: flex;
  padding: 18px 24px;
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

.item {
  display: flex;
  flex: 1;
  align-items: flex-start;
  gap: var(--spacing-3);
}

.item + .item {
  margin-left: var(--spacing-6);
  padding-left: var(--spacing-6);
  border-left: 1px solid var(--color-border-default);
}

.icone { flex: none; color: var(--color-text-brand); }
.titulo { font-weight: 600; }
.legenda { color: var(--color-text-secondary); }

@media (max-width: 767px) {
  .faixa { flex-direction: column; }

  .item + .item {
    margin: var(--spacing-4) 0 0;
    padding: var(--spacing-4) 0 0;
    border-left: 0;
    border-top: 1px solid var(--color-border-default);
  }
}
</style>
