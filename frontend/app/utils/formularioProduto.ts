// o código vira Integer no backend
const MAIOR_CODIGO = 2_147_483_647
// NUMERIC(12,2) da tabela de produtos
const MAIOR_CENTAVOS = 999_999_999_999

export type ErroCodigo = 'obrigatorio' | 'invalido'
export type ErroPreco = 'obrigatorio'

export function validarCodigo(texto: string): ErroCodigo | null {
  const limpo = texto.trim()
  if (!limpo) return 'obrigatorio'
  return /^[1-9]\d*$/.test(limpo) && Number(limpo) <= MAIOR_CODIGO ? null : 'invalido'
}

// máscara de moeda: os dígitos entram pela direita (5990 → 59,90)
export function centavosDe(texto: string): number {
  const digitos = texto.replace(/\D/g, '').replace(/^0+/, '')
  return digitos ? Math.min(Number(digitos), MAIOR_CENTAVOS) : 0
}

export function validarPreco(centavos: number): ErroPreco | null {
  return centavos > 0 ? null : 'obrigatorio'
}
