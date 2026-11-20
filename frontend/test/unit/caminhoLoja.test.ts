import { describe, expect, it } from 'vitest'
import { caminhoLoja } from '../../server/utils/caminhoLoja'

describe('caminhoLoja', () => {
  it('mantém caminhos da API e a query string', () => {
    expect(caminhoLoja('/loja/produto/123/estoque')).toBe('/loja/produto/123/estoque')
    expect(caminhoLoja('/loja/venda?x=1')).toBe('/loja/venda?x=1')
  })

  it('bloqueia fuga de /loja por ../ literal ou codificado', () => {
    expect(caminhoLoja('/loja/../actuator/env')).toBeNull()
    expect(caminhoLoja('/loja/%2e%2e/actuator/env')).toBeNull()
    expect(caminhoLoja('/loja/produto/../../h2-console')).toBeNull()
  })

  it('bloqueia barras codificadas', () => {
    expect(caminhoLoja('/loja/produto%2F..%2F..%2Fadmin')).toBeNull()
    expect(caminhoLoja('/loja/produto%5c..')).toBeNull()
  })

  it('bloqueia path parameter do Tomcat e codificação dupla', () => {
    expect(caminhoLoja('/loja/..;/actuator/env')).toBeNull()
    expect(caminhoLoja('/loja/produto/123;jsessionid=1')).toBeNull()
    expect(caminhoLoja('/loja/%252e%252e/actuator')).toBeNull()
  })

  it('bloqueia o que não é /loja', () => {
    expect(caminhoLoja('/lojax/produto')).toBeNull()
    expect(caminhoLoja('/actuator/env')).toBeNull()
  })

  it('aceita ../ que continua dentro de /loja', () => {
    expect(caminhoLoja('/loja/produto/../venda')).toBe('/loja/venda')
  })
})
