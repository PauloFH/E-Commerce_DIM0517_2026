import { listar, registrar, lerProdutosDoStorage } from '~/utils/produtosConhecidos'
import type { MapaProdutos } from '~/utils/produtosConhecidos'

export function useProdutosConhecidos() {
  const mapa = useState<MapaProdutos>('produtos-conhecidos', () => ({}))
  persistirNoCliente('kompraki:produtos', mapa, lerProdutosDoStorage)

  return {
    produtos: computed(() => listar(mapa.value)),
    registrar: (parcial: { codigo: number, preco?: number, estoque?: number }) => {
      mapa.value = registrar(mapa.value, parcial)
    }
  }
}
