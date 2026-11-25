import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mockNuxtImport, mountSuspended } from '@nuxt/test-utils/runtime'
import NovoProduto from '~/pages/vendedor/produtos/novo.vue'

const { cadastrarProduto } = vi.hoisted(() => ({ cadastrarProduto: vi.fn() }))
mockNuxtImport('useLoja', () => () => ({ cadastrarProduto }))

async function preencher(codigo: string, preco: string) {
  const tela = await mountSuspended(NovoProduto)
  const [campoCodigo, campoPreco] = tela.findAll('input')
  await campoCodigo!.setValue(codigo)
  await campoPreco!.setValue(preco)
  return tela
}

describe('tela Novo produto', () => {
  beforeEach(async () => {
    cadastrarProduto.mockReset()
    await useNuxtApp().$i18n.setLocale('pt-BR')
  })

  it('formata o preço como moeda enquanto digita, ignorando letras', async () => {
    const tela = await preencher('123', '5990a')
    // o Intl separa "R$" do número com espaço não separável
    expect(tela.findAll('input')[1]!.element.value.replace(/\s/g, ' ')).toBe('R$ 59,90')
  })

  it('não chama a API com código ou preço inválido e mostra os erros nos campos', async () => {
    const tela = await preencher('2.5', '')
    await tela.find('form').trigger('submit')

    expect(cadastrarProduto).not.toHaveBeenCalled()
    expect(tela.text()).toContain('Use um número inteiro maior que zero.')
    expect(tela.text()).toContain('Informe um preço maior que zero.')
  })

  it('código duplicado (409) aparece no campo do código', async () => {
    cadastrarProduto.mockRejectedValue({ status: 409, title: 'Conflict', detail: 'produto.ja.existe' })
    const tela = await preencher('123', '5990')
    await tela.find('form').trigger('submit')

    await vi.waitFor(() => expect(tela.text()).toContain('Já existe um produto com o código 123.'))
    expect(tela.text()).not.toContain('produto.ja.existe')
    expect(tela.findAll('input')[0]!.attributes('aria-invalid')).toBe('true')
  })

  it('no sucesso guarda o produto, limpa o formulário e oferece repor estoque', async () => {
    cadastrarProduto.mockResolvedValue({ codigo: 321, preco: 59.9, estoque: 0 })
    const tela = await preencher('321', '5990')
    await tela.find('form').trigger('submit')

    expect(cadastrarProduto).toHaveBeenCalledWith(321, 59.9)
    await vi.waitFor(() => expect(tela.text()).toContain('Produto 321 criado com estoque 0.'))
    expect(tela.find('a[href="/vendedor/estoque?codigo=321"]').exists()).toBe(true)
    expect(tela.findAll('input')[0]!.element.value).toBe('')
    expect(useProdutosConhecidos().produtos.value.find(p => p.codigo === 321))
      .toMatchObject({ codigo: 321, preco: 59.9, estoque: 0 })
  })
})
