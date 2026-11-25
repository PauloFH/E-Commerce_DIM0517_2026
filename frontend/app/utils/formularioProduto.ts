// o código vira Integer no backend
const MAIOR_CODIGO = 2_147_483_647
// NUMERIC(12,2) da tabela de produtos
const MAIOR_CENTAVOS = 999_999_999_999

export type ErroCodigo = 'obrigatorio' | 'invalido'
export type ErroPreco = 'obrigatorio' | 'acimaDoLimite'

export function validarCodigo(texto: string): ErroCodigo | null {
  const limpo = texto.trim()
  if (!limpo) return 'obrigatorio'
  return /^[1-9]\d*$/.test(limpo) && Number(limpo) <= MAIOR_CODIGO ? null : 'invalido'
}

// máscara de moeda: os dígitos entram pela direita (5990 → 59,90)
export function centavosDe(texto: string): number {
  // 15 dígitos ainda cabem num número exato do JS; acima disso o validador já acusa o limite
  const digitos = texto.replace(/\D/g, '').replace(/^0+/, '').slice(0, 15)
  return digitos ? Number(digitos) : 0
}

export function validarPreco(centavos: number): ErroPreco | null {
  if (centavos <= 0) return 'obrigatorio'
  return centavos > MAIOR_CENTAVOS ? 'acimaDoLimite' : null
}
