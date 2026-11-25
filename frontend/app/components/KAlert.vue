<script setup lang="ts">
defineProps<{ tipo: 'sucesso' | 'info' | 'atencao' | 'erro' }>()
</script>

<template>
  <div
    class="alerta"
    :class="tipo"
    :role="tipo === 'erro' ? 'alert' : 'status'"
  >
    <slot />
  </div>
</template>

<style scoped>
.alerta {
  padding: 16px 20px;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 20px;
  font-weight: 500;
}

.sucesso { background: var(--color-bg-success); color: var(--color-text-success); }
.info { background: var(--color-bg-info); color: var(--color-text-info); }
.atencao { background: var(--color-bg-warning); color: var(--color-text-warning); }
.erro { background: var(--color-bg-danger); color: var(--color-text-danger-strong); }
</style>
