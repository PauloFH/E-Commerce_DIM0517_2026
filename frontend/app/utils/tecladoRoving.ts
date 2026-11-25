// destino de ←/→/Home/End num grupo com tabindex móvel, ou undefined se a tecla não navega
export function indiceDaTecla(tecla: string, atual: number, total: number): number | undefined {
  if (tecla === 'ArrowRight') return (atual + 1) % total
  if (tecla === 'ArrowLeft') return (atual - 1 + total) % total
  if (tecla === 'Home') return 0
  if (tecla === 'End') return total - 1
  return undefined
}
