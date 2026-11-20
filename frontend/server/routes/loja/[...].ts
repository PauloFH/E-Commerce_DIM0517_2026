// Repassa /loja/** para a API em tempo de execução
export default defineEventHandler((event) => {
  return proxyRequest(event, `${useRuntimeConfig(event).apiBase}${event.path}`)
})
