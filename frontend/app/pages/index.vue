<script setup lang="ts">
const { t } = useI18n()
const { produtos } = useProdutosConhecidos()

useHead({ title: () => `${t('inicio.seo')} · kompraki` })
</script>

<template>
  <div class="pagina">
    <section class="banner">
      <h1 class="titulo">
        {{ t('inicio.titulo1') }}<br>{{ t('inicio.titulo2') }}
      </h1>
      <p class="texto">
        {{ t('inicio.texto') }}
      </p>
      <KButton
        variante="comprar"
        class="cta"
        @click="focarBusca"
      >
        {{ t('inicio.buscar') }}
      </KButton>

      <svg
        class="grafismo"
        viewBox="0 0 64 64"
        fill="none"
        aria-hidden="true"
        focusable="false"
      >
        <path
          d="M32 3C17.6 3 7 13.8 7 27.6 7 45.2 32 61 32 61s25-15.8 25-33.4C57 13.8 46.4 3 32 3Z"
          fill="#C6F432"
        />
        <path
          d="M25 15v24M39.5 16 26.5 29M30 26.5l10.5 12.5"
          stroke="#2541B2"
          stroke-width="5.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </section>

    <BenefitsStrip />

    <section
      id="vistos"
      aria-labelledby="vistos-titulo"
    >
      <div class="cabecalho-secao">
        <div>
          <h2
            id="vistos-titulo"
            class="heading-md"
          >
            {{ t('inicio.vistosTitulo') }}
          </h2>
          <p class="caption legenda">
            {{ t('inicio.vistosLegenda') }}
          </p>
        </div>
        <NuxtLink
          v-if="produtos.length"
          to="/vendedor"
          class="label ver-todos"
        >{{ t('inicio.verTodos') }}</NuxtLink>
      </div>

      <ul
        v-if="produtos.length"
        class="grade"
      >
        <li
          v-for="p in produtos"
          :key="p.codigo"
        >
          <ProductCard
            :codigo="p.codigo"
            :preco="p.preco"
            :estoque="p.estoque"
          />
        </li>
      </ul>

      <div
        v-else
        class="vazio"
      >
        <span class="vazio-icone"><KIcon
          nome="estoque"
          :tamanho="32"
        /></span>
        <h3 class="heading-sm">
          {{ t('inicio.vazioTitulo') }}
        </h3>
        <p class="body">
          {{ t('inicio.vazioTexto') }}
        </p>
        <KButton
          variante="primario"
          @click="focarBusca"
        >
          {{ t('inicio.vazioBotao') }}
        </KButton>
      </div>
    </section>
  </div>
</template>

<style scoped>
.pagina {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-12);
}

.banner {
  position: relative;
  overflow: hidden;
  padding: 40px 56px;
  border-radius: var(--radius-md);
  background: var(--color-bg-brand);
  color: var(--color-text-on-brand);
}

.titulo {
  font-size: 44px;
  line-height: 52px;
  font-weight: 800;
}

.texto {
  max-width: 560px;
  margin: var(--spacing-4) 0 var(--spacing-6);
}

/* o pino fica parcialmente fora do banner, cortado pela borda direita */
.grafismo {
  position: absolute;
  top: 50%;
  right: -137px;
  width: 360px;
  height: 360px;
  transform: translateY(-50%);
  pointer-events: none;
}

.cabecalho-secao {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-4);
}

.legenda { color: var(--color-text-secondary); }
.ver-todos { color: var(--color-text-brand); white-space: nowrap; }

.grade {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--spacing-4);
}

.vazio {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-3);
  padding: var(--spacing-12) var(--spacing-6);
  border: 1px dashed var(--color-border-field);
  border-radius: var(--radius-md);
  background: var(--color-bg-surface);
  text-align: center;
}

.vazio .body { max-width: 440px; color: var(--color-text-secondary); }

.vazio-icone {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background: var(--color-bg-info);
  color: var(--color-text-brand);
}

@media (max-width: 767px) {
  .pagina { gap: var(--spacing-8); }
  .banner { padding: 28px 24px; }
  .titulo { font-size: 28px; line-height: 36px; padding-right: 110px; }
  .cta { width: 100%; }

  .grafismo {
    top: -40px;
    right: -50px;
    width: 130px;
    height: 130px;
    transform: none;
  }

  .texto { position: relative; }

  .grade {
    display: flex;
    gap: var(--spacing-3);
    margin-inline: calc(var(--spacing-4) * -1);
    padding: 0 var(--spacing-4) var(--spacing-2);
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    scroll-padding-inline: var(--spacing-4);
  }

  .grade li { flex: 0 0 200px; scroll-snap-align: start; }
}
</style>
