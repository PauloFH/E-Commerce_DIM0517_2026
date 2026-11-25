<script setup lang="ts">
import type { ProblemDetail } from '~/utils/api'
import type { ErroCampo } from '~/utils/formularioEstoque'

definePageMeta({ layout: 'vendedor' })

const { t, locale } = useI18n()
const route = useRoute()
const { consultarEstoque, darEntrada } = useLoja()
const { produtos, registrar } = useProdutosConhecidos()

useHead({ title: () => `${t('paginas.estoque.titulo')} · kompraki` })

const codigo = ref('')
const quantidade = ref('')
const produto = ref<{ codigo: number, estoque: number }>()
const ocupado = ref(false)
const erroCodigo = ref<ErroCampo>()
const erroQuantidade = ref<ErroCampo>()
const falha = ref<'erros.rede' | 'erros.generico'>()
const sucesso = ref<{ codigo: number, antes: number, depois: number }>()

const abas = computed(() => [{ id: 'repor', rotulo: t('paginas.estoque.repor') }])
const abaAtiva = ref(abas.value.find(a => a.id === route.query.aba)?.id ?? 'repor')

function trocarAba(id: string) {
  abaAtiva.value = id
  navigateTo({ query: { ...route.query, aba: id } }, { replace: true })
}

function teclaAba(evento: KeyboardEvent) {
  const ids = abas.value.map(a => a.id)
  const destino = indiceDaTecla(evento.key, ids.indexOf(abaAtiva.value), ids.length)
  if (destino === undefined) return
  evento.preventDefault()
  trocarAba(ids[destino]!)
  nextTick(() => document.getElementById(`aba-${ids[destino]}`)?.focus())
}

// o preço só existe no que este navegador já viu; a API de estoque não devolve
const preco = computed(() => produtos.value.find(p => p.codigo === produto.value?.codigo)?.preco)
const selo = computed(() => seloEstoque(produto.value?.estoque))
const rotuloSelo = computed(() => {
  if (selo.value === 'em-estoque') return t('selo.emEstoque')
  if (selo.value === 'ultimas') return t('selo.ultimas', produto.value!.estoque)
  return t('selo.esgotado')
})

const textoErro = (erro?: ErroCampo) => (erro ? t(erro.chave, erro.params ?? {}) : undefined)

watch(codigo, () => (erroCodigo.value = undefined))
watch(quantidade, () => (erroQuantidade.value = undefined))

function limparMensagens() {
  erroCodigo.value = undefined
  erroQuantidade.value = undefined
  falha.value = undefined
  sucesso.value = undefined
}

function tratarFalha(erro: unknown, numero: number) {
  const { status } = erro as ProblemDetail
  if (status === 404) {
    produto.value = undefined
    erroCodigo.value = { chave: 'paginas.estoque.erros.naoEncontrado', params: { codigo: numero } }
  }
  else {
    falha.value = status === 0 ? 'erros.rede' : 'erros.generico'
  }
}

// só a consulta mais recente atualiza a tela; uma resposta atrasada é descartada
let ultimaConsulta = 0

async function consultar() {
  limparMensagens()
  // o formulário de reposição some até o novo produto carregar, para nunca repor no anterior
  produto.value = undefined
  const invalido = validarCodigo(codigo.value)
  if (invalido) {
    erroCodigo.value = { chave: invalido === 'obrigatorio' ? 'paginas.estoque.erros.codigoVazio' : 'paginas.estoque.erros.codigoInvalido' }
    return
  }
  const numero = Number(codigo.value.trim())
  const consulta = ++ultimaConsulta
  ocupado.value = true
  try {
    await navigateTo({ query: { ...route.query, codigo: String(numero) } }, { replace: true })
    const { estoque } = await consultarEstoque(numero)
    if (consulta !== ultimaConsulta) return
    produto.value = { codigo: numero, estoque }
    registrar({ codigo: numero, estoque })
  }
  catch (erro) {
    if (consulta === ultimaConsulta) tratarFalha(erro, numero)
  }
  finally {
    if (consulta === ultimaConsulta) ocupado.value = false
  }
}

async function repor() {
  if (!produto.value) return
  limparMensagens()
  const resultado = validarQuantidade(quantidade.value)
  if ('erro' in resultado) {
    erroQuantidade.value = resultado.erro
    return
  }
  const { codigo: numero, estoque: antes } = produto.value
  ocupado.value = true
  try {
    const { estoque: depois } = await darEntrada(numero, resultado.valor)
    produto.value = { codigo: numero, estoque: depois }
    registrar({ codigo: numero, estoque: depois })
    sucesso.value = { codigo: numero, antes, depois }
    quantidade.value = ''
  }
  catch (erro) {
    const problema = erro as ProblemDetail
    if (problema.status === 400) {
      // com a quantidade já validada aqui, um 400 sem erros por campo é o estouro do limite de estoque
      erroQuantidade.value = { chave: problema.erros?.length ? 'paginas.estoque.erros.quantidadeInvalida' : 'paginas.estoque.erros.quantidadeLimite' }
    }
    else {
      tratarFalha(erro, numero)
    }
  }
  finally {
    ocupado.value = false
  }
}

onMounted(() => {
  const inicial = route.query.codigo
  if (typeof inicial === 'string' && inicial) {
    codigo.value = inicial
    consultar()
  }
})
</script>

<template>
  <div class="pagina">
    <KBreadcrumb
      :itens="[
        { texto: t('vendedor.titulo'), to: '/vendedor' },
        { texto: t('paginas.estoque.titulo') },
      ]"
    />
    <h1 class="heading-lg">
      {{ t('paginas.estoque.titulo') }}
    </h1>
    <p class="body">
      {{ t('paginas.estoque.texto') }}
    </p>

    <div class="coluna">
      <KAlert
        v-if="sucesso"
        tipo="sucesso"
      >
        {{ t('paginas.estoque.sucesso', sucesso) }}
      </KAlert>
      <KAlert
        v-if="falha"
        tipo="erro"
      >
        {{ t(falha) }}
      </KAlert>

      <form
        class="painel linha"
        novalidate
        @submit.prevent="consultar"
      >
        <KInput
          v-model="codigo"
          class="campo"
          :label="t('paginas.estoque.codigo')"
          :erro="textoErro(erroCodigo)"
          inputmode="numeric"
          autocomplete="off"
        />
        <KButton
          type="submit"
          class="acao"
          :disabled="ocupado"
        >
          {{ t('paginas.estoque.consultar') }}
        </KButton>
      </form>

      <template v-if="produto">
        <dl class="painel resumo">
          <div>
            <dt class="caption">
              {{ t('paginas.estoque.resumo.codigo') }}
            </dt>
            <dd class="heading-sm">
              {{ produto.codigo }}
            </dd>
          </div>
          <div>
            <dt class="caption">
              {{ t('paginas.estoque.resumo.preco') }}
            </dt>
            <dd class="heading-sm">
              {{ preco === undefined ? t('paginas.estoque.resumo.precoDesconhecido') : formatarPreco(preco, locale) }}
            </dd>
          </div>
          <div>
            <dt class="caption">
              {{ t('paginas.estoque.resumo.estoque') }}
            </dt>
            <dd class="heading-sm">
              {{ t('paginas.estoque.resumo.unidades', produto.estoque) }}
            </dd>
          </div>
          <div>
            <dt class="caption">
              {{ t('paginas.estoque.resumo.situacao') }}
            </dt>
            <dd>
              <KBadge :tipo="selo!">
                {{ rotuloSelo }}
              </KBadge>
            </dd>
          </div>
        </dl>

        <div class="painel sem-padding">
          <div
            class="abas"
            role="tablist"
            :aria-label="t('paginas.estoque.abas')"
            @keydown="teclaAba"
          >
            <button
              v-for="aba in abas"
              :id="`aba-${aba.id}`"
              :key="aba.id"
              type="button"
              role="tab"
              class="aba label"
              :aria-selected="abaAtiva === aba.id"
              :aria-controls="`painel-${aba.id}`"
              :tabindex="abaAtiva === aba.id ? 0 : -1"
              @click="trocarAba(aba.id)"
            >
              {{ aba.rotulo }}
            </button>
          </div>

          <div
            v-if="abaAtiva === 'repor'"
            id="painel-repor"
            role="tabpanel"
            aria-labelledby="aba-repor"
          >
            <form
              class="conteudo-aba linha"
              novalidate
              @submit.prevent="repor"
            >
              <KInput
                v-model="quantidade"
                class="campo"
                :label="t('paginas.estoque.quantidade')"
                :erro="textoErro(erroQuantidade)"
                :placeholder="t('paginas.estoque.quantidadeExemplo')"
                inputmode="numeric"
                autocomplete="off"
              />
              <KButton
                type="submit"
                class="acao"
                :disabled="ocupado"
              >
                {{ t('paginas.estoque.repor') }}
              </KButton>
            </form>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }

.coluna {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-6);
  max-width: 640px;
}

.painel {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  padding: var(--spacing-6);
}

.sem-padding { padding: 0; }

.linha {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-4);
}

.campo { flex: 1; }

/* alinha o botão ao campo, pulando a altura do rótulo */
.acao { margin-top: calc(20px + var(--spacing-2)); }

.resumo {
  display: grid;
  grid-template-columns: repeat(4, auto);
  justify-content: space-between;
  gap: var(--spacing-4);
  margin: 0;
}

.resumo dt { color: var(--color-text-secondary); }
.resumo dd { margin: 0; }

.abas {
  display: flex;
  gap: var(--spacing-8);
  padding: 0 var(--spacing-6);
  border-bottom: 1px solid var(--color-border-default);
}

.aba {
  padding: var(--spacing-4) 0;
  border: 0;
  border-bottom: 3px solid transparent;
  margin-bottom: -1px;
  background: none;
  color: var(--color-text-secondary);
  font-size: 16px;
}

.aba[aria-selected='true'] {
  color: var(--color-text-brand);
  border-bottom-color: var(--color-bg-brand);
}

.conteudo-aba { padding: var(--spacing-6); }

@media (max-width: 640px) {
  .linha { flex-direction: column; align-items: stretch; }
  .acao { margin-top: 0; }
  .resumo { grid-template-columns: repeat(2, 1fr); }
}
</style>
