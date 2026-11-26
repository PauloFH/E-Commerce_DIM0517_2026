<script setup lang="ts">
import type { ErroVenda } from '~/utils/venda'

defineProps<{
  codigo: number
  quantidade: number
  subtotal: string
  erro?: Extract<ErroVenda, { codigo: number }>
  mensagemErro?: string
  desabilitado?: boolean
}>()

defineEmits<{
  alterar: [quantidade: number]
  remover: []
}>()

const { t } = useI18n()
const LIMITE = 2_147_483_647
</script>

<template>
  <li
    class="linha"
    :class="{ comErro: erro }"
  >
    <div
      class="imagem"
      aria-hidden="true"
    >
      <KIcon
        nome="estoque"
        :tamanho="32"
      />
    </div>
    <div class="info">
      <h2 class="heading-sm">
        {{ t('produto.titulo', { codigo }) }}
      </h2>
      <p class="caption codigo">
        {{ t('paginas.carrinho.codigo', { codigo }) }}
      </p>
    </div>
    <div
      class="seletor"
      role="group"
      :aria-label="t('paginas.carrinho.quantidadeDe', { codigo })"
    >
      <KQuantidade
        :model-value="quantidade"
        :max="LIMITE"
        :disabled="desabilitado"
        @update:model-value="$emit('alterar', $event)"
      />
    </div>
    <p class="subtotal">
      {{ subtotal }}
    </p>
    <button
      type="button"
      class="remover label"
      :class="{ destaque: erro?.tipo === 'inexistente' }"
      :aria-label="t('paginas.carrinho.removerProduto', { codigo })"
      :disabled="desabilitado"
      @click="$emit('remover')"
    >
      <KIcon
        nome="lixeira"
        :tamanho="20"
      />
      {{ t('paginas.carrinho.remover') }}
    </button>
    <p
      v-if="erro && mensagemErro"
      class="mensagem caption"
    >
      {{ mensagemErro }}
    </p>
  </li>
</template>

<style scoped>
.linha {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) auto 168px auto;
  grid-template-areas: 'imagem info seletor subtotal remover' 'mensagem mensagem mensagem mensagem mensagem';
  align-items: center;
  column-gap: var(--spacing-4);
  row-gap: var(--spacing-3);
  padding: var(--spacing-5);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

.comErro { border: 2px solid var(--color-border-danger); padding: calc(var(--spacing-5) - 1px); }

.imagem {
  grid-area: imagem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-info);
  color: var(--color-text-brand);
}

.info { grid-area: info; display: flex; flex-direction: column; gap: var(--spacing-1); min-width: 0; }
.codigo { color: var(--color-text-secondary); }
.seletor { grid-area: seletor; }
.subtotal { grid-area: subtotal; text-align: right; font-weight: 600; }

.remover {
  grid-area: remover;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  min-height: 44px;
  padding: 0 var(--spacing-3);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: none;
  color: var(--color-text-danger);
}

.remover:hover:not(:disabled) { background: var(--color-bg-danger); }
.remover.destaque { border-color: var(--color-border-danger); background: var(--color-bg-danger); }
.remover:disabled { color: var(--color-text-disabled); cursor: not-allowed; }

.mensagem {
  grid-area: mensagem;
  padding: var(--spacing-2) var(--spacing-3);
  border-radius: var(--radius-sm);
  background: var(--color-bg-danger);
  color: var(--color-text-danger-strong);
}

@media (max-width: 767px) {
  .linha {
    grid-template-columns: 72px minmax(0, 1fr) auto;
    grid-template-areas: 'imagem info info' 'imagem subtotal subtotal' 'seletor seletor remover' 'mensagem mensagem mensagem';
    align-items: start;
    row-gap: var(--spacing-1);
  }

  .subtotal { text-align: left; }
  .seletor { margin-top: var(--spacing-3); }
  .remover { margin-top: var(--spacing-3); justify-self: end; }
  .mensagem { margin-top: var(--spacing-3); }
}
</style>
