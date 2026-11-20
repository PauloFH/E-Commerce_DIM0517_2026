<script setup lang="ts">
const { t } = useI18n()

const entrada = ref<HTMLInputElement>()
const valor = ref('')
const erro = ref('')

function buscar() {
  const texto = valor.value.trim()
  if (!/^[1-9]\d*$/.test(texto)) {
    erro.value = t('busca.erro')
    return
  }
  erro.value = ''
  valor.value = ''
  navigateTo(`/produto/${texto}`)
}

defineExpose({ focar: () => entrada.value?.focus() })
</script>

<template>
  <form class="busca" role="search" novalidate @submit.prevent="buscar">
    <div class="campo" :class="{ comErro: erro }">
      <label for="busca-codigo" class="visually-hidden">{{ t('busca.rotulo') }}</label>
      <input
        id="busca-codigo"
        ref="entrada"
        v-model="valor"
        type="search"
        inputmode="numeric"
        autocomplete="off"
        :placeholder="t('busca.placeholder')"
        :aria-invalid="erro ? 'true' : undefined"
        :aria-describedby="erro ? 'busca-erro' : undefined"
      >
      <button type="submit" class="botao" :aria-label="t('busca.botao')">
        <KIcon nome="busca" :tamanho="20" />
      </button>
    </div>
    <p v-if="erro" id="busca-erro" class="erro" role="alert">{{ erro }}</p>
  </form>
</template>

<style scoped>
.campo {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 6px 0 var(--spacing-5);
  border: 2px solid transparent;
  border-radius: var(--radius-full);
  background: var(--color-bg-surface);
}

.comErro { border-color: var(--color-border-danger); }

input {
  flex: 1;
  min-width: 0;
  border: 0;
  background: none;
  font-size: 16px;
}

input:focus-visible { outline: none; }
.campo:focus-within { outline: 3px solid var(--color-border-focus); outline-offset: 2px; }

.botao {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: var(--radius-full);
  background: var(--color-bg-inverse);
  color: var(--color-text-on-brand);
}

.erro {
  margin-top: var(--spacing-1);
  padding: 0 var(--spacing-3);
  color: var(--color-text-on-brand);
  font-size: 13px;
  line-height: 18px;
  font-weight: 600;
}
</style>
