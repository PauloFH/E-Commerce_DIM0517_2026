import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/vendedor/estoque.vue'

const { consultarEstoque, darEntrada, darSaida } = vi.hoisted(() => ({
  consultarEstoque: vi.fn(),
  darEntrada: vi.fn(),
  darSaida: vi.fn(),
}))

mockNuxtImport('useLoja', () => () => ({ consultarEstoque, darEntrada, darSaida }))

async function abrir(consulta = 'codigo=123') {
  const pagina = await mountSuspended(Pagina, { route: `/vendedor/estoque?${consulta}` })
  await flushPromises()
  return pagina
}

describe('página Gerenciar estoque', () => {
  beforeEach(async () => {
    vi.resetAllMocks()
    await useNuxtApp().$i18n.setLocale('pt-BR')
    consultarEstoque.mockResolvedValue({ codigo: 123, estoque: 15 })
  })

  it('abre com ?codigo= e consulta sozinha', async () => {
    const pagina = await abrir()
    expect(consultarEstoque).toHaveBeenCalledWith(123)
    expect(pagina.find('input').element.value).toBe('123')
    expect(pagina.find('dl').text()).toContain('15 unidades')
    expect(pagina.find('dl').text()).toContain('Em estoque')
  })

  it('404 mostra o erro no campo e some com o resumo', async () => {
    consultarEstoque.mockRejectedValue({ status: 404 })
    const pagina = await abrir()
    expect(pagina.find('p[role="alert"]').text()).toBe('Produto 123 não encontrado.')
    expect(pagina.find('dl').exists()).toBe(false)
  })

  it('consultar outro código esconde a reposição do produto anterior, mesmo se a consulta falhar', async () => {
    const pagina = await abrir()
    expect(pagina.find('dl').exists()).toBe(true)

    consultarEstoque.mockRejectedValue({ status: 0 })
    await pagina.find('input').setValue('456')
    await pagina.find('form').trigger('submit')
    await flushPromises()

    expect(pagina.find('dl').exists()).toBe(false)
    expect(pagina.find('[role="tabpanel"]').exists()).toBe(false)
    expect(darEntrada).not.toHaveBeenCalled()
  })

  it('quantidade inválida não chama a API', async () => {
    const pagina = await abrir()
    await pagina.find('[role="tabpanel"] input').setValue('0')
    await pagina.find('[role="tabpanel"] form').trigger('submit')
    expect(darEntrada).not.toHaveBeenCalled()
    expect(pagina.find('[role="tabpanel"] p[role="alert"]').text()).toBe('Informe uma quantidade inteira maior que zero.')
  })

  it('repor com sucesso mostra antes → depois e atualiza o resumo', async () => {
    darEntrada.mockResolvedValue({ codigo: 123, estoque: 25 })
    const pagina = await abrir()
    await pagina.find('[role="tabpanel"] input').setValue('10')
    await pagina.find('[role="tabpanel"] form').trigger('submit')
    await flushPromises()
    expect(darEntrada).toHaveBeenCalledWith(123, 10)
    expect(pagina.find('[role="status"]').text()).toBe('Estoque do produto 123: 15 → 25 unidades.')
    expect(pagina.find('dl').text()).toContain('25 unidades')
    expect(pagina.find<HTMLInputElement>('[role="tabpanel"] input').element.value).toBe('')
  })

  it('aba desconhecida na URL cai na primeira e o teclado navega entre as abas', async () => {
    const pagina = await abrir('codigo=123&aba=inexistente')
    const aba = pagina.find('[role="tab"]')
    expect(aba.attributes('aria-selected')).toBe('true')
    expect(aba.attributes('tabindex')).toBe('0')
    expect(aba.attributes('aria-controls')).toBe('painel-repor')

    await pagina.find('[role="tablist"]').trigger('keydown', { key: 'End' })
    expect(pagina.find('[role="tab"]').attributes('aria-selected')).toBe('false')
    expect(pagina.find('[role="tabpanel"]').attributes('aria-labelledby')).toBe('aba-baixa')
  })

  describe('aba Dar baixa', () => {
    type Pagina = Awaited<ReturnType<typeof abrir>>
    const abrirBaixa = () => abrir('codigo=123&aba=baixa')
    const painel = (pagina: Pagina) => pagina.find('[role="tabpanel"]')
    const botao = (pagina: Pagina, texto: string) => pagina.findAll('button').find(b => b.text() === texto)!
    async function pedir(pagina: Pagina, quantidade: string) {
      await painel(pagina).find('input').setValue(quantidade)
      await painel(pagina).find('form').trigger('submit')
      await flushPromises()
    }

    it('abre na aba pela query', async () => {
      const pagina = await abrirBaixa()
      expect(pagina.find('[role="tab"][aria-selected="true"]').text()).toBe('Dar baixa')
      expect(painel(pagina).attributes('aria-labelledby')).toBe('aba-baixa')
      expect(painel(pagina).find('[role="radio"][aria-checked="true"]').text()).toBe('Perda')
    })

    it('pede confirmação e só chama a API depois, sem enviar o motivo', async () => {
      darSaida.mockResolvedValue({ codigo: 123, estoque: 5 })
      const pagina = await abrirBaixa()
      await painel(pagina).findAll('[role="radio"]')[1]!.trigger('click')
      await pedir(pagina, '10')

      expect(darSaida).not.toHaveBeenCalled()
      expect(painel(pagina).text()).toContain('Dar baixa de 10 unidades do produto 123? O estoque vai de 15 para 5.')
      expect(painel(pagina).text()).toContain('Motivo: Avaria (apenas informativo)')

      await botao(pagina, 'Confirmar baixa').trigger('click')
      await flushPromises()
      expect(darSaida).toHaveBeenCalledTimes(1)
      expect(darSaida.mock.calls[0]).toEqual([123, 10])
    })

    it('cancelar não chama a API e mantém os valores', async () => {
      const pagina = await abrirBaixa()
      await pedir(pagina, '10')
      await botao(pagina, 'Cancelar').trigger('click')
      expect(darSaida).not.toHaveBeenCalled()
      expect(painel(pagina).find<HTMLInputElement>('input').element.value).toBe('10')
    })

    it('quantidade maior que o estoque mostra o erro sem chamar a API', async () => {
      const pagina = await abrirBaixa()
      await pedir(pagina, '20')
      expect(painel(pagina).find('p[role="alert"]').text()).toBe('Estoque insuficiente: há 15 unidades.')
      expect(darSaida).not.toHaveBeenCalled()
    })

    it('400 da API reconsulta o estoque e mostra quantas unidades há', async () => {
      darSaida.mockRejectedValue({ status: 400 })
      const pagina = await abrirBaixa()
      consultarEstoque.mockResolvedValue({ codigo: 123, estoque: 7 })
      await pedir(pagina, '10')
      await botao(pagina, 'Confirmar baixa').trigger('click')
      await flushPromises()

      expect(consultarEstoque).toHaveBeenCalledTimes(2)
      expect(painel(pagina).find('p[role="alert"]').text()).toBe('Estoque insuficiente: há 7 unidades.')
      expect(pagina.find('dl').text()).toContain('7 unidades')
    })

    it('sucesso mostra de 15 para 5, atualiza o resumo e limpa o formulário', async () => {
      darSaida.mockResolvedValue({ codigo: 123, estoque: 5 })
      const pagina = await abrirBaixa()
      await painel(pagina).findAll('[role="radio"]')[2]!.trigger('click')
      await pedir(pagina, '10')
      await botao(pagina, 'Confirmar baixa').trigger('click')
      await flushPromises()

      expect(pagina.find('[role="status"]').text()).toBe('Baixa registrada: estoque do produto 123 foi de 15 para 5 unidades.')
      expect(pagina.find('dl').text()).toContain('5 unidades')
      expect(painel(pagina).find<HTMLInputElement>('input').element.value).toBe('')
      expect(painel(pagina).find('[role="radio"][aria-checked="true"]').text()).toBe('Perda')
    })

    it('setas trocam a aba e o motivo', async () => {
      const pagina = await abrirBaixa()
      await painel(pagina).find('[role="radiogroup"]').trigger('keydown', { key: 'ArrowRight' })
      expect(painel(pagina).find('[role="radio"][aria-checked="true"]').text()).toBe('Avaria')
      await painel(pagina).find('[role="radiogroup"]').trigger('keydown', { key: 'ArrowLeft' })
      await painel(pagina).find('[role="radiogroup"]').trigger('keydown', { key: 'ArrowLeft' })
      expect(painel(pagina).find('[role="radio"][aria-checked="true"]').text()).toBe('Ajuste')

      await pagina.find('[role="tablist"]').trigger('keydown', { key: 'ArrowLeft' })
      expect(pagina.find('[role="tab"][aria-selected="true"]').text()).toBe('Repor estoque')
      await pagina.find('[role="tablist"]').trigger('keydown', { key: 'End' })
      expect(pagina.find('[role="tab"][aria-selected="true"]').text()).toBe('Dar baixa')
    })

    it('trocar de aba descarta a confirmação pendente', async () => {
      const pagina = await abrirBaixa()
      await pedir(pagina, '10')
      await pagina.find('[role="tablist"]').trigger('keydown', { key: 'Home' })
      await pagina.find('[role="tablist"]').trigger('keydown', { key: 'End' })
      expect(painel(pagina).find('form').exists()).toBe(true)
      expect(darSaida).not.toHaveBeenCalled()
    })
  })
})
