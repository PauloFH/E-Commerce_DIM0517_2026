<script setup lang="ts">
const { t, locale, locales, setLocale } = useI18n()
</script>

<template>
  <footer class="rodape">
    <div class="topo">
      <div class="marca">
        <KLogo fundo="escuro" />
        <p class="body">{{ t('marca.lema') }}</p>
      </div>

      <nav class="coluna" :aria-label="t('rodape.comprar')">
        <h2 class="label">{{ t('rodape.comprar') }}</h2>
        <ul>
          <li><button type="button" @click="focarBusca">{{ t('rodape.buscarCodigo') }}</button></li>
          <li><NuxtLink to="/#vistos" aria-current-value="false">{{ t('rodape.vistos') }}</NuxtLink></li>
          <li><NuxtLink to="/carrinho">{{ t('rodape.carrinho') }}</NuxtLink></li>
        </ul>
      </nav>

      <nav class="coluna" :aria-label="t('rodape.vender')">
        <h2 class="label">{{ t('rodape.vender') }}</h2>
        <ul>
          <li><NuxtLink to="/vendedor">{{ t('rodape.painel') }}</NuxtLink></li>
          <li><NuxtLink to="/vendedor/produtos/novo">{{ t('rodape.novoProduto') }}</NuxtLink></li>
          <li><NuxtLink to="/vendedor/estoque">{{ t('rodape.gerenciarEstoque') }}</NuxtLink></li>
        </ul>
      </nav>

      <div class="coluna">
        <h2 class="label">{{ t('rodape.idioma') }}</h2>
        <ul>
          <li v-for="l in locales" :key="l.code">
            <button type="button" :lang="l.code" :aria-pressed="l.code === locale" @click="setLocale(l.code)">
              {{ l.name }}
            </button>
          </li>
        </ul>
      </div>
    </div>

    <hr>
    <p class="caption copia">{{ t('rodape.copia') }}</p>
  </footer>
</template>

<style scoped>
.rodape {
  padding: 48px 64px 32px;
  background: var(--color-bg-inverse);
  color: var(--color-text-on-brand);
}

.topo {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: var(--spacing-8);
}

.marca { display: flex; flex-direction: column; gap: var(--spacing-3); }

.coluna h2 { margin-bottom: var(--spacing-3); color: var(--color-text-on-brand); }
.coluna ul { display: flex; flex-direction: column; gap: var(--spacing-2); }

.coluna a,
.coluna button {
  padding: 0;
  border: 0;
  background: none;
  color: inherit;
  font-size: 14px;
  line-height: 20px;
  text-align: left;
  text-decoration: none;
}

.coluna a:hover,
.coluna button:hover { text-decoration: underline; }
.coluna button[aria-pressed='true'] { font-weight: 700; }

hr {
  margin: var(--spacing-8) 0 var(--spacing-5);
  border: 0;
  border-top: 1px solid rgba(255, 255, 255, .2);
}

@media (max-width: 767px) {
  .rodape { padding: var(--spacing-8) var(--spacing-4) var(--spacing-6); }
  .topo { grid-template-columns: 1fr; }
}
</style>
