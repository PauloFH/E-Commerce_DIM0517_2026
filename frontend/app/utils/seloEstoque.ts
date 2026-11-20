export type SeloEstoque = 'em-estoque' | 'ultimas' | 'esgotado'

// null quando o estoque ainda não é conhecido
export function seloEstoque(estoque?: number): SeloEstoque | null {
  if (estoque === undefined || estoque === null || Number.isNaN(estoque)) return null
  if (estoque <= 0) return 'esgotado'
  return estoque <= 3 ? 'ultimas' : 'em-estoque'
}
