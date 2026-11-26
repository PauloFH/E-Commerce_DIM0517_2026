import type { Venda } from '~/utils/api'
import { lerPedidoDoStorage } from '~/utils/venda'

const CHAVE = 'kompraki:ultimoPedido'

// o último pedido fica em sessionStorage para a confirmação sobreviver a um recarregamento
export function useUltimoPedido() {
  const pedido = useState<Venda | undefined>('ultimo-pedido', () => undefined)

  return {
    pedido,
    guardar: (venda: Venda) => {
      pedido.value = venda
      try {
        sessionStorage.setItem(CHAVE, JSON.stringify(venda))
      }
      catch { /* storage cheio ou bloqueado: a confirmação segue só em memória */ }
    },
    carregar: () => {
      if (pedido.value) return
      try {
        pedido.value = lerPedidoDoStorage(sessionStorage.getItem(CHAVE))
      }
      catch { /* storage indisponível: sem pedido */ }
    },
  }
}
