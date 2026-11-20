<script setup lang="ts">
const props = defineProps<{ codigo: number, preco?: number, estoque?: number }>()

const { t, locale } = useI18n()
const { adicionar } = useCarrinho()

const selo = computed(() => seloEstoque(props.estoque))
const adicionado = ref(false)

function adicionarAoCarrinho() {
  adicionar(props.codigo)
  adicionado.value = true
}
</script>

<template>
  <article class="cartao">
    <div class="topo">
      <span class="caption">{{ t('produto.cod') }}</span>
      <span class="codigo">{{ codigo }}</span>
    </div>

    <div class="corpo">
      <h3 class="body titulo">
        <NuxtLink :to="`/produto/${codigo}`" class="link">{{ t('produto.titulo', { codigo }) }}</NuxtLink>
      </h3>

      <div class="linha">
        <span v-if="preco !== undefined" class="heading-md">{{ formatarPreco(preco, locale) }}</span>
        <span v-else class="caption sem-preco">{{ t('produto.precoFechamento') }}</span>

        <button
          v-if="estoque !== 0"
          type="button"
          class="adicionar"
          :aria-label="t('produto.adicionar', { codigo })"
          @click="adicionarAoCarrinho"
        >
          <KIcon nome="carrinho" />
        </button>
      </div>

      <KBadge v-if="selo" :tipo="selo">
        <template v-if="selo === 'em-estoque'">{{ t('selo.emEstoque') }}</template>
        <template v-else-if="selo === 'ultimas'">{{ t('selo.ultimas', estoque!) }}</template>
        <template v-else>{{ t('selo.esgotado') }}</template>
      </KBadge>

      <span class="visually-hidden" role="status">{{ adicionado ? t('produto.adicionado', { codigo }) : '' }}</span>
    </div>
  </article>
</template>

<style scoped>
.cartao {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
  box-shadow: var(--elevation-1);
}

.topo {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding-left: var(--spacing-4);
  height: 136px;
  background: var(--color-bg-info);
  color: var(--color-text-secondary);
}

.codigo {
  color: var(--color-text-brand);
  font-size: 40px;
  line-height: 48px;
  font-weight: 800;
}

.corpo {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  padding: var(--spacing-4);
}

.titulo { font-weight: 600; }

/* área clicável do card inteiro, sem envolver o botão num link */
.link { text-decoration: none; }
.link::after {
  content: '';
  position: absolute;
  inset: 0;
}

.linha {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-2);
  height: 44px;
}

.sem-preco { color: var(--color-text-secondary); }

.adicionar {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  flex: none;
  width: 44px;
  height: 44px;
  border: 1.5px solid var(--color-border-brand);
  border-radius: var(--radius-full);
  background: var(--color-bg-surface);
  color: var(--color-text-brand);
}

.adicionar:hover { background: var(--color-bg-info); }
</style>
