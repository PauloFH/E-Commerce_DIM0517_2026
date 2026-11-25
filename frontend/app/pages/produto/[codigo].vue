<script setup lang="ts">
import type { ProblemDetail } from '~/utils/api'

definePageMeta({
  // cada código monta a página do zero: o estoque e o 404 nunca vêm do produto anterior
  key: route => route.fullPath,
  // inteiro positivo que cabe no Integer da API; qualquer outro valor cai no 404
  validate: route => /^[1-9]\d*$/.test(String(route.params.codigo)) && Number(route.params.codigo) <= 2_147_483_647,
})

const { t, locale } = useI18n()
const route = useRoute()
const { consultarEstoque } = useLoja()
const { produtos, registrar } = useProdutosConhecidos()
const { adicionar } = useCarrinho()

const codigo = computed(() => Number(route.params.codigo))
const titulo = computed(() => t('paginas.produto.seo', { codigo: codigo.value }))

// a falha vira dado (e não exceção) para a página decidir entre 404 e alerta
const { data: consulta } = await useAsyncData(
  () => `estoque-${codigo.value}`,
  async () => {
    try {
      return { estoque: (await consultarEstoque(codigo.value)).estoque }
    }
    catch (erro) {
      return { falha: (erro as ProblemDetail).status }
    }
  },
  { watch: [codigo] },
)

if (consulta.value?.falha === 404) throw createError({ statusCode: 404, fatal: true })

const estoque = computed(() => consulta.value?.estoque)
const selo = computed(() => seloEstoque(estoque.value))
const esgotado = computed(() => selo.value === 'esgotado')
const preco = computed(() => produtos.value.find(p => p.codigo === codigo.value)?.preco)
const quantidade = ref(esgotado.value ? 0 : 1)
const adicionado = ref(false)

// o preço continua o que já era conhecido; aqui só se guarda o estoque visto
// depois da hidratação, para a leitura do localStorage não apagar esta visita
onNuxtReady(() => {
  watch([codigo, estoque], ([cod, est]) => {
    if (est !== undefined) registrar({ codigo: cod, estoque: est })
  }, { immediate: true })
})

watch(estoque, (novo) => {
  quantidade.value = limitarQuantidade(quantidade.value, novo ? 1 : 0, novo ?? 0, novo ? 1 : 0)
})
watch(codigo, () => {
  adicionado.value = false
})

function adicionarAoCarrinho() {
  adicionar(codigo.value, quantidade.value)
  adicionado.value = true
}

function comprarAgora() {
  adicionarAoCarrinho()
  return navigateTo('/carrinho')
}

useHead({ title: () => `${titulo.value} · kompraki` })
</script>

<template>
  <div class="pagina">
    <KBreadcrumb :itens="[{ texto: t('nav.inicio'), to: '/' }, { texto: titulo }]" />

    <div class="produto">
      <div
        class="arte"
        aria-hidden="true"
      >
        <span class="heading-sm">{{ t('produto.cod') }}</span>
        <span
          class="numero"
          :class="{ longo: String(codigo).length > 4 }"
        >{{ codigo }}</span>
      </div>

      <div class="lateral">
        <KAlert
          v-if="adicionado"
          tipo="sucesso"
        >
          <div class="aviso">
            <span>{{ t('paginas.produto.adicionado', { codigo }) }}</span>
            <KButton
              variante="secundario"
              to="/carrinho"
            >
              {{ t('paginas.produto.verCarrinho') }}
            </KButton>
          </div>
        </KAlert>

        <KAlert
          v-if="estoque === undefined"
          tipo="erro"
        >
          {{ t(consulta?.falha === 0 ? 'erros.rede' : 'erros.generico') }}
        </KAlert>

        <section
          v-else
          class="caixa"
        >
          <h1 class="heading-lg">
            {{ t('produto.titulo', { codigo }) }}
          </h1>

          <p
            v-if="preco !== undefined"
            class="preco"
          >
            {{ formatarPreco(preco, locale) }}
          </p>
          <p
            v-else
            class="body sem-preco"
          >
            {{ t('paginas.produto.precoConfirmado') }}
          </p>

          <KBadge :tipo="selo!">
            <template v-if="selo === 'em-estoque'">
              {{ t('paginas.produto.emEstoque', estoque) }}
            </template>
            <template v-else-if="selo === 'ultimas'">
              {{ t('selo.ultimas', estoque) }}
            </template>
            <template v-else>
              {{ t('selo.esgotado') }}
            </template>
          </KBadge>

          <div class="escolha">
            <p class="label">
              {{ t('quantidade.rotulo') }}
            </p>
            <div class="seletor">
              <KQuantidade
                v-model="quantidade"
                :min="esgotado ? 0 : 1"
                :max="estoque"
                :disabled="esgotado"
              />
              <span class="caption limite">
                {{ esgotado ? t('paginas.produto.indisponivel') : t('paginas.produto.maximo', estoque) }}
              </span>
            </div>
          </div>

          <div class="acoes">
            <KButton
              variante="comprar"
              :disabled="esgotado"
              @click="adicionarAoCarrinho"
            >
              {{ t('paginas.produto.adicionarCarrinho') }}
            </KButton>
            <KButton
              variante="primario"
              :disabled="esgotado"
              @click="comprarAgora"
            >
              {{ t('paginas.produto.comprarAgora') }}
            </KButton>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }

.produto {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 480px;
  gap: var(--spacing-12);
  padding-bottom: var(--spacing-12);
}

.arte {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 320px;
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

.lateral { display: flex; flex-direction: column; gap: var(--spacing-4); }

.aviso { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: var(--spacing-3); }

.caixa {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);
  padding: var(--spacing-6);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

.preco { font-size: 32px; line-height: 40px; font-weight: 800; }
.sem-preco { color: var(--color-text-secondary); }

.escolha {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
  padding-top: var(--spacing-4);
  border-top: 1px solid var(--color-border-default);
}

.seletor { display: flex; align-items: center; gap: var(--spacing-3); }
.limite { color: var(--color-text-secondary); }

.acoes { display: flex; flex-direction: column; gap: var(--spacing-3); }
.acoes :deep(.botao) { width: 100%; }

@media (max-width: 767px) {
  .produto { grid-template-columns: 1fr; gap: var(--spacing-4); }
  .arte { order: -1; min-height: 200px; padding-block: var(--spacing-6); }
  .numero { font-size: 80px; line-height: 88px; letter-spacing: -2.4px; }
  .numero.longo { font-size: 48px; line-height: 56px; letter-spacing: -1.2px; }
}
</style>
