// Repassa /loja/** para a API em tempo de execução
const METODOS = new Set(['GET', 'POST', 'PUT'])

export default defineEventHandler((event) => {
  if (!METODOS.has(event.method)) throw createError({ statusCode: 405 })
  const caminho = caminhoLoja(event.path)
  if (!caminho) throw createError({ statusCode: 400 })
  const base = String(useRuntimeConfig(event).apiBase).replace(/\/+$/, '')
  return proxyRequest(event, `${base}${caminho}`)
})
