import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/carrinho.vue'

const { registrarVenda, navegar } = vi.hoisted(() => ({ registrarVenda: vi.fn(), navegar: vi.fn() }))
mockNuxtImport('useLoja', () => () => ({ registrarVenda }))
mockNuxtImport('navigateTo', () => navegar)

const venda = {
  total: 189.7,
  itens: [
    { codigo: 123, quantidade: 2, subtotal: 119.8, estoque: 13 },
    { codigo: 456, quantidade: 1, subtotal: 69.9, estoque: 7 },
  ],
}

async function abrir(itens = [{ codigo: 123, quantidade: 2 }, { codigo: 456, quantidade: 1 }]) {
  itens.forEach(i => useCarrinho().adicionar(i.codigo, i.quantidade))
  const pagina = await mountSuspended(Pagina, { route: '/carrinho', attachTo: document.body })
  await flushPromises()
  return pagina
}

const botao = (pagina: Awaited<ReturnType<typeof abrir>>, texto: string) => pagina.findAll('button').find(b => b.text() === texto)!
const removerDe = (pagina: Awaited<ReturnType<typeof abrir>>, codigo: number) => pagina.find(`button[aria-label="Remover Produto ${codigo}"]`)

describe('página do carrinho', () => {
  beforeEach(async () => {
    await useNuxtApp().$i18n.setLocale('pt-BR')
    // espera a leitura do localStorage terminar para não apagar o que o teste montar
    useCarrinho()
    useProdutosConhecidos()
    await new Promise<void>(resolve => onNuxtReady(() => resolve()))
    useCarrinho().esvaziar()
    useState('produtos-conhecidos').value = {}
    useState('ultimo-pedido').value = undefined
    sessionStorage.clear()
    registrarVenda.mockReset()
    navegar.mockReset()
    useProdutosConhecidos().registrar({ codigo: 123, preco: 59.9 })
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('com itens mostra o subtotal estimado, o preço no fechamento e o total parcial', async () => {
    const pagina = await abrir()

    expect(pagina.find('h1').text()).toBe('Carrinho (2 produtos)')
    expect(pagina.text()).toMatch(/R\$\s*119,80 estimado/)
    expect(pagina.text()).toContain('Preço no fechamento')
    const resumo = pagina.find('aside')
    expect(resumo.text()).toContain('Unidades3')
    expect(resumo.text()).toMatch(/Total estimadoR\$\s*119,80/)
    expect(resumo.text()).toContain('Não inclui 1 item sem preço conhecido.')
    expect(resumo.text()).toContain('O valor final é confirmado ao finalizar.')
  })

  it('sem nenhum preço conhecido esconde o total e a nota', async () => {
    useState('produtos-conhecidos').value = {}
    const pagina = await abrir()
    const resumo = pagina.find('aside')

    expect(resumo.text()).toContain('Total estimadoPreço no fechamento')
    expect(resumo.text()).not.toContain('R$')
    expect(resumo.text()).not.toContain('Não inclui')
  })

  it('vazio mostra o convite para ver produtos', async () => {
    const pagina = await abrir([])

    expect(pagina.find('h1').text()).toBe('Carrinho')
    expect(pagina.find('h2').text()).toBe('Seu carrinho está vazio')
    expect(pagina.find('section a[href="/"]').text()).toBe('Ver produtos')
    expect(pagina.find('aside').exists()).toBe(false)
  })

  it('alterar a quantidade e remover atualizam o carrinho', async () => {
    const pagina = await abrir()

    await pagina.findAll('.lista input')[0]!.setValue('5')
    expect(useCarrinho().itens.value).toEqual([{ codigo: 123, quantidade: 5 }, { codigo: 456, quantidade: 1 }])
    expect(pagina.text()).toMatch(/R\$\s*299,50 estimado/)

    await removerDe(pagina, 456).trigger('click')
    expect(useCarrinho().itens.value).toEqual([{ codigo: 123, quantidade: 5 }])
    expect(pagina.find('h1').text()).toBe('Carrinho (1 produto)')
  })

  it('o botão remover tem texto visível e rótulo com o código', async () => {
    const pagina = await abrir()
    const remover = removerDe(pagina, 123)

    expect(remover.text()).toBe('Remover')
    expect(remover.attributes('aria-label')).toBe('Remover Produto 123')
  })

  it('finalizar envia os itens, guarda o pedido, esvazia o carrinho e vai para a confirmação', async () => {
    registrarVenda.mockResolvedValue(venda)
    const pagina = await abrir()

    await botao(pagina, 'Finalizar compra').trigger('click')
    await flushPromises()

    expect(registrarVenda).toHaveBeenCalledWith([{ codigo: 123, quantidade: 2 }, { codigo: 456, quantidade: 1 }])
    expect(useCarrinho().itens.value).toEqual([])
    expect(useUltimoPedido().pedido.value).toEqual(venda)
    expect(JSON.parse(sessionStorage.getItem('kompraki:ultimoPedido')!)).toEqual(venda)
    expect(useProdutosConhecidos().produtos.value.map(p => [p.codigo, p.estoque]).sort()).toEqual([[123, 13], [456, 7]])
    expect(useProdutosConhecidos().produtos.value.find(p => p.codigo === 123)!.preco).toBe(59.9)
    expect(navegar).toHaveBeenCalledWith('/pedido/confirmado')
  })

  it('enquanto envia, o botão fica ocupado e os controles desabilitados', async () => {
    let concluir!: (v: typeof venda) => void
    registrarVenda.mockReturnValue(new Promise((resolve) => {
      concluir = resolve
    }))
    const pagina = await abrir()

    await botao(pagina, 'Finalizar compra').trigger('click')

    const enviando = botao(pagina, 'Enviando…')
    expect(enviando.attributes('disabled')).toBeDefined()
    expect(enviando.attributes('aria-busy')).toBe('true')
    expect(pagina.findAll('.lista button, .lista input').every(e => e.attributes('disabled') !== undefined)).toBe(true)

    concluir(venda)
    await flushPromises()
  })

  describe('erros', () => {
    const itens = [{ codigo: 123, quantidade: 2 }, { codigo: 456, quantidade: 5 }]

    it('estoque insuficiente marca a linha, avisa no topo e mantém o carrinho', async () => {
      registrarVenda.mockRejectedValue({ status: 409, title: 't', detail: 'd', codigoErro: 'produto.estoque.insuficiente', argumentos: [3, 456] })
      const pagina = await abrir(itens)

      await botao(pagina, 'Finalizar compra').trigger('click')
      await flushPromises()

      const alerta = pagina.find('[role="alert"]')
      expect(alerta.text()).toBe('Estoque insuficiente para o produto 456: há 3 unidades.')
      expect(alerta.attributes('tabindex')).toBe('-1')
      expect(document.activeElement).toBe(alerta.element)
      const linhas = pagina.findAll('.lista > li')
      expect(linhas[1]!.classes()).toContain('comErro')
      expect(linhas[1]!.text()).toContain('Estoque insuficiente: há 3 unidades.')
      expect(linhas[0]!.classes()).not.toContain('comErro')
      expect(useCarrinho().itens.value).toEqual(itens)
      expect(navegar).not.toHaveBeenCalled()
      expect(botao(pagina, 'Finalizar compra').attributes('disabled')).toBeUndefined()
    })

    it('alterar a quantidade da linha em erro limpa o erro e o alerta', async () => {
      registrarVenda.mockRejectedValue({ status: 409, title: 't', detail: 'd', codigoErro: 'produto.estoque.insuficiente', argumentos: [3, 456] })
      const pagina = await abrir(itens)
      await botao(pagina, 'Finalizar compra').trigger('click')
      await flushPromises()

      await pagina.findAll('.lista input')[1]!.setValue('3')

      expect(pagina.findAll('.lista > li')[1]!.classes()).not.toContain('comErro')
      expect(pagina.find('[role="alert"]').exists()).toBe(false)
    })

    it('produto inexistente destaca o botão remover e remover limpa o erro', async () => {
      registrarVenda.mockRejectedValue({ status: 404, title: 't', detail: 'd', codigoErro: 'produto.nao.encontrado', argumentos: [999] })
      const pagina = await abrir([{ codigo: 123, quantidade: 2 }, { codigo: 999, quantidade: 1 }])

      await botao(pagina, 'Finalizar compra').trigger('click')
      await flushPromises()

      expect(pagina.find('[role="alert"]').text()).toBe('O produto 999 não existe mais. Remova-o do carrinho para continuar.')
      expect(pagina.findAll('.lista > li')[1]!.text()).toContain('O produto 999 não existe mais.')
      expect(removerDe(pagina, 999).classes()).toContain('destaque')
      expect(removerDe(pagina, 123).classes()).not.toContain('destaque')

      await removerDe(pagina, 999).trigger('click')

      expect(pagina.find('[role="alert"]').exists()).toBe(false)
      expect(useCarrinho().itens.value).toEqual([{ codigo: 123, quantidade: 2 }])
    })

    it('erro de rede mostra o alerta de conexão sem marcar linhas', async () => {
      registrarVenda.mockRejectedValue({ status: 0, title: '', detail: '' })
      const pagina = await abrir()

      await botao(pagina, 'Finalizar compra').trigger('click')
      await flushPromises()

      expect(pagina.find('[role="alert"]').text()).toContain('Não foi possível falar com o servidor')
      expect(pagina.find('.comErro').exists()).toBe(false)
      expect(useCarrinho().itens.value.length).toBe(2)
    })

    it('validação 400 e outros erros mostram o alerta genérico em vez do detail da API', async () => {
      registrarVenda.mockRejectedValue({ status: 400, title: 't', detail: 'requisicao.dados.invalidos', erros: [{ campo: 'itens[1].quantidade', chave: 'x' }] })
      const pagina = await abrir()

      await botao(pagina, 'Finalizar compra').trigger('click')
      await flushPromises()

      expect(pagina.find('[role="alert"]').text()).toBe('Algo deu errado. Tente novamente.')
      expect(pagina.find('.comErro').exists()).toBe(false)
    })
  })

  it('em inglês traduz os textos e formata a moeda pelo idioma', async () => {
    await useNuxtApp().$i18n.setLocale('en')
    const pagina = await abrir([{ codigo: 123, quantidade: 2 }])

    expect(pagina.find('h1').text()).toBe('Cart (1 product)')
    expect(pagina.text()).toMatch(/R\$\s*119\.80 estimated/)
    expect(pagina.find('aside').text()).toContain('Estimated total')
    expect(pagina.find('button[aria-label="Remove Product 123"]').text()).toBe('Remove')
    expect(botao(pagina, 'Check out').exists()).toBe(true)
  })
})
