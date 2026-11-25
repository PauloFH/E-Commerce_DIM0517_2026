import { describe, expect, it } from 'vitest'
import { validarQuantidade } from '../../app/utils/formularioEstoque'

describe('validarQuantidade', () => {
  it('aceita inteiro positivo, ignorando espaços', () => {
    expect(validarQuantidade(' 10 ')).toEqual({ valor: 10 })
  })

  it.each(['', '0', '-3', '1.5', '1,5', 'abc', '1e3'])('recusa "%s"', (texto) => {
    expect(validarQuantidade(texto)).toEqual({ erro: { chave: 'paginas.estoque.erros.quantidadeInvalida' } })
  })

  it('recusa acima do limite de estoque', () => {
    expect(validarQuantidade('2147483648')).toEqual({ erro: { chave: 'paginas.estoque.erros.quantidadeLimite' } })
    expect(validarQuantidade('2147483647')).toEqual({ valor: 2147483647 })
  })
})
