import { adicionar, alterarQuantidade, remover, totalItens, lerCarrinhoDoStorage } from '~/utils/carrinho'
import type { ItemCarrinho } from '~/utils/carrinho'

export function useCarrinho() {
  const itens = useState<ItemCarrinho[]>('carrinho', () => [])
  persistirNoCliente('kompraki:carrinho', itens, lerCarrinhoDoStorage)

  return {
    itens,
    total: computed(() => totalItens(itens.value)),
    adicionar: (codigo: number, quantidade = 1) => { itens.value = adicionar(itens.value, codigo, quantidade) },
    alterarQuantidade: (codigo: number, quantidade: number) => { itens.value = alterarQuantidade(itens.value, codigo, quantidade) },
    remover: (codigo: number) => { itens.value = remover(itens.value, codigo) },
    esvaziar: () => { itens.value = [] },
  }
}
