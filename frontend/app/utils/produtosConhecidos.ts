export interface ProdutoConhecido {
  codigo: number
  preco?: number
  estoque?: number
  vistoEm: number
}

export type MapaProdutos = Record<string, ProdutoConhecido>

const inteiroPositivo = (v: unknown): v is number => Number.isInteger(v) && (v as number) > 0
const numeroValido = (v: unknown): v is number => typeof v === 'number' && Number.isFinite(v) && v >= 0

// mescla sem apagar campos já conhecidos e marca o produto como visto agora
export function registrar(
  mapa: MapaProdutos,
  parcial: { codigo: number, preco?: number, estoque?: number },
  agora = Date.now()
): MapaProdutos {
  const atual = mapa[parcial.codigo]
  const novo: ProdutoConhecido = { ...atual, codigo: parcial.codigo, vistoEm: agora }
  if (parcial.preco !== undefined) novo.preco = parcial.preco
  if (parcial.estoque !== undefined) novo.estoque = parcial.estoque
  return { ...mapa, [parcial.codigo]: novo }
}

// mais recentes primeiro
export function listar(mapa: MapaProdutos): ProdutoConhecido[] {
  return Object.values(mapa).sort((a, b) => b.vistoEm - a.vistoEm)
}

// leitura tolerante: qualquer entrada inválida é descartada
export function lerProdutosDoStorage(json: string | null | undefined): MapaProdutos {
  if (!json) return {}
  try {
    const dados: unknown = JSON.parse(json)
    if (!dados || typeof dados !== 'object' || Array.isArray(dados)) return {}
    const mapa: MapaProdutos = {}
    for (const d of Object.values(dados) as Record<string, unknown>[]) {
      if (!d || typeof d !== 'object' || !inteiroPositivo(d.codigo) || !numeroValido(d.vistoEm)) continue
      const p: ProdutoConhecido = { codigo: d.codigo, vistoEm: d.vistoEm }
      if (numeroValido(d.preco)) p.preco = d.preco
      if (numeroValido(d.estoque)) p.estoque = d.estoque
      mapa[p.codigo] = p
    }
    return mapa
  }
  catch {
    return {}
  }
}
