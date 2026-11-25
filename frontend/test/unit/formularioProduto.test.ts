import { describe, expect, it } from 'vitest'
import { centavosDe, validarCodigo, validarPreco } from '../../app/utils/formularioProduto'

describe('validarCodigo', () => {
  it('aceita inteiro positivo até o limite do Integer', () => {
    expect(validarCodigo('123')).toBeNull()
    expect(validarCodigo(' 7 ')).toBeNull()
    expect(validarCodigo('2147483647')).toBeNull()
  })

  it('pede o código quando vazio', () => {
    expect(validarCodigo('')).toBe('obrigatorio')
    expect(validarCodigo('   ')).toBe('obrigatorio')
  })

  it('recusa zero, negativo, decimal, texto e acima do limite', () => {
    for (const texto of ['0', '-1', '2.5', '2,5', 'abc', '012', '2147483648']) {
      expect(validarCodigo(texto), texto).toBe('invalido')
    }
  })
})

describe('centavosDe', () => {
  it('lê os dígitos como centavos, ignorando a máscara', () => {
    expect(centavosDe('5990')).toBe(5990)
    expect(centavosDe('R$ 59,90')).toBe(5990)
    expect(centavosDe('R$1,234.50')).toBe(123450)
    expect(centavosDe('0,05')).toBe(5)
  })

  it('vazio ou sem dígitos é zero', () => {
    expect(centavosDe('')).toBe(0)
    expect(centavosDe('R$ ')).toBe(0)
  })

  it('não reduz um valor acima do limite', () => {
    expect(centavosDe('99999999999999')).toBe(99_999_999_999_999)
  })
})

describe('validarPreco', () => {
  it('exige preço maior que zero', () => {
    expect(validarPreco(1)).toBeNull()
    expect(validarPreco(0)).toBe('obrigatorio')
  })

  it('acusa preço acima de R$ 9.999.999.999,99', () => {
    expect(validarPreco(999_999_999_999)).toBeNull()
    expect(validarPreco(1_000_000_000_000)).toBe('acimaDoLimite')
  })
})
