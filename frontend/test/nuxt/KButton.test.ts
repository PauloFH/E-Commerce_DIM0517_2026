import { describe, expect, it } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import KButton from '~/components/KButton.vue'

describe('KButton', () => {
  it('renderiza <button> sem "to"', async () => {
    const c = await mountSuspended(KButton, { slots: { default: 'Comprar' } })
    expect(c.find('button').exists()).toBe(true)
    expect(c.find('a').exists()).toBe(false)
    expect(c.text()).toBe('Comprar')
    expect(c.find('button').attributes('type')).toBe('button')
  })

  it('aceita type="submit" para enviar formulários', async () => {
    const c = await mountSuspended(KButton, { props: { type: 'submit' } })
    expect(c.find('button').attributes('type')).toBe('submit')
  })

  it('renderiza link com "to"', async () => {
    const c = await mountSuspended(KButton, { props: { to: '/carrinho' }, slots: { default: 'Ir' } })
    expect(c.find('a').attributes('href')).toBe('/carrinho')
    expect(c.find('button').exists()).toBe(false)
  })

  it('desabilitado vira botão desabilitado, mesmo com "to"', async () => {
    const c = await mountSuspended(KButton, { props: { to: '/carrinho', disabled: true } })
    expect(c.find('a').exists()).toBe(false)
    expect(c.find('button').attributes('disabled')).toBeDefined()
  })

  it('carregando desabilita, marca aria-busy e mostra o indicador', async () => {
    const c = await mountSuspended(KButton, { props: { carregando: true, to: '/carrinho' }, slots: { default: 'Enviando…' } })
    const botao = c.find('button')
    expect(botao.attributes('disabled')).toBeDefined()
    expect(botao.attributes('aria-busy')).toBe('true')
    expect(botao.find('.giro').exists()).toBe(true)
    expect(c.find('a').exists()).toBe(false)
  })

  it('sem carregando não marca aria-busy', async () => {
    const c = await mountSuspended(KButton, { slots: { default: 'Ok' } })
    expect(c.find('button').attributes('aria-busy')).toBeUndefined()
    expect(c.find('.giro').exists()).toBe(false)
  })
})
