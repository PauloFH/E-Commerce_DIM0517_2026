// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: ['@nuxt/fonts', '@nuxt/eslint', '@nuxt/test-utils/module', '@nuxt/hints', '@nuxt/a11y', '@nuxtjs/i18n'],
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  css: ['~/assets/css/tokens.css'],
  // URL da API: NUXT_API_BASE ou, sem ela, o backend local
  runtimeConfig: {
    apiBase: process.env.NUXT_API_BASE || 'http://localhost:8080'
  },
  // fonte baixada no build e servida pelo próprio site, sem <link> para o Google
  fonts: {
    families: [
      { name: 'Plus Jakarta Sans', provider: 'google', weights: [400, 500, 600, 700, 800] }
    ]
  },
  i18n: {
    strategy: 'no_prefix',
    defaultLocale: 'pt-BR',
    locales: [
      { code: 'pt-BR', language: 'pt-BR', name: 'Português (Brasil)', file: 'pt-BR.json' },
      { code: 'en', language: 'en', name: 'English', file: 'en.json' }
    ],
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'kompraki_idioma',
      fallbackLocale: 'pt-BR'
    }
  }
})
