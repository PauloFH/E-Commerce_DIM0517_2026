// Só repassa caminhos dentro de /loja cujos segmentos tenham letras, números, - ou _.
export function caminhoLoja(caminhoBruto: string): string | null {
  const url = new URL(caminhoBruto, 'http://interno')
  const segmentos = url.pathname.split('/').slice(1)
  if (segmentos[0] !== 'loja' || !segmentos.every(s => /^[\w-]+$/.test(s))) return null
  return url.pathname + url.search
}
