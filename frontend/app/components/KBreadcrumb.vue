<script setup lang="ts">
defineProps<{ itens: { texto: string, to?: string }[] }>()
const { t } = useI18n()
</script>

<template>
  <nav :aria-label="t('geral.voce')" class="caption trilha">
    <ol>
      <li v-for="(item, i) in itens" :key="i">
        <NuxtLink v-if="item.to" :to="item.to">{{ item.texto }}</NuxtLink>
        <span v-else aria-current="page">{{ item.texto }}</span>
        <span v-if="i < itens.length - 1" class="separador" aria-hidden="true">›</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.trilha { color: var(--color-text-secondary); }
ol { display: flex; flex-wrap: wrap; }
.separador { margin-inline: var(--spacing-1); }
a { text-decoration: none; }
a:hover { text-decoration: underline; }
</style>
