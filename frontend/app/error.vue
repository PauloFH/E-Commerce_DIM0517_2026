<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{ error: NuxtError }>()
const { t, locale } = useI18n()
const route = useRoute()

// a página de erro é renderizada fora do app.vue, então define o idioma do <html> de novo
useHead({ htmlAttrs: { lang: () => locale.value } })

// 404 em /produto/... é "produto não encontrado"; outro 404 é página; o resto é falha
const tipo = computed(() => {
  if (props.error.statusCode !== 404) return 'Falha'
  return route.path.startsWith('/produto/') ? 'Produto' : '404'
})
const codigo = computed(() => {
  const bruto = route.path.split('/')[2] ?? ''
  try { return decodeURIComponent(bruto).slice(0, 8) }
  catch { return bruto.slice(0, 8) }
})
const titulo = computed(() => t(`erroPagina.titulo${tipo.value}`))
const numero = computed(() => (tipo.value === 'Produto' ? codigo.value : String(props.error.statusCode)))

useHead({ title: () => `${titulo.value} · kompraki` })

// clearError limpa o estado de erro; um link comum deixaria a página de erro presa
const voltar = () => clearError({ redirect: '/' })
</script>

<template>
  <NuxtLayout>
    <div class="erro">
      <div class="mensagem">
        <p class="label rotulo">{{ t('erroPagina.rotulo', { status: error.statusCode }) }}</p>
        <h1 class="titulo">{{ titulo }}</h1>
        <p class="body texto">{{ t(`erroPagina.texto${tipo}`, { codigo }) }}</p>
        <div class="acoes">
          <SearchBar id="busca-erro-pagina" :placeholder="t('erroPagina.buscarOutro')" contorno class="busca" />
          <KButton variante="primario" @click="voltar">{{ t('erroPagina.voltar') }}</KButton>
        </div>
      </div>
      <div class="arte" aria-hidden="true">
        <span class="heading-sm">{{ tipo === 'Produto' ? t('produto.cod') : t('erroPagina.legenda') }}</span>
        <span class="numero" :class="{ longo: numero.length > 4 }">{{ numero }}</span>
      </div>
    </div>
  </NuxtLayout>
</template>

<style scoped>
.erro {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  align-items: center;
  gap: var(--spacing-12);
  padding-block: var(--spacing-12);
}

.mensagem { display: flex; flex-direction: column; gap: var(--spacing-4); }
.rotulo { color: var(--color-text-secondary); }
.titulo { font-size: 33px; line-height: 40px; font-weight: 800; letter-spacing: -0.6px; }
.texto { color: var(--color-text-secondary); max-width: 620px; }
.acoes { display: flex; align-items: center; gap: var(--spacing-3); flex-wrap: wrap; }
.busca { width: 400px; max-width: 100%; }

.arte {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 320px;
  padding-left: var(--spacing-8);
  border-radius: var(--radius-sm);
  background: var(--color-bg-info);
  color: var(--color-text-info);
}

.numero {
  color: var(--color-text-brand);
  font-size: 120px;
  line-height: 132px;
  font-weight: 800;
  letter-spacing: -3.6px;
}

.numero.longo { font-size: 64px; line-height: 72px; letter-spacing: -1.6px; }

@media (max-width: 767px) {
  .erro { grid-template-columns: 1fr; gap: var(--spacing-8); padding-block: 0; }
  .arte { order: -1; height: 200px; }
  .numero { font-size: 80px; line-height: 88px; letter-spacing: -2.4px; }
  .numero.longo { font-size: 48px; line-height: 56px; letter-spacing: -1.2px; }
  .titulo { font-size: 28px; line-height: 36px; }
  .acoes { flex-direction: column; align-items: stretch; }
  .busca { width: 100%; }
}
</style>
