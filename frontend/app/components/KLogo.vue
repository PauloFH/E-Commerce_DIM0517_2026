<script setup lang="ts">
withDefaults(defineProps<{ fundo?: 'claro' | 'escuro' }>(), { fundo: 'claro' })
const { t } = useI18n()
</script>

<template>
  <NuxtLink
    to="/"
    class="logo"
    :class="fundo"
    :aria-label="t('marca.inicio')"
  >
    <svg
      width="32"
      height="32"
      viewBox="0 0 64 64"
      fill="none"
      aria-hidden="true"
      focusable="false"
    >
      <path
        class="pino"
        d="M32 3C17.6 3 7 13.8 7 27.6 7 45.2 32 61 32 61s25-15.8 25-33.4C57 13.8 46.4 3 32 3Z"
      />
      <path
        class="k"
        d="M25 15v24M39.5 16 26.5 29M30 26.5l10.5 12.5"
        stroke-width="5.5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
    <span
      class="texto"
      aria-hidden="true"
    >kompraki</span>
  </NuxtLink>
</template>

<style scoped>
.logo {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2);
  text-decoration: none;
}

.texto {
  font-size: 24px;
  line-height: 32px;
  font-weight: 800;
  letter-spacing: -0.9px;
}

.claro .pino { fill: var(--color-bg-brand); }
.claro .k { stroke: var(--color-bg-accent); }
.claro .texto { color: var(--color-text-primary); }

.escuro .pino { fill: #FFFFFF; }
.escuro .k { stroke: var(--color-bg-brand); }
.escuro .texto { color: #FFFFFF; }
</style>
