<script setup lang="ts">
import type { ProblemDetail, Venda } from '~/utils/api'
import { classificarErroVenda, estimarTotal } from '~/utils/venda'
import type { ErroVenda } from '~/utils/venda'

type ErroLinha = Extract<ErroVenda, { codigo: number }>

const { t, locale } = useI18n()
const { itens, total: unidades, alterarQuantidade, remover: removerDoCarrinho, esvaziar } = useCarrinho()
const { produtos, registrar } = useProdutosConhecidos()
const { registrarVenda } = useLoja()
const { guardar } = useUltimoPedido()

useHead({ title: () => `${t('paginas.carrinho.titulo')} · kompraki` })

// o carrinho vem do localStorage depois da hidratação: até lá nem o "vazio" nem os itens aparecem
const pronto = ref(import.meta.client && !useNuxtApp().isHydrating)
onNuxtReady(() => {
  pronto.value = true
})

const enviando = ref(false)
const erroGeral = ref<ErroVenda>()
const errosLinha = ref<Record<number, ErroLinha>>({})
const alerta = ref<{ $el: HTMLElement }>()

const precoDe = (codigo: number) => produtos.value.find(p => p.codigo === codigo)?.preco
const estimativa = computed(() => estimarTotal(itens.value, precoDe))

function textoDoErro(erro: ErroVenda, linha = false) {
  if (erro.tipo === 'estoque') {
    return t(linha ? 'paginas.carrinho.erros.estoqueLinha' : 'paginas.carrinho.erros.estoque', { codigo: erro.codigo, n: erro.disponivel })
  }
  if (erro.tipo === 'inexistente') return t(linha ? 'paginas.carrinho.erros.naoExisteLinha' : 'paginas.carrinho.erros.naoExiste', { codigo: erro.codigo })
  return t(erro.tipo === 'rede' ? 'erros.rede' : 'erros.generico')
}

function subtotalDe(codigo: number, quantidade: number) {
  const preco = precoDe(codigo)
  return preco === undefined
    ? t('produto.precoFechamento')
    : t('paginas.carrinho.estimado', { valor: formatarPreco(preco * quantidade, locale.value) })
}

function limparLinha(codigo: number) {
  const { [codigo]: _, ...resto } = errosLinha.value
  errosLinha.value = resto
  const geral = erroGeral.value
  if (geral && 'codigo' in geral && geral.codigo === codigo) erroGeral.value = undefined
}

function alterar(codigo: number, quantidade: number) {
  limparLinha(codigo)
  alterarQuantidade(codigo, quantidade)
}

function remover(codigo: number) {
  limparLinha(codigo)
  removerDoCarrinho(codigo)
}

async function finalizar() {
  if (enviando.value || !itens.value.length) return
  erroGeral.value = undefined
  errosLinha.value = {}
  enviando.value = true
  let venda: Venda
  try {
    venda = await registrarVenda(itens.value)
  }
  catch (erro) {
    const classificado = classificarErroVenda(erro as ProblemDetail)
    erroGeral.value = classificado
    if (classificado.tipo === 'estoque' || classificado.tipo === 'inexistente') errosLinha.value = { [classificado.codigo]: classificado }
    enviando.value = false
    await nextTick()
    alerta.value?.$el.focus()
    return
  }
  for (const { codigo, estoque } of venda.itens) registrar({ codigo, estoque })
  guardar(venda)
  esvaziar()
  await navigateTo('/pedido/confirmado')
}
</script>

<template>
  <div class="pagina">
    <KBreadcrumb :itens="[{ texto: t('nav.inicio'), to: '/' }, { texto: t('paginas.carrinho.titulo') }]" />

    <template v-if="pronto">
      <div class="cabecalho">
        <h1 class="heading-lg">
          {{ itens.length ? t('paginas.carrinho.tituloQtd', itens.length) : t('paginas.carrinho.titulo') }}
        </h1>
        <p class="body secundario">
          {{ itens.length ? t('paginas.carrinho.texto') : t('paginas.carrinho.vazioTitulo') }}
        </p>
      </div>

      <KAlert
        v-if="erroGeral"
        ref="alerta"
        tipo="erro"
        tabindex="-1"
      >
        {{ textoDoErro(erroGeral) }}
      </KAlert>

      <section
        v-if="!itens.length"
        class="painel vazio"
      >
        <div
          class="selo"
          aria-hidden="true"
        >
          <KIcon
            nome="carrinho"
            :tamanho="32"
          />
        </div>
        <h2 class="heading-sm">
          {{ t('paginas.carrinho.vazioTitulo') }}
        </h2>
        <p class="body secundario">
          {{ t('paginas.carrinho.vazioTexto') }}
        </p>
        <KButton to="/">
          {{ t('paginas.carrinho.vazioBotao') }}
        </KButton>
      </section>

      <div
        v-else
        class="colunas"
      >
        <ul
          class="lista"
          :aria-label="t('paginas.carrinho.lista')"
        >
          <CarrinhoLinha
            v-for="item in itens"
            :key="item.codigo"
            :codigo="item.codigo"
            :quantidade="item.quantidade"
            :subtotal="subtotalDe(item.codigo, item.quantidade)"
            :erro="errosLinha[item.codigo]"
            :mensagem-erro="errosLinha[item.codigo] && textoDoErro(errosLinha[item.codigo]!, true)"
            :desabilitado="enviando"
            @alterar="alterar(item.codigo, $event)"
            @remover="remover(item.codigo)"
          />
        </ul>

        <aside
          class="painel resumo"
          :aria-label="t('paginas.carrinho.resumo.titulo')"
        >
          <h2 class="heading-sm">
            {{ t('paginas.carrinho.resumo.titulo') }}
          </h2>
          <div class="par">
            <span class="secundario">{{ t('paginas.carrinho.resumo.unidades') }}</span>
            <span class="forte">{{ unidades }}</span>
          </div>
          <div class="divisoria" />
          <div class="par total">
            <span class="forte">{{ t('paginas.carrinho.resumo.total') }}</span>
            <span
              v-if="estimativa.total !== undefined"
              class="heading-md"
            >{{ formatarPreco(estimativa.total, locale) }}</span>
            <span
              v-else
              class="forte"
            >{{ t('produto.precoFechamento') }}</span>
          </div>
          <p
            v-if="estimativa.total !== undefined && estimativa.semPreco"
            class="caption secundario"
          >
            {{ t('paginas.carrinho.resumo.semPreco', estimativa.semPreco) }}
          </p>
          <p class="caption aviso">
            {{ t('paginas.carrinho.resumo.aviso') }}
          </p>
          <KButton
            :carregando="enviando"
            @click="finalizar"
          >
            {{ enviando ? t('paginas.carrinho.resumo.enviando') : t('paginas.carrinho.resumo.finalizar') }}
          </KButton>
          <KButton
            variante="texto"
            to="/"
            :disabled="enviando"
          >
            {{ t('paginas.carrinho.resumo.continuar') }}
          </KButton>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }

.cabecalho { display: flex; flex-direction: column; gap: var(--spacing-2); }
.secundario { color: var(--color-text-secondary); }
.forte { font-weight: 600; }

.colunas {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: var(--spacing-8);
  align-items: start;
  padding-bottom: var(--spacing-12);
}

.lista { display: flex; flex-direction: column; gap: var(--spacing-4); }

.painel {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
}

.resumo { display: flex; flex-direction: column; gap: var(--spacing-4); padding: var(--spacing-6); }
.resumo :deep(.botao) { width: 100%; }

.par { display: flex; align-items: center; justify-content: space-between; gap: var(--spacing-3); }
.divisoria { height: 1px; background: var(--color-border-default); }

.aviso {
  padding: var(--spacing-3);
  border-radius: var(--radius-sm);
  background: var(--color-bg-info);
  color: var(--color-text-info);
}

.vazio {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-12) var(--spacing-4);
  margin-bottom: var(--spacing-12);
  text-align: center;
}

.selo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: var(--radius-full);
  background: var(--color-bg-info);
  color: var(--color-text-brand);
}

@media (max-width: 1279px) {
  .colunas { grid-template-columns: 1fr; gap: var(--spacing-6); }
}
</style>
