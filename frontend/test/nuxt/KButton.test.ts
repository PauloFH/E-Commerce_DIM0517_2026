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
})
