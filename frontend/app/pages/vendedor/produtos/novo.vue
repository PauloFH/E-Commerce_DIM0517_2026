<script setup lang="ts">
import type { ProblemDetail } from '~/utils/api'
import { centavosDe, validarCodigo, validarPreco } from '~/utils/formularioProduto'

definePageMeta({ layout: 'vendedor' })

const { t, locale } = useI18n()
const { cadastrarProduto } = useLoja()
const { registrar } = useProdutosConhecidos()
useHead({ title: () => `${t('paginas.novoProduto.titulo')} · kompraki` })

interface Mensagem { chave: string, parametros?: Record<string, unknown> }

const codigoTexto = ref('')
const centavos = ref(0)
const erroCodigo = ref<Mensagem | null>(null)
const erroPreco = ref<Mensagem | null>(null)
const erroGeral = ref<Mensagem | null>(null)
const criado = ref<number | null>(null)
const enviando = ref(false)

const precoTexto = computed(() => (centavos.value ? formatarPreco(centavos.value / 100, locale.value) : ''))

// formata direto no campo: se o valor formatado não muda (ex.: digitou uma letra), o Vue não redesenha
function mascararPreco(evento: Event) {
  const campo = evento.target as HTMLInputElement
  centavos.value = centavosDe(campo.value)
  campo.value = precoTexto.value
}

const codigoPrevia = computed(() => (validarCodigo(codigoTexto.value) ? null : Number(codigoTexto.value)))

// guarda a chave, e não o texto, para a mensagem acompanhar a troca de idioma
const traduzir = (m: Mensagem | null) => (m ? t(m.chave, m.parametros ?? {}) : undefined)

function validar(): boolean {
  const codigo = validarCodigo(codigoTexto.value)
  erroCodigo.value = codigo === 'obrigatorio'
    ? { chave: 'paginas.novoProduto.erros.codigoObrigatorio' }
    : codigo === 'invalido' ? { chave: 'paginas.novoProduto.erros.codigoInvalido' } : null
  erroPreco.value = validarPreco(centavos.value) ? { chave: 'paginas.novoProduto.erros.precoObrigatorio' } : null
  return !erroCodigo.value && !erroPreco.value
}

async function cadastrar() {
  criado.value = null
  erroGeral.value = null
  if (!validar() || enviando.value) return
  const codigo = Number(codigoTexto.value.trim())
  enviando.value = true
  try {
    const produto = await cadastrarProduto(codigo, centavos.value / 100)
    registrar(produto)
    criado.value = produto.codigo
    codigoTexto.value = ''
    centavos.value = 0
  }
  catch (erro) {
    const problema = erro as ProblemDetail
    // o detail da API ainda vem como chave (#4); a tela usa as próprias mensagens
    if (problema.status === 409) erroCodigo.value = { chave: 'paginas.novoProduto.erros.codigoDuplicado', parametros: { codigo } }
    else erroGeral.value = { chave: problema.status === 0 ? 'erros.rede' : 'erros.generico' }
  }
  finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="pagina">
    <KBreadcrumb
      :itens="[
        { texto: t('vendedor.titulo'), to: '/vendedor' },
        { texto: t('paginas.novoProduto.titulo') },
      ]"
    />
    <h1 class="heading-lg">
      {{ t('paginas.novoProduto.titulo') }}
    </h1>
    <p class="body texto">
      {{ t('paginas.novoProduto.texto') }}
    </p>

    <div class="conteudo">
      <div class="coluna">
        <KAlert
          v-if="criado !== null"
          tipo="sucesso"
          class="aviso"
        >
          <span>{{ t('paginas.novoProduto.sucesso', { codigo: criado }) }}</span>
          <KButton
            variante="secundario"
            :to="`/vendedor/estoque?codigo=${criado}`"
          >
            {{ t('paginas.novoProduto.reporEstoque') }}
          </KButton>
        </KAlert>
        <KAlert
          v-if="erroGeral"
          tipo="erro"
        >
          {{ traduzir(erroGeral) }}
        </KAlert>

        <form
          class="formulario"
          novalidate
          @submit.prevent="cadastrar"
        >
          <KInput
            v-model="codigoTexto"
            :label="t('paginas.novoProduto.codigo')"
            :erro="traduzir(erroCodigo)"
            :placeholder="t('paginas.novoProduto.codigoExemplo')"
            inputmode="numeric"
            autocomplete="off"
          />
          <KInput
            :model-value="precoTexto"
            :label="t('paginas.novoProduto.preco')"
            :erro="traduzir(erroPreco)"
            :placeholder="formatarPreco(0, locale)"
            inputmode="numeric"
            autocomplete="off"
            @input="mascararPreco"
          />
          <KButton
            type="submit"
            :disabled="enviando"
            class="enviar"
          >
            {{ t('paginas.novoProduto.cadastrar') }}
          </KButton>
        </form>
      </div>

      <section
        v-if="codigoPrevia !== null"
        class="previa"
        :aria-label="t('paginas.novoProduto.previa')"
      >
        <h2 class="label">
          {{ t('paginas.novoProduto.previa') }}
        </h2>
        <p class="caption texto">
          {{ t('paginas.novoProduto.previaTexto') }}
        </p>
        <!-- só ilustração: o produto ainda não existe, então link e botão do card ficam inativos -->
        <div
          inert
          class="cartao"
        >
          <ProductCard
            :codigo="codigoPrevia"
            :preco="centavos / 100"
            :estoque="0"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.pagina { display: flex; flex-direction: column; gap: var(--spacing-4); }
.texto { color: var(--color-text-secondary); }

.conteudo {
  display: grid;
  grid-template-columns: minmax(0, 480px) 232px;
  align-items: start;
  gap: var(--spacing-12);
}

.coluna { display: flex; flex-direction: column; gap: var(--spacing-4); }

.aviso {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-4);
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-5);
  padding: var(--spacing-6);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
}

.enviar { align-self: flex-start; }

.previa { display: flex; flex-direction: column; gap: var(--spacing-2); }
.cartao { margin-top: var(--spacing-2); }

@media (max-width: 767px) {
  .conteudo { grid-template-columns: 1fr; gap: var(--spacing-8); }
  .enviar { align-self: stretch; }
  .aviso { flex-direction: column; align-items: flex-start; }
}
</style>
