<script setup lang="ts">
defineOptions({ inheritAttrs: false })

defineProps<{ label: string, erro?: string }>()
const modelo = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="campo">
    <label :for="id" class="rotulo">{{ label }}</label>
    <input
      v-bind="$attrs"
      :id="id"
      v-model="modelo"
      class="entrada"
      :class="{ comErro: erro }"
      :aria-invalid="erro ? 'true' : undefined"
      :aria-describedby="erro ? `${id}-erro` : undefined"
    >
    <p v-if="erro" :id="`${id}-erro`" class="erro" role="alert">{{ erro }}</p>
  </div>
</template>

<style scoped>
.campo {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.rotulo {
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
}

.entrada {
  height: 48px;
  padding: 0 var(--spacing-4);
  border: 1.5px solid var(--color-border-field);
  border-radius: var(--radius-md);
  background: var(--color-bg-surface);
  font-size: 16px;
}

.comErro {
  border: 2px solid var(--color-border-danger);
}

.erro {
  color: var(--color-text-danger);
  font-size: 13px;
  line-height: 18px;
  font-weight: 500;
}
</style>
