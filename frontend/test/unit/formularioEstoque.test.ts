import { describe, expect, it } from 'vitest'
import { validarCodigo, validarQuantidade } from '../../app/utils/formularioEstoque'

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

describe('validarCodigo', () => {
  it('aceita inteiro positivo', () => {
    expect(validarCodigo('123')).toEqual({ valor: 123 })
  })

  it('vazio tem mensagem própria', () => {
    expect(validarCodigo('  ')).toEqual({ erro: { chave: 'paginas.estoque.erros.codigoVazio' } })
  })

  it.each(['0', '-1', '12.5', 'x1', '2147483648'])('recusa "%s"', (texto) => {
    expect(validarCodigo(texto)).toEqual({ erro: { chave: 'paginas.estoque.erros.codigoInvalido' } })
  })
})
