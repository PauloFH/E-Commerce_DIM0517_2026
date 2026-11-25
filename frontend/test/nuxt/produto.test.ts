import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/produto/[codigo].vue'
import KQuantidade from '~/components/KQuantidade.vue'

const { consultarEstoque } = vi.hoisted(() => ({ consultarEstoque: vi.fn() }))
mockNuxtImport('useLoja', () => () => ({ consultarEstoque }))

async function abrir(codigo: number, estoque: number) {
  consultarEstoque.mockResolvedValue({ codigo, estoque })
  return mountSuspended(Pagina, { route: `/produto/${codigo}` })
}

describe('página do produto', () => {
  beforeEach(async () => {
    // o ambiente de teste abre em inglês
    await useNuxtApp().$i18n.setLocale('pt-BR')
    clearNuxtData()
    useCarrinho().esvaziar()
    // espera a leitura do localStorage terminar para não apagar o que o teste registrar
    useProdutosConhecidos()
    await new Promise<void>(resolve => onNuxtReady(() => resolve()))
    consultarEstoque.mockReset()
  })

  it('em estoque mostra o selo com a quantidade e o preço conhecido', async () => {
    useProdutosConhecidos().registrar({ codigo: 101, preco: 59.9 })
    const c = await abrir(101, 15)

    expect(consultarEstoque).toHaveBeenCalledWith(101)
    expect(c.text()).toContain('Produto 101')
    expect(c.text()).toContain('Em estoque: 15 unidades')
    expect(c.text()).toContain('Máximo: 15 unidades')
    expect(c.text()).toMatch(/R\$\s*59,90/)
  })

  it('sem preço conhecido mostra o texto do fechamento da compra', async () => {
    const c = await abrir(102, 3)

    expect(c.text()).toContain('Últimas 3 unidades')
    expect(c.text()).toContain('Preço confirmado no fechamento da compra')
    expect(c.text()).not.toContain('R$')
  })

  it('esgotado desabilita o seletor e os botões', async () => {
    const c = await abrir(103, 0)

    expect(c.text()).toContain('Esgotado')
    expect(c.text()).toContain('Indisponível no momento')
    const controles = c.findAll('button, input')
    expect(controles.length).toBe(5)
    expect(controles.every(e => e.attributes('disabled') !== undefined)).toBe(true)
  })

  it('adicionar coloca a quantidade escolhida no carrinho e mostra o alerta', async () => {
    const c = await abrir(104, 15)

    await c.find('input').setValue('4')
    await c.findAll('button').find(b => b.text() === 'Adicionar ao carrinho')!.trigger('click')

    expect(useCarrinho().itens.value).toEqual([{ codigo: 104, quantidade: 4 }])
    expect(c.text()).toContain('Produto 104 adicionado ao carrinho.')
    expect(c.find('a[href="/carrinho"]').text()).toBe('Ver carrinho')
  })

  it('erro de rede mostra o alerta no lugar da caixa de compra', async () => {
    consultarEstoque.mockRejectedValue({ status: 0, title: '', detail: '' })
    const c = await mountSuspended(Pagina, { route: '/produto/105' })

    expect(c.find('[role="alert"]').text()).toContain('Não foi possível falar com o servidor')
    expect(c.text()).not.toContain('Adicionar ao carrinho')
  })
})

describe('KQuantidade', () => {
  it('não passa do máximo nem do mínimo', async () => {
    const c = await mountSuspended(KQuantidade, { props: { 'modelValue': 14, 'max': 15, 'onUpdate:modelValue': (v: number) => c.setProps({ modelValue: v }) } })
    const [menos, mais] = c.findAll('button')

    await mais!.trigger('click')
    await mais!.trigger('click')
    expect(c.props('modelValue')).toBe(15)

    await c.find('input').setValue('99')
    expect(c.props('modelValue')).toBe(15)

    await c.find('input').setValue('1')
    await menos!.trigger('click')
    expect(c.props('modelValue')).toBe(1)
  })

  it('valor inválido volta ao último válido ao sair do campo', async () => {
    const c = await mountSuspended(KQuantidade, { props: { 'modelValue': 5, 'max': 15, 'onUpdate:modelValue': (v: number) => c.setProps({ modelValue: v }) } })
    const campo = c.find('input')

    await campo.setValue('abc')
    await campo.trigger('blur')
    expect(c.props('modelValue')).toBe(5)
    expect(campo.element.value).toBe('5')
  })
})
