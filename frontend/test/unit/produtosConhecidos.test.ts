import { describe, expect, it } from 'vitest'
import { listar, lerProdutosDoStorage, registrar } from '../../app/utils/produtosConhecidos'

describe('produtosConhecidos', () => {
  it('mescla sem apagar campos conhecidos', () => {
    let mapa = registrar({}, { codigo: 5, preco: 10 }, 100)
    mapa = registrar(mapa, { codigo: 5, estoque: 3 }, 200)
    expect(mapa[5]).toEqual({ codigo: 5, preco: 10, estoque: 3, vistoEm: 200 })
  })

  it('lista do mais recente para o mais antigo', () => {
    let mapa = registrar({}, { codigo: 1 }, 100)
    mapa = registrar(mapa, { codigo: 2 }, 300)
    mapa = registrar(mapa, { codigo: 3 }, 200)
    expect(listar(mapa).map(p => p.codigo)).toEqual([2, 3, 1])
  })

  it('lê o storage e tolera lixo', () => {
    const json = JSON.stringify({ 1: { codigo: 1, preco: 9.9, vistoEm: 10 }, 2: { codigo: 'x' }, 3: null })
    expect(lerProdutosDoStorage(json)).toEqual({ 1: { codigo: 1, preco: 9.9, vistoEm: 10 } })
    for (const lixo of [null, undefined, '', 'não é json', '[]', '42', 'null']) {
      expect(lerProdutosDoStorage(lixo)).toEqual({})
    }
  })
})
