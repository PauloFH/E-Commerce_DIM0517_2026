<script setup lang="ts">
const { t, locale } = useI18n()
const { pedido, carregar } = useUltimoPedido()

useHead({ title: () => `${t(pedido.value ? 'paginas.pedido.titulo' : 'paginas.pedido.vazioTitulo')} · kompraki` })

// o pedido guardado só é lido no cliente: até lá nem o resumo nem o "sem pedido" aparecem
const iniciouNoCliente = import.meta.client && !useNuxtApp().isHydrating
if (iniciouNoCliente) carregar()
const pronto = ref(iniciouNoCliente)
const titulo = ref<HTMLElement>()

onMounted(async () => {
  carregar()
  pronto.value = true
  await nextTick()
  titulo.value?.focus()
})
</script>

<template>
  <div class="pagina">
    <KBreadcrumb :itens="[{ texto: t('nav.inicio'), to: '/' }, { texto: t(pedido ? 'paginas.pedido.titulo' : 'paginas.pedido.migalhaVazio') }]" />

    <template v-if="pronto">
      <template v-if="pedido">
        <div class="confirmacao">
          <div
            class="selo"
            aria-hidden="true"
          >
            <KIcon
              nome="sucesso"
              :tamanho="36"
            />
          </div>
          <div class="cabecalho">
            <h1
              ref="titulo"
              class="heading-lg"
              tabindex="-1"
            >
              {{ t('paginas.pedido.titulo') }}
            </h1>
            <p class="body secundario">
              {{ t('paginas.pedido.texto') }}
            </p>
          </div>
        </div>

        <div class="painel">
          <table class="tabela">
            <caption class="visually-hidden">
              {{ t('paginas.pedido.tabela') }}
            </caption>
            <thead>
              <tr class="label">
                <th scope="col">
                  {{ t('paginas.pedido.produto') }}
                </th>
                <th
                  scope="col"
                  class="col-quantidade"
                >
                  {{ t('paginas.pedido.quantidade') }}
                </th>
                <th
                  scope="col"
                  class="col-subtotal num"
                >
                  {{ t('paginas.pedido.subtotal') }}
                </th>
                <th
                  scope="col"
                  class="col-estoque num"
                >
                  {{ t('paginas.pedido.estoque') }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in pedido.itens"
                :key="item.codigo"
              >
                <th
                  scope="row"
                  class="produto"
                >
                  <span
                    class="imagem"
                    aria-hidden="true"
                  >
                    <KIcon
                      nome="estoque"
                      :tamanho="24"
                    />
                  </span>
                  <span class="nome">
                    <span class="heading-sm">{{ t('produto.titulo', { codigo: item.codigo }) }}</span>
                    <span class="caption secundario">{{ t('paginas.carrinho.codigo', { codigo: item.codigo }) }}</span>
                  </span>
                </th>
                <td
                  class="quantidade"
                  :data-rotulo="t('paginas.pedido.quantidade')"
                >
                  {{ item.quantidade }}
                </td>
                <td class="subtotal num">
                  {{ formatarPreco(item.subtotal, locale) }}
                </td>
                <td
                  class="estoque num secundario"
                  :data-rotulo="t('paginas.pedido.estoque')"
                >
                  {{ t('paginas.pedido.unidades', item.estoque) }}
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <th
                  scope="row"
                  colspan="2"
                  class="heading-sm"
                >
                  {{ t('paginas.pedido.total') }}
                </th>
                <td
                  colspan="2"
                  class="total num"
                >
                  {{ formatarPreco(pedido.total, locale) }}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>

        <KButton
          class="continuar"
          to="/"
        >
          {{ t('paginas.pedido.continuar') }}
        </KButton>
      </template>

      <section
        v-else
        class="painel vazio"
      >
        <div
          class="selo claro"
          aria-hidden="true"
        >
          <KIcon
            nome="recibo"
            :tamanho="32"
          />
        </div>
        <h1
          ref="titulo"
          class="heading-sm"
          tabindex="-1"
        >
          {{ t('paginas.pedido.vazioTitulo') }}
        </h1>
        <p class="body secundario">
          {{ t('paginas.pedido.vazioTexto') }}
        </p>
        <KButton to="/">
          {{ t('paginas.pedido.vazioBotao') }}
        </KButton>
      </section>
    </template>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-6); padding-bottom: var(--spacing-12); }
.secundario { color: var(--color-text-secondary); }

/* o foco programático no título só serve ao leitor de tela */
h1:focus-visible { outline: none; }

.confirmacao { display: flex; align-items: center; gap: var(--spacing-4); }
.cabecalho { display: flex; flex-direction: column; gap: var(--spacing-2); }

.selo {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background: var(--color-bg-success);
  color: var(--color-text-success);
}

.selo.claro { width: 68px; height: 68px; background: var(--color-bg-info); color: var(--color-text-brand); }

.painel {
  max-width: 880px;
  overflow: hidden;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
}

.tabela { width: 100%; border-collapse: collapse; }
.tabela th,
.tabela td { padding: var(--spacing-4) var(--spacing-5); text-align: left; }
.tabela td,
.tabela tbody th { font-weight: 400; }

thead tr { background: var(--color-bg-info); color: var(--color-text-secondary); }
thead th { padding-block: var(--spacing-3); font-weight: 600; }
tbody tr { border-top: 1px solid var(--color-border-default); }
tfoot tr { border-top: 1px solid var(--color-border-default); background: var(--color-bg-info); }

.col-quantidade { width: 120px; }
.col-subtotal { width: 140px; }
.col-estoque { width: 160px; }
.num { text-align: right !important; }

.produto { display: flex; align-items: center; gap: var(--spacing-3); }
.imagem {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-info);
  color: var(--color-text-brand);
}
.nome { display: flex; flex-direction: column; gap: 2px; }
.subtotal { font-weight: 600 !important; }
.total { font-size: 30px; line-height: 36px; font-weight: 800 !important; }

.vazio {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-3);
  max-width: none;
  padding: var(--spacing-12) var(--spacing-4);
  text-align: center;
}

.continuar { align-self: flex-start; }

@media (max-width: 767px) {
  .confirmacao { flex-direction: column; align-items: flex-start; }

  thead { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }

  tbody tr {
    display: grid;
    grid-template-columns: 1fr auto;
    grid-template-areas: 'produto subtotal' 'quantidade estoque';
    gap: var(--spacing-2) var(--spacing-4);
    padding: var(--spacing-4);
  }

  .tabela tbody th,
  .tabela tbody td { padding: 0; }
  .imagem { display: none; }
  .produto { grid-area: produto; align-items: flex-start; }
  .subtotal { grid-area: subtotal; }
  .quantidade { grid-area: quantidade; }
  .estoque { grid-area: estoque; }

  .quantidade::before,
  .estoque::before {
    display: block;
    content: attr(data-rotulo);
    font-size: 13px;
    line-height: 18px;
    font-weight: 500;
    color: var(--color-text-secondary);
  }

  tfoot tr { display: flex; align-items: center; justify-content: space-between; padding: var(--spacing-4); }
  .tabela tfoot th,
  .tabela tfoot td { padding: 0; }

  .continuar { align-self: stretch; }
}
</style>
