<script setup lang="ts">
const props = withDefaults(defineProps<{
  min?: number
  max: number
  disabled?: boolean
}>(), { min: 1 })

const modelo = defineModel<number>({ required: true })
const { t } = useI18n()

// o campo guarda o texto digitado; o valor válido só é confirmado no blur
const texto = ref(String(modelo.value))
watch(modelo, (valor) => {
  texto.value = String(valor)
})

function definir(valor: string | number) {
  modelo.value = limitarQuantidade(valor, props.min, props.max, modelo.value)
}

function aoDigitar(evento: Event) {
  texto.value = (evento.target as HTMLInputElement).value
  definir(texto.value)
}

function confirmar() {
  texto.value = String(modelo.value)
}
</script>

<template>
  <div class="quantidade">
    <button
      type="button"
      class="botao"
      :aria-label="t('quantidade.diminuir')"
      :disabled="disabled"
      @click="definir(modelo - 1)"
    >
      −
    </button>
    <input
      :value="texto"
      class="campo"
      type="text"
      inputmode="numeric"
      :aria-label="t('quantidade.rotulo')"
      :disabled="disabled"
      @input="aoDigitar"
      @blur="confirmar"
    >
    <button
      type="button"
      class="botao"
      :aria-label="t('quantidade.aumentar')"
      :disabled="disabled"
      @click="definir(modelo + 1)"
    >
      +
    </button>
  </div>
</template>

<style scoped>
.quantidade { display: inline-flex; gap: var(--spacing-2); }

.botao,
.campo {
  width: 44px;
  height: 44px;
  border: 1px solid var(--color-border-brand);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.botao { padding: 0; color: var(--color-text-brand); }
.botao:hover:not(:disabled) { background: var(--color-bg-info); }

.campo { border-color: var(--color-border-field); padding: 0; }

.botao:disabled,
.campo:disabled {
  border-color: transparent;
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}
</style>
