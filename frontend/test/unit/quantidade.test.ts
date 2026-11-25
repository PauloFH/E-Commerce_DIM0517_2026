import { describe, expect, it } from 'vitest'
import { limitarQuantidade } from '../../app/utils/quantidade'

describe('limitarQuantidade', () => {
  it('mantém valores dentro do intervalo', () => {
    expect(limitarQuantidade(5, 1, 15, 1)).toBe(5)
    expect(limitarQuantidade('7', 1, 15, 1)).toBe(7)
  })

  it('limita ao máximo e ao mínimo', () => {
    expect(limitarQuantidade(99, 1, 15, 1)).toBe(15)
    expect(limitarQuantidade('0', 1, 15, 3)).toBe(1)
  })

  it('volta ao valor anterior quando a entrada não é inteiro', () => {
    expect(limitarQuantidade('', 1, 15, 4)).toBe(4)
    expect(limitarQuantidade('abc', 1, 15, 4)).toBe(4)
    expect(limitarQuantidade('2,5', 1, 15, 4)).toBe(4)
    expect(limitarQuantidade('-3', 1, 15, 4)).toBe(4)
  })

  it('aceita min igual ao max (esgotado)', () => {
    expect(limitarQuantidade(3, 0, 0, 0)).toBe(0)
  })
})
