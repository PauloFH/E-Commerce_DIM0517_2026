import type { Ref } from 'vue'

// Carrega o estado do localStorage só depois da hidratação (evita divergência com o SSR)
// e passa a gravar cada mudança. No servidor não faz nada.
export function persistirNoCliente<T>(chave: string, estado: Ref<T>, ler: (json: string | null) => T) {
  const ligado = useState(`persistencia:${chave}`, () => false)
  if (!import.meta.client || ligado.value) return
  ligado.value = true
  onNuxtReady(() => {
    try {
      estado.value = ler(localStorage.getItem(chave))
    }
    catch { /* storage indisponível: segue só em memória */ }
    watch(estado, (valor) => {
      try {
        localStorage.setItem(chave, JSON.stringify(valor))
      }
      catch { /* storage cheio ou bloqueado */ }
    }, { deep: true })
  })
}
