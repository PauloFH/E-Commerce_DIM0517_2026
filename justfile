set windows-shell := ["powershell.exe", "-NoLogo", "-Command"]

mvnw := if os_family() == "windows" { ".\\mvnw.cmd" } else { "./mvnw" }

# lista os comandos
default:
    @just --list --unsorted

# instala as dependências do front e ativa o hook de pre-commit
[working-directory: 'frontend']
instalar *opcoes:
    bun install {{ opcoes }}
    git config core.hooksPath .githooks

# sobe a API em http://localhost:8080
[working-directory: 'backend/ecommerce']
api:
    {{ mvnw }} spring-boot:run

# sobe o front em http://localhost:3000
[working-directory: 'frontend']
front:
    bun run dev

# formata o back (Spotless) e o front (ESLint)
formatar: formatar-back formatar-front

# formata só o back
[working-directory: 'backend/ecommerce']
formatar-back:
    {{ mvnw }} spotless:apply

# formata só o front
[working-directory: 'frontend']
formatar-front:
    bun run lint:fix

# roda tudo o que o CI roda
verificar: verificar-back verificar-front

# formatação, testes, arquitetura e cobertura do back
[working-directory: 'backend/ecommerce']
verificar-back:
    {{ mvnw }} verify

# lint, tipos, testes e build do front
[working-directory: 'frontend']
verificar-front:
    bun run lint
    bun run typecheck
    bun run test
    bun run build

# testes do back e do front
testar: testar-back testar-front

# testes do back
[working-directory: 'backend/ecommerce']
testar-back:
    {{ mvnw }} test

# testes do front
[working-directory: 'frontend']
testar-front:
    bun run test
