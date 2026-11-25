// texto digitado que não é inteiro volta ao valor anterior; o resto fica entre min e max
export function limitarQuantidade(entrada: string | number, min: number, max: number, anterior: number): number {
  const texto = String(entrada).trim()
  if (!/^\d+$/.test(texto)) return anterior
  return Math.min(Math.max(Number(texto), min), max)
}
