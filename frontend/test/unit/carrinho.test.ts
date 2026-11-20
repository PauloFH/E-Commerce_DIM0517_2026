import { describe, expect, it } from 'vitest'
import { adicionar, alterarQuantidade, remover, totalItens, lerCarrinhoDoStorage } from '../../app/utils/carrinho'

describe('carrinho', () => {
  it('adiciona e soma quando o código se repete', () => {
    let itens = adicionar([], 7)
    itens = adicionar(itens, 7, 2)
    itens = adicionar(itens, 9)
    expect(itens).toEqual([{ codigo: 7, quantidade: 3 }, { codigo: 9, quantidade: 1 }])
    expect(totalItens(itens)).toBe(4)
  })

  it('ignora código ou quantidade inválidos', () => {
    expect(adicionar([], 0)).toEqual([])
    expect(adicionar([], 1.5)).toEqual([])
    expect(adicionar([], 3, 0)).toEqual([])
  })

  it('altera a quantidade e remove quando <= 0', () => {
    const itens = [{ codigo: 1, quantidade: 2 }, { codigo: 2, quantidade: 1 }]
    expect(alterarQuantidade(itens, 1, 5)[0]).toEqual({ codigo: 1, quantidade: 5 })
    expect(alterarQuantidade(itens, 1, 0)).toEqual([{ codigo: 2, quantidade: 1 }])
    expect(alterarQuantidade(itens, 2, -3)).toEqual([{ codigo: 1, quantidade: 2 }])
  })

  it('remove um item', () => {
    expect(remover([{ codigo: 1, quantidade: 2 }], 1)).toEqual([])
  })

  it('lê o storage e tolera lixo', () => {
    expect(lerCarrinhoDoStorage('[{"codigo":4,"quantidade":2}]')).toEqual([{ codigo: 4, quantidade: 2 }])
    expect(lerCarrinhoDoStorage('[{"codigo":4,"quantidade":2},{"codigo":"x"},null,5]')).toEqual([{ codigo: 4, quantidade: 2 }])
    for (const lixo of [null, undefined, '', 'não é json', '{}', '"texto"', '42', 'null']) {
      expect(lerCarrinhoDoStorage(lixo)).toEqual([])
    }
  })
})
