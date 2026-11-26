import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/pedido/confirmado.vue'

const venda = {
  total: 189.7,
  itens: [
    { codigo: 123, quantidade: 2, subtotal: 119.8, estoque: 13 },
    { codigo: 456, quantidade: 1, subtotal: 69.9, estoque: 1 },
  ],
}

async function abrir() {
  const pagina = await mountSuspended(Pagina, { route: '/pedido/confirmado', attachTo: document.body })
  await flushPromises()
  return pagina
}

describe('página do pedido confirmado', () => {
  beforeEach(async () => {
    await useNuxtApp().$i18n.setLocale('pt-BR')
    useState('ultimo-pedido').value = undefined
    sessionStorage.clear()
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('mostra os valores devolvidos pela API e o estoque restante', async () => {
    useUltimoPedido().guardar(venda)
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Pedido confirmado')
    const linhas = pagina.findAll('tbody tr')
    expect(linhas.length).toBe(2)
    expect(linhas[0]!.text()).toContain('Produto 123')
    expect(linhas[0]!.text()).toContain('Código 123')
    expect(linhas[0]!.text()).toMatch(/2\s*R\$\s*119,80\s*13 unidades/)
    expect(linhas[1]!.text()).toMatch(/1\s*R\$\s*69,90\s*1 unidade$/)
    expect(pagina.find('tfoot').text()).toMatch(/TotalR\$\s*189,70/)
    expect(pagina.find('a.continuar').text()).toBe('Continuar comprando')
  })

  it('não recalcula: o total exibido é o da API, mesmo que os subtotais não somem', async () => {
    useUltimoPedido().guardar({ total: 20, itens: [{ codigo: 1, quantidade: 1, subtotal: 10, estoque: 0 }] })
    const pagina = await abrir()

    expect(pagina.find('tfoot').text()).toMatch(/R\$\s*20,00/)
  })

  it('a tabela tem legenda e cabeçalhos com escopo, e o foco vai para o título', async () => {
    useUltimoPedido().guardar(venda)
    const pagina = await abrir()

    expect(pagina.find('caption').text()).toBe('Itens do pedido')
    expect(pagina.findAll('thead th').map(th => th.attributes('scope'))).toEqual(['col', 'col', 'col', 'col'])
    expect(pagina.findAll('tbody th').every(th => th.attributes('scope') === 'row')).toBe(true)
    expect(pagina.findAll('thead th').map(th => th.text())).toEqual(['Produto', 'Quantidade', 'Subtotal', 'Estoque restante'])
    expect(document.activeElement).toBe(pagina.find('h1').element)
  })

  it('sem pedido mostra o estado vazio com botão para a loja', async () => {
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Nenhum pedido recente')
    expect(pagina.find('table').exists()).toBe(false)
    expect(pagina.find('section a[href="/"]').text()).toBe('Ir para a loja')
  })

  it('depois de um recarregamento lê o pedido do sessionStorage', async () => {
    sessionStorage.setItem('kompraki:ultimoPedido', JSON.stringify(venda))
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Pedido confirmado')
    expect(pagina.find('tfoot').text()).toMatch(/R\$\s*189,70/)
  })

  it('pedido guardado inválido vira sem pedido', async () => {
    sessionStorage.setItem('kompraki:ultimoPedido', '{"total":"x"}')
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Nenhum pedido recente')
  })

  it('em inglês traduz os textos e formata a moeda pelo idioma', async () => {
    await useNuxtApp().$i18n.setLocale('en')
    useUltimoPedido().guardar(venda)
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Order confirmed')
    expect(pagina.findAll('thead th').map(th => th.text())).toEqual(['Product', 'Quantity', 'Subtotal', 'Remaining stock'])
    expect(pagina.find('tfoot').text()).toMatch(/TotalR\$\s*189\.70/)
    expect(pagina.findAll('tbody tr')[0]!.text()).toContain('13 units')
  })
})
