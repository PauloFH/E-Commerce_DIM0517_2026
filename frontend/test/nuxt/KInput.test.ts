import { describe, expect, it } from 'vitest'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import KInput from '~/components/KInput.vue'

describe('KInput', () => {
  it('associa o label ao campo', async () => {
    const c = await mountSuspended(KInput, { props: { label: 'Código' } })
    const input = c.find('input')
    expect(c.find('label').attributes('for')).toBe(input.attributes('id'))
    expect(input.attributes('aria-invalid')).toBeUndefined()
    expect(input.attributes('aria-describedby')).toBeUndefined()
  })

  it('com erro marca aria-invalid e aponta para a mensagem', async () => {
    const c = await mountSuspended(KInput, { props: { label: 'Código', erro: 'Valor inválido' } })
    const input = c.find('input')
    const mensagem = c.find('p')
    expect(input.attributes('aria-invalid')).toBe('true')
    expect(input.attributes('aria-describedby')).toBe(mensagem.attributes('id'))
    expect(mensagem.text()).toBe('Valor inválido')
  })

  it('mantém o aria-describedby de quem usa junto com o da mensagem de erro', async () => {
    const c = await mountSuspended(KInput, { props: { label: 'Código', erro: 'Valor inválido' }, attrs: { 'aria-describedby': 'dica' } })
    expect(c.find('input').attributes('aria-describedby')).toBe(`dica ${c.find('p').attributes('id')}`)
  })
})
