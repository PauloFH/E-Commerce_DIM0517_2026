export interface ProblemDetail {
  status: number
  title: string
  detail: string
  codigoErro?: string
  erros?: { campo: string, chave: string }[]
}

export interface Produto {
  codigo: number
  preco: number
  estoque: number
}

export interface EstoqueProduto {
  codigo: number
  estoque: number
}

export interface ItemVenda {
  codigo: number
  quantidade: number
}

export interface Venda {
  total: number
  itens: { codigo: number, quantidade: number, subtotal: number, estoque: number }[]
}

// converte o erro do $fetch em ProblemDetail; sem resposta HTTP é erro de rede (status 0)
export function normalizarErro(erro: unknown, mensagemRede: string, mensagemGenerica: string): ProblemDetail {
  const e = (erro ?? {}) as { response?: unknown, status?: number, statusCode?: number, data?: Record<string, unknown> }
  if (!e.response && !e.status && !e.statusCode) {
    return { status: 0, title: mensagemRede, detail: mensagemRede }
  }
  const dados = e.data && typeof e.data === 'object' ? e.data : {}
  const texto = (v: unknown) => (typeof v === 'string' && v ? v : undefined)
  const problema: ProblemDetail = {
    status: typeof dados.status === 'number' ? dados.status : (e.status ?? e.statusCode ?? 0),
    title: texto(dados.title) ?? mensagemGenerica,
    detail: texto(dados.detail) ?? mensagemGenerica,
  }
  const codigoErro = texto(dados.codigoErro)
  if (codigoErro) problema.codigoErro = codigoErro
  if (Array.isArray(dados.erros)) {
    problema.erros = (dados.erros as Record<string, unknown>[])
      .filter(e => e && typeof e.campo === 'string' && typeof e.chave === 'string')
      .map(e => ({ campo: e.campo as string, chave: e.chave as string }))
  }
  return problema
}
