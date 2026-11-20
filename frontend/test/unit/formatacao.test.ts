import { describe, expect, it } from 'vitest'
import { formatarPreco } from '../../app/utils/formatarPreco'
import { seloEstoque } from '../../app/utils/seloEstoque'

describe('formatarPreco', () => {
  it('formata em reais no padrão pt-BR', () => {
    expect(formatarPreco(59.9, 'pt-BR')).toBe('R$ 59,90')
  })

  it('formata em reais no padrão en', () => {
    expect(formatarPreco(1234.5, 'en')).toBe('R$1,234.50')
  })
})

describe('seloEstoque', () => {
  it('classifica o estoque', () => {
    expect(seloEstoque(4)).toBe('em-estoque')
    expect(seloEstoque(3)).toBe('ultimas')
    expect(seloEstoque(1)).toBe('ultimas')
    expect(seloEstoque(0)).toBe('esgotado')
    expect(seloEstoque()).toBeNull()
  })
})
