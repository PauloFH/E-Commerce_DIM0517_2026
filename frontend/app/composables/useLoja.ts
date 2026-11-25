import { normalizarErro } from '~/utils/api'
import type { EstoqueProduto, ItemVenda, Produto, Venda } from '~/utils/api'

// Cliente da API da loja (via proxy /loja/**). Erros saem sempre como ProblemDetail.
export function useLoja() {
  const { locale, t } = useI18n()

  async function chamar<T>(url: string, metodo: 'GET' | 'POST' | 'PUT', corpo?: object): Promise<T> {
    try {
      return await $fetch<T>(url, {
        method: metodo,
        timeout: 10_000, // sem isso uma API travada deixa a chamada pendente para sempre; vira erro de rede
        body: corpo,
        headers: { 'Accept-Language': locale.value },
      })
    }
    catch (erro) {
      throw normalizarErro(erro, t('erros.rede'), t('erros.generico'))
    }
  }

  return {
    cadastrarProduto: (codigo: number, preco: number) =>
      chamar<Produto>('/loja/produto', 'POST', { codigo, preco }),
    consultarEstoque: (codigo: number) =>
      chamar<EstoqueProduto>(`/loja/produto/${codigo}/estoque`, 'GET'),
    darEntrada: (codigo: number, quantidade: number) =>
      chamar<EstoqueProduto>(`/loja/produto/${codigo}/entrada`, 'PUT', { quantidade }),
    darSaida: (codigo: number, quantidade: number) =>
      chamar<EstoqueProduto>(`/loja/produto/${codigo}/saida`, 'PUT', { quantidade }),
    registrarVenda: (itens: ItemVenda[]) =>
      chamar<Venda>('/loja/venda', 'POST', { itens }),
  }
}
