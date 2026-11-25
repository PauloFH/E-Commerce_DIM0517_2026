import type { ProblemDetail, Venda } from '~/utils/api'
import type { ItemCarrinho } from '~/utils/carrinho'

export type ErroVenda
  = | { tipo: 'estoque', codigo: number, disponivel: number }
    | { tipo: 'inexistente', codigo: number }
    | { tipo: 'rede' | 'generico' }

// soma só os itens com preço conhecido (em centavos, para não acumular erro de ponto flutuante)
export function estimarTotal(itens: ItemCarrinho[], preco: (codigo: number) => number | undefined): { total?: number, semPreco: number } {
  let centavos = 0
  let semPreco = 0
  let conhecidos = 0
  for (const { codigo, quantidade } of itens) {
    const valor = preco(codigo)
    if (valor === undefined) {
      semPreco++
      continue
    }
    conhecidos++
    centavos += Math.round(valor * 100) * quantidade
  }
  return { total: conhecidos ? centavos / 100 : undefined, semPreco }
}

const inteiro = (v: unknown) => (Number.isInteger(Number(v)) && v !== null && v !== '' ? Number(v) : undefined)

// o código de erro é estável; os textos mostrados vêm sempre do i18n do front
export function classificarErroVenda(problema: ProblemDetail): ErroVenda {
  if (problema.status === 0) return { tipo: 'rede' }
  const [a, b] = problema.argumentos ?? []
  if (problema.codigoErro === 'produto.estoque.insuficiente') {
    const disponivel = inteiro(a)
    const codigo = inteiro(b)
    if (disponivel !== undefined && codigo !== undefined) return { tipo: 'estoque', codigo, disponivel }
  }
  if (problema.codigoErro === 'produto.nao.encontrado') {
    const codigo = inteiro(a)
    if (codigo !== undefined) return { tipo: 'inexistente', codigo }
  }
  return { tipo: 'generico' }
}

// leitura tolerante do pedido guardado: formato inválido vira "sem pedido"
export function lerPedidoDoStorage(json: string | null | undefined): Venda | undefined {
  if (!json) return undefined
  try {
    const d = JSON.parse(json) as Venda
    const numero = (v: unknown) => typeof v === 'number' && Number.isFinite(v)
    const valido = numero(d?.total) && Array.isArray(d.itens) && d.itens.length > 0
      && d.itens.every(i => i && numero(i.codigo) && numero(i.quantidade) && numero(i.subtotal) && numero(i.estoque))
    return valido ? d : undefined
  }
  catch {
    return undefined
  }
}
