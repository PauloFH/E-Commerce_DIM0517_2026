import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import Pagina from '~/pages/vendedor/estoque.vue'

const { consultarEstoque, darEntrada } = vi.hoisted(() => ({
  consultarEstoque: vi.fn(),
  darEntrada: vi.fn(),
}))

mockNuxtImport('useLoja', () => () => ({ consultarEstoque, darEntrada }))

async function abrir() {
  const pagina = await mountSuspended(Pagina, { route: '/vendedor/estoque?codigo=123' })
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
})
