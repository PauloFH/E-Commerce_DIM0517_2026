import { describe, expect, it } from 'vitest'
import { normalizarErro } from '../../app/utils/api'

describe('normalizarErro', () => {
  it('lê o ProblemDetail da resposta', () => {
    const erro = { response: {}, status: 409, data: { status: 409, title: 'Conflito', detail: 'Estoque insuficiente', codigoErro: 'ESTOQUE_INSUFICIENTE' } }
    expect(normalizarErro(erro, 'rede', 'generico')).toEqual({
      status: 409, title: 'Conflito', detail: 'Estoque insuficiente', codigoErro: 'ESTOQUE_INSUFICIENTE',
    })
  })

  it('usa a mensagem genérica quando o corpo não é ProblemDetail', () => {
    expect(normalizarErro({ response: {}, statusCode: 500, data: '<html>' }, 'rede', 'generico'))
      .toEqual({ status: 500, title: 'generico', detail: 'generico' })
  })

  it('erro de rede vira status 0', () => {
    expect(normalizarErro(new TypeError('Failed to fetch'), 'sem rede', 'generico'))
      .toEqual({ status: 0, title: 'sem rede', detail: 'sem rede' })
  })
})
