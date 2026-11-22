# E-Commerce DIM0517 2026
Sistema simples de controle de produtos e estoque de uma loja virtual, feito para a disciplina de Gerência de Configuração e Mudanças (DIM0517) da UFRN.

## Participantes
 
Emanuel Kywal Pinto Cabral Filho e Paulo Roberto Fernandes Holanda

## Como rodar

O backend é Java 25 com Spring Boot 4 e fica em `backend/`. O front fica em `frontend/`.

### Frontend

O front é feito em Nuxt 4 e usa o [Bun](https://bun.sh) 1.4. Para subir em modo de desenvolvimento:

```bash
cd frontend
bun install
bun run dev
```

Também funciona com npm: `npm install` e `npm run dev` (os demais comandos abaixo valem com `npm run` no lugar de `bun run`).

A loja abre em `http://localhost:3000`.

As chamadas a `/loja/**` são repassadas pelo próprio servidor do front para a API em `http://localhost:8080`; para apontar para outro endereço, defina a variável `NUXT_API_BASE`.

Outros comandos úteis: `bun run lint` confere o padrão do código, `bun run test` roda os testes e `bun run build` gera a versão de produção em `.output/`.

