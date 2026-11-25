import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/vendedor/estoque.vue'

const { consultarEstoque, darEntrada } = vi.hoisted(() => ({
  consultarEstoque: vi.fn(),
  darEntrada: vi.fn(),
}))

mockNuxtImport('useLoja', () => () => ({ consultarEstoque, darEntrada }))

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
    expect(pagina.find('[role="tab"]').attributes('aria-selected')).toBe('true')
    expect(pagina.find('[role="tabpanel"]').attributes('aria-labelledby')).toBe('aba-repor')
  })
})
