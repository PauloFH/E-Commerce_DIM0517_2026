export interface ItemCarrinho {
  codigo: number
  quantidade: number
}

const inteiroPositivo = (v: unknown): v is number => Number.isInteger(v) && (v as number) > 0

// soma a quantidade se o código já estiver no carrinho
export function adicionar(itens: ItemCarrinho[], codigo: number, quantidade = 1): ItemCarrinho[] {
  if (!inteiroPositivo(codigo) || !inteiroPositivo(quantidade)) return itens
  if (itens.some(i => i.codigo === codigo)) {
    return itens.map(i => (i.codigo === codigo ? { ...i, quantidade: i.quantidade + quantidade } : i))
  }
  return [...itens, { codigo, quantidade }]
}

// quantidade que arredonda para baixo de 1 remove o item
export function alterarQuantidade(itens: ItemCarrinho[], codigo: number, quantidade: number): ItemCarrinho[] {
  const q = Math.floor(quantidade)
  if (!(q > 0)) return remover(itens, codigo)
  return itens.map(i => (i.codigo === codigo ? { ...i, quantidade: q } : i))
}

export function remover(itens: ItemCarrinho[], codigo: number): ItemCarrinho[] {
  return itens.filter(i => i.codigo !== codigo)
}

export function totalItens(itens: ItemCarrinho[]): number {
  return itens.reduce((soma, i) => soma + i.quantidade, 0)
}

// lê o JSON salvo no navegador; lixo ou formato inválido vira carrinho vazio ou descarta só o item ruim
export function lerCarrinhoDoStorage(json: string | null | undefined): ItemCarrinho[] {
  if (!json) return []
  try {
    const dados: unknown = JSON.parse(json)
    if (!Array.isArray(dados)) return []
    return dados.reduce<ItemCarrinho[]>((itens, d) => {
      if (d && typeof d === 'object' && inteiroPositivo(d.codigo) && inteiroPositivo(d.quantidade)) {
        return adicionar(itens, d.codigo, d.quantidade)
      }
      return itens
    }, [])
  }
  catch {
    return []
  }
}
