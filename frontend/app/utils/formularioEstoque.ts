// erro como chave i18n + parâmetros, para o texto acompanhar a troca de idioma
export interface ErroCampo {
  chave: string
  params?: Record<string, unknown>
}

const LIMITE_ESTOQUE = 2_147_483_647

function lerInteiroPositivo(texto: string): number | null {
  const limpo = texto.trim()
  if (!/^\d+$/.test(limpo)) return null
  const numero = Number(limpo)
  return numero > 0 ? numero : null
}

export function validarQuantidade(texto: string): { valor: number } | { erro: ErroCampo } {
  const valor = lerInteiroPositivo(texto)
  if (valor === null) return { erro: { chave: 'paginas.estoque.erros.quantidadeInvalida' } }
  if (valor > LIMITE_ESTOQUE) return { erro: { chave: 'paginas.estoque.erros.quantidadeLimite' } }
  return { valor }
}
