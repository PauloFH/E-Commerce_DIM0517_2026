<script setup lang="ts">
definePageMeta({ layout: 'vendedor' })

const { t, locale } = useI18n()
const { produtos } = useProdutosConhecidos()

useHead({ title: () => `${t('painelProdutos.seo')} · kompraki` })

const rotuloSelo = (estoque?: number) => {
  const selo = seloEstoque(estoque)
  if (selo === 'em-estoque') return t('selo.emEstoque')
  if (selo === 'ultimas') return t('selo.ultimas', estoque!)
  return t('selo.esgotado')
}
</script>

<template>
  <div class="pagina">
    <KBreadcrumb
      :itens="[
        { texto: t('vendedor.titulo'), to: '/vendedor' },
        { texto: t('painelProdutos.titulo') },
      ]"
    />

    <div class="titulo-linha">
      <h1 class="heading-lg">
        {{ t('painelProdutos.titulo') }}
      </h1>
      <div class="acoes-topo">
        <KButton
          variante="secundario"
          to="/vendedor/estoque"
        >
          {{ t('vendedor.gerenciarEstoque') }}
        </KButton>
        <KButton
          variante="primario"
          to="/vendedor/produtos/novo"
        >
          {{ t('vendedor.novoProduto') }}
        </KButton>
      </div>
    </div>

    <KAlert tipo="info">
      {{ t('painelProdutos.aviso') }}
    </KAlert>

    <div class="tabela-caixa">
      <table>
        <caption class="visually-hidden">
          {{ t('painelProdutos.tabela') }}
        </caption>
        <thead>
          <tr>
            <th
              scope="col"
              class="label"
            >
              {{ t('painelProdutos.codigo') }}
            </th>
            <th
              scope="col"
              class="label"
            >
              {{ t('painelProdutos.preco') }}
            </th>
            <th
              scope="col"
              class="label"
            >
              {{ t('painelProdutos.estoque') }}
            </th>
            <th
              scope="col"
              class="label"
            >
              {{ t('painelProdutos.situacao') }}
            </th>
            <th
              scope="col"
              class="label"
            >
              {{ t('painelProdutos.acoes') }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!produtos.length">
            <td
              colspan="5"
              class="vazio body"
            >
              {{ t('painelProdutos.vazio') }}
            </td>
          </tr>
          <tr
            v-for="p in produtos"
            :key="p.codigo"
          >
            <th
              scope="row"
              class="body"
            >
              {{ p.codigo }}
            </th>
            <td class="body numero">
              {{ p.preco !== undefined ? formatarPreco(p.preco, locale) : t('painelProdutos.desconhecido') }}
            </td>
            <td class="body numero">
              {{ p.estoque ?? t('painelProdutos.desconhecido') }}
            </td>
            <td>
              <KBadge
                v-if="seloEstoque(p.estoque)"
                :tipo="seloEstoque(p.estoque)!"
              >
                {{ rotuloSelo(p.estoque) }}
              </KBadge>
              <span
                v-else
                class="caption"
              >{{ t('painelProdutos.situacaoDesconhecida') }}</span>
            </td>
            <td class="acoes">
              <KButton
                variante="texto"
                :to="`/vendedor/estoque?codigo=${p.codigo}`"
                :aria-label="t('painelProdutos.reporProduto', { codigo: p.codigo })"
              >
                {{ t('painelProdutos.repor') }}
              </KButton>
              <KButton
                variante="texto"
                :to="`/vendedor/estoque?codigo=${p.codigo}`"
                :aria-label="t('painelProdutos.baixaProduto', { codigo: p.codigo })"
              >
                {{ t('painelProdutos.baixa') }}
              </KButton>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }

.titulo-linha {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-4);
}

.acoes-topo { display: flex; flex-wrap: wrap; gap: var(--spacing-3); }

.tabela-caixa {
  overflow-x: auto;
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

table { width: 100%; border-collapse: collapse; }

th,
td {
  padding: var(--spacing-3) var(--spacing-4);
  border-bottom: 1px solid var(--color-border-default);
  text-align: left;
  vertical-align: middle;
}

tbody tr:last-child > * { border-bottom: 0; }
thead th { background: var(--color-bg-page); }
.numero { font-variant-numeric: tabular-nums; }
.acoes { white-space: nowrap; }
.vazio { padding: var(--spacing-8) var(--spacing-4); text-align: center; color: var(--color-text-secondary); }
</style>
