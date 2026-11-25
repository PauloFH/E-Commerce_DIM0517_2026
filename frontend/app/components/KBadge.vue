<script setup lang="ts">
defineProps<{ tipo: 'em-estoque' | 'ultimas' | 'esgotado' | 'novo' }>()
</script>

<template>
  <span
    class="selo"
    :class="tipo"
  ><slot /></span>
</template>

<style scoped>
.selo {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: 13px;
  line-height: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.em-estoque { background: var(--color-bg-success); color: var(--color-text-success); }
.ultimas { background: var(--color-bg-warning); color: var(--color-text-warning); }
.esgotado { background: var(--color-bg-danger); color: var(--color-text-danger-strong); }
.novo { background: var(--color-bg-accent); color: var(--color-text-on-accent); }
</style>
