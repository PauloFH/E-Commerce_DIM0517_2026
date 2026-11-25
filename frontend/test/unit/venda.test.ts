import { describe, expect, it } from 'vitest'
import { classificarErroVenda, estimarTotal, lerPedidoDoStorage } from '../../app/utils/venda'

const precos: Record<number, number> = { 123: 59.9, 456: 69.9, 789: 0.1 }
const preco = (codigo: number) => precos[codigo]

describe('estimarTotal', () => {
  it('soma só os itens com preço conhecido e conta os demais', () => {
    expect(estimarTotal([{ codigo: 123, quantidade: 2 }, { codigo: 999, quantidade: 1 }], preco)).toEqual({ total: 119.8, semPreco: 1 })
  })

  it('sem nenhum preço conhecido não há total', () => {
    expect(estimarTotal([{ codigo: 999, quantidade: 1 }], preco)).toEqual({ total: undefined, semPreco: 1 })
    expect(estimarTotal([], preco)).toEqual({ total: undefined, semPreco: 0 })
  })

  it('não acumula erro de ponto flutuante', () => {
    expect(estimarTotal([{ codigo: 789, quantidade: 3 }], preco).total).toBe(0.3)
  })
})

describe('classificarErroVenda', () => {
  const problema = (extra: object) => ({ status: 409, title: 't', detail: 'd', ...extra })

  it('estoque insuficiente lê [disponível, código]', () => {
    expect(classificarErroVenda(problema({ codigoErro: 'produto.estoque.insuficiente', argumentos: [3, 456] })))
      .toEqual({ tipo: 'estoque', codigo: 456, disponivel: 3 })
    expect(classificarErroVenda(problema({ codigoErro: 'produto.estoque.insuficiente', argumentos: ['0', '456'] })))
      .toEqual({ tipo: 'estoque', codigo: 456, disponivel: 0 })
  })

  it('produto inexistente lê o código', () => {
    expect(classificarErroVenda(problema({ status: 404, codigoErro: 'produto.nao.encontrado', argumentos: [999] })))
      .toEqual({ tipo: 'inexistente', codigo: 999 })
  })

  it('status 0 é rede; validação, argumentos ruins e códigos desconhecidos são genéricos', () => {
    expect(classificarErroVenda(problema({ status: 0 }))).toEqual({ tipo: 'rede' })
    expect(classificarErroVenda(problema({ status: 400, erros: [{ campo: 'itens[1].quantidade', chave: 'x' }] }))).toEqual({ tipo: 'generico' })
    expect(classificarErroVenda(problema({ codigoErro: 'produto.estoque.insuficiente', argumentos: ['x'] }))).toEqual({ tipo: 'generico' })
    expect(classificarErroVenda(problema({ codigoErro: 'produto.nao.encontrado' }))).toEqual({ tipo: 'generico' })
    expect(classificarErroVenda(problema({ status: 500 }))).toEqual({ tipo: 'generico' })
  })
})

describe('lerPedidoDoStorage', () => {
  const venda = { total: 189.7, itens: [{ codigo: 123, quantidade: 2, subtotal: 119.8, estoque: 13 }] }

  it('lê um pedido válido', () => {
    expect(lerPedidoDoStorage(JSON.stringify(venda))).toEqual(venda)
  })

  it('lixo, formato inválido ou vazio viram sem pedido', () => {
    for (const json of [null, '', '{', '[]', '{"total":1}', '{"total":1,"itens":[]}', '{"total":1,"itens":[{"codigo":1}]}']) {
      expect(lerPedidoDoStorage(json)).toBeUndefined()
    }
  })
})
