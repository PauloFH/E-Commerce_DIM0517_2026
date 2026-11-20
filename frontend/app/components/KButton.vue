<script setup lang="ts">
const props = withDefaults(defineProps<{
  variante?: 'comprar' | 'primario' | 'secundario' | 'texto'
  disabled?: boolean
  to?: string
}>(), { variante: 'primario', to: undefined })

// link desabilitado não faz sentido: vira botão desabilitado
const comoLink = computed(() => !!props.to && !props.disabled)
</script>

<template>
  <NuxtLink v-if="comoLink" :to="to" class="botao" :class="variante">
    <slot />
  </NuxtLink>
  <button v-else type="button" class="botao" :class="variante" :disabled="disabled">
    <slot />
  </button>
</template>

<style scoped>
.botao {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  height: 48px;
  padding: 0 24px;
  border: 2px solid transparent;
  border-radius: var(--radius-full);
  font-size: 16px;
  line-height: 24px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.comprar { background: var(--color-bg-accent); color: var(--color-text-on-accent); }
.comprar:hover { background: var(--color-bg-accent-hover); }

.primario { background: var(--color-bg-brand); color: var(--color-text-on-brand); }
.primario:hover { background: var(--color-bg-brand-hover); }

.secundario { background: var(--color-bg-surface); border-color: var(--color-border-brand); color: var(--color-text-brand); }
.secundario:hover { background: var(--color-bg-info); }

.texto { background: none; color: var(--color-text-brand); padding: 0 var(--spacing-3); }
.texto:hover { background: var(--color-bg-info); }

.botao:disabled {
  background: var(--color-bg-disabled);
  border-color: transparent;
  color: var(--color-text-disabled);
  cursor: not-allowed;
}
</style>
