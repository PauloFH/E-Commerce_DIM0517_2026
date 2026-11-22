import { describe, expect, it, vi } from 'vitest'
import { lerCarrinhoDoStorage } from '../../app/utils/carrinho'
import { lerProdutosDoStorage } from '../../app/utils/produtosConhecidos'

// "Recarregar" = um estado novo começando com o que ficou salvo no localStorage.
// Cada teste usa uma chave própria para não depender do estado global dos composables.
describe('persistência ao recarregar', () => {
  it('carrinho: recupera o que estava salvo e grava as mudanças', async () => {
    localStorage.setItem('teste:carrinho', '[{"codigo":7,"quantidade":3}]')
    const itens = ref<{ codigo: number, quantidade: number }[]>([])
    persistirNoCliente('teste:carrinho', itens, lerCarrinhoDoStorage)

    await vi.waitFor(() => expect(itens.value).toEqual([{ codigo: 7, quantidade: 3 }]))

    itens.value = [...itens.value, { codigo: 9, quantidade: 1 }]
    await vi.waitFor(() => expect(lerCarrinhoDoStorage(localStorage.getItem('teste:carrinho')))
      .toEqual([{ codigo: 7, quantidade: 3 }, { codigo: 9, quantidade: 1 }]))
  })

  it('produtos conhecidos: recupera o que estava salvo', async () => {
    localStorage.setItem('teste:produtos', '{"5":{"codigo":5,"preco":10,"vistoEm":1}}')
    const mapa = ref({})
    persistirNoCliente('teste:produtos', mapa, lerProdutosDoStorage)

    await vi.waitFor(() => expect(mapa.value).toEqual({ 5: { codigo: 5, preco: 10, vistoEm: 1 } }))
  })

  it('useCarrinho e useProdutosConhecidos gravam nas chaves que são lidas ao recarregar', async () => {
    const carrinho = useCarrinho()
    const produtos = useProdutosConhecidos()
    // callbacks do onNuxtReady rodam em ordem: quando este roda, a leitura e o watch já estão ligados
    await new Promise<void>(resolve => onNuxtReady(() => resolve()))

    carrinho.esvaziar()
    carrinho.adicionar(4, 2)
    produtos.registrar({ codigo: 4, preco: 25 })

    await vi.waitFor(() => {
      expect(lerCarrinhoDoStorage(localStorage.getItem('kompraki:carrinho'))).toEqual([{ codigo: 4, quantidade: 2 }])
      expect(lerProdutosDoStorage(localStorage.getItem('kompraki:produtos'))[4]).toMatchObject({ codigo: 4, preco: 25 })
    })

    carrinho.esvaziar()
    await vi.waitFor(() => expect(localStorage.getItem('kompraki:carrinho')).toBe('[]'))
  })
})
