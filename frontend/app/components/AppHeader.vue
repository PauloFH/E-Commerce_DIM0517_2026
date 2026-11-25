<script setup lang="ts">
const { t, locale, setLocale } = useI18n()
const route = useRoute()
const { total } = useCarrinho()

const outroIdioma = computed(() => (locale.value === 'pt-BR' ? 'en' : 'pt-BR'))
const rotuloIdioma = computed(() => t(locale.value === 'pt-BR' ? 'idioma.paraEn' : 'idioma.paraPt'))
const rotuloCarrinho = computed(() => t('cabecalho.carrinhoQtd', total.value))

const menuAberto = ref(false)
const botaoMenu = ref<HTMLButtonElement>()
const botaoFechar = ref<HTMLButtonElement>()

async function abrirMenu() {
  menuAberto.value = true
  await nextTick()
  botaoFechar.value?.focus()
}

function fecharMenu(devolverFoco = false) {
  menuAberto.value = false
  if (devolverFoco) botaoMenu.value?.focus()
}

function aoTeclar(e: KeyboardEvent) {
  if (e.key === 'Escape' && menuAberto.value) fecharMenu(true)
}

onMounted(() => window.addEventListener('keydown', aoTeclar))
onBeforeUnmount(() => window.removeEventListener('keydown', aoTeclar))
watch(() => route.fullPath, () => fecharMenu())
</script>

<template>
  <header class="cabecalho">
    <div class="faixa">
      <button
        ref="botaoMenu"
        type="button"
        class="botao-menu"
        :aria-label="t('nav.abrirMenu')"
        :aria-expanded="menuAberto"
        aria-controls="gaveta"
        @click="abrirMenu"
      >
        <KIcon nome="menu" />
      </button>

      <KLogo
        fundo="escuro"
        class="logo"
      />

      <SearchBar class="busca" />

      <button
        type="button"
        class="idioma"
        :aria-label="rotuloIdioma"
        @click="setLocale(outroIdioma)"
      >
        <KIcon
          nome="idioma"
          :tamanho="20"
        />
        <span class="label">{{ t('idioma.sigla') }}</span>
      </button>

      <NuxtLink
        to="/vendedor"
        class="painel"
      >
        <KIcon
          nome="loja"
          :tamanho="26"
        />
        <span class="painel-texto">
          <span class="caption">{{ t('cabecalho.vendePergunta') }}</span>
          <span class="label">{{ t('cabecalho.painelVendedor') }}</span>
        </span>
      </NuxtLink>

      <NuxtLink
        to="/carrinho"
        class="carrinho"
        :aria-label="rotuloCarrinho"
      >
        <span class="carrinho-icone">
          <KIcon
            nome="carrinho"
            :tamanho="26"
          />
          <span
            class="contador"
            aria-hidden="true"
          >{{ total }}</span>
        </span>
        <span
          class="label carrinho-texto"
          aria-hidden="true"
        >{{ t('nav.carrinho') }}</span>
      </NuxtLink>
    </div>

    <nav
      class="faixa-secundaria"
      :aria-label="t('nav.principal')"
    >
      <NuxtLink to="/">{{ t('nav.inicio') }}</NuxtLink>
      <NuxtLink
        to="/#vistos"
        aria-current-value="false"
      >{{ t('nav.vistos') }}</NuxtLink>
      <NuxtLink to="/carrinho">{{ t('nav.carrinho') }}</NuxtLink>
      <NuxtLink to="/vendedor">{{ t('nav.vender') }}</NuxtLink>
    </nav>

    <div
      v-show="menuAberto"
      class="fundo-gaveta"
      @click.self="fecharMenu(true)"
    >
      <nav
        id="gaveta"
        class="gaveta"
        :aria-label="t('nav.menu')"
      >
        <button
          ref="botaoFechar"
          type="button"
          class="fechar"
          :aria-label="t('nav.fecharMenu')"
          @click="fecharMenu(true)"
        >
          <KIcon nome="fechar" />
        </button>
        <NuxtLink to="/">{{ t('nav.inicio') }}</NuxtLink>
        <NuxtLink
          to="/#vistos"
          aria-current-value="false"
        >{{ t('nav.vistos') }}</NuxtLink>
        <NuxtLink to="/carrinho">{{ t('nav.carrinho') }}</NuxtLink>
        <NuxtLink to="/vendedor">{{ t('cabecalho.painelVendedor') }}</NuxtLink>
        <button
          type="button"
          @click="setLocale(outroIdioma)"
        >
          <KIcon
            nome="idioma"
            :tamanho="20"
          />
          {{ rotuloIdioma }}
        </button>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.cabecalho { position: relative; }

.faixa {
  display: flex;
  align-items: center;
  gap: var(--spacing-6);
  padding: 14px 32px;
  background: var(--color-bg-brand);
  color: var(--color-text-on-brand);
}

.busca { flex: 1; min-width: 0; }

.botao-menu { display: none; }

.idioma {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2);
  height: 40px;
  padding: 0 14px;
  border: 1.5px solid #FFFFFF;
  border-radius: var(--radius-full);
  background: none;
  color: var(--color-text-on-brand);
}

.painel,
.carrinho {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2);
  color: var(--color-text-on-brand);
  text-decoration: none;
}

.painel-texto { display: flex; flex-direction: column; }

.carrinho-icone { position: relative; display: inline-flex; margin-right: var(--spacing-2); }

.contador {
  position: absolute;
  top: -8px;
  right: -10px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: var(--radius-full);
  background: var(--color-bg-accent);
  color: var(--color-text-on-accent);
  font-size: 12px;
  line-height: 20px;
  font-weight: 800;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.faixa-secundaria {
  display: flex;
  gap: var(--spacing-8);
  padding: 10px 32px;
  background: var(--color-bg-brand-hover);
}

.faixa-secundaria a {
  color: var(--color-text-on-brand);
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
  text-decoration: none;
}

.faixa-secundaria a:hover,
.faixa-secundaria a[aria-current='page'] { text-decoration: underline; text-underline-offset: 4px; }

.fundo-gaveta { display: none; }

@media (max-width: 767px) {
  .faixa {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    grid-template-areas: 'menu logo carrinho' 'busca busca busca';
    gap: var(--spacing-3);
    padding: var(--spacing-3) var(--spacing-4);
  }

  .botao-menu {
    grid-area: menu;
    justify-self: start;
    display: grid;
    place-items: center;
    width: 44px;
    height: 44px;
    border: 0;
    background: none;
    color: var(--color-text-on-brand);
  }

  .logo { grid-area: logo; }
  .busca { grid-area: busca; }
  .carrinho { grid-area: carrinho; justify-self: end; margin-right: 10px; }
  .carrinho-texto,
  .faixa .idioma,
  .faixa .painel,
  .faixa-secundaria { display: none; }

  .fundo-gaveta {
    position: fixed;
    inset: 0;
    z-index: 20;
    display: block;
    background: rgba(15, 23, 51, .5);
  }

  .gaveta {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-1);
    width: min(320px, 85vw);
    height: 100%;
    padding: var(--spacing-4);
    background: var(--color-bg-surface);
    box-shadow: var(--elevation-2);
  }

  .gaveta a,
  .gaveta button:not(.fechar) {
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
    min-height: 44px;
    padding: 0 var(--spacing-3);
    border: 0;
    border-radius: var(--radius-sm);
    background: none;
    color: var(--color-text-primary);
    font-size: 16px;
    font-weight: 600;
    text-align: left;
    text-decoration: none;
  }

  .gaveta a[aria-current='page'] { background: var(--color-bg-info); color: var(--color-text-info); }

  .fechar {
    align-self: flex-end;
    display: grid;
    place-items: center;
    width: 44px;
    height: 44px;
    border: 0;
    background: none;
  }
}
</style>
