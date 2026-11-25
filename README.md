# E-Commerce DIM0517 2026

[![CI](https://github.com/PauloFH/E-Commerce_DIM0517_2026/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/PauloFH/E-Commerce_DIM0517_2026/actions/workflows/ci.yml)

Sistema simples de controle de produtos e estoque de uma loja virtual, feito para a disciplina de Gerência de Configuração e Mudanças (DIM0517) da UFRN.

## Participantes

Emanuel Kywal Pinto Cabral Filho e Paulo Roberto Fernandes Holanda

## Ferramentas necessárias

| Ferramenta | Versão | Para quê |
|---|---|---|
| [Git](https://git-scm.com) | qualquer recente | Versionamento e hook de pre-commit |
| [mise](https://mise.jdx.dev) | qualquer recente | Instala as versões abaixo a partir do `mise.toml` (recomendado) |
| JDK ([Temurin](https://adoptium.net)) | 25 | Backend |
| [Node.js](https://nodejs.org) | 24 | Nuxt e Vitest |
| [Bun](https://bun.sh) | 1.4.5 | Dependências e scripts do front |
| [just](https://just.systems) | 1.59 | Comandos do projeto |
| `unzip` | qualquer | O `mvnw` confere o Maven baixado (Linux, macOS e WSL) |

Com o mise, depois de clonar:

```bash
mise trust
mise install
just instalar
```

Sem o mise, instale o JDK 25, o Node 24, o Bun 1.4.5 e o just pelo gerenciador do seu sistema, e deixe o JDK 25 no `JAVA_HOME` (o `mvnw` usa o Java de lá). O Maven não precisa ser instalado: o Maven Wrapper já vem no projeto.

## Comandos

Os comandos do dia a dia estão no `justfile` da raiz. O CI usa os mesmos comandos.

| Comando | O que faz |
|---|---|
| `just` | Lista os comandos |
| `just instalar` | Instala as dependências do front e ativa o hook de pre-commit (rode uma vez depois de clonar) |
| `just api` | Sobe a API em `http://localhost:8080` |
| `just front` | Sobe o front em `http://localhost:3000` |
| `just formatar` | Formata back e front (`formatar-back` e `formatar-front` fazem um lado só) |
| `just verificar` | Roda o mesmo que o CI (`verificar-back` e `verificar-front` fazem um lado só) |
| `just testar` | Roda os testes do back e do front (`testar-back` e `testar-front` fazem um lado só) |

## Backend

O backend é feito em Spring Boot 4 e usa o Java 25; no Windows, o `just` chama o `mvnw.cmd`. Para subir a API:

```bash
just api
```

A API abre em `http://localhost:8080`, sem banco ou Docker. A primeira execução baixa o Maven e as dependências, então precisa de acesso à internet.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Outros comandos úteis: `just verificar-back` compila, roda os testes e confere a formatação e a cobertura mínima; `just formatar-back` formata o código; e `./mvnw javadoc:javadoc -Ddoclint=all`, dentro de `backend/ecommerce`, gera o Javadoc em `target/reports/apidocs/index.html`.

## Arquitetura

Cada pacote imediatamente abaixo de `br.ufrn.imd.ecommerce` representa um módulo do Spring Modulith. Os testes verificam as fronteiras entre os módulos e as dependências entre camadas (ver [Padrões do projeto](#padrões-do-projeto)).

```text
produto/
  dominio/
    modelo/         Produto imutável; somente Java
    excecao/        Exceções de negócio com chaves e argumentos
  aplicacao/
    dto/            Entradas com Jakarta Validation
    porta/          Contrato ProdutoRepository
    servico/        Casos de uso de produto
  adaptador/
    api/            Interface Swagger e controller
      dto/
        requisicao/ Corpos HTTP de entrada
        resposta/   Representações HTTP de saída
      mapper/       Conversões MapStruct
    memoria/        Implementação volátil de ProdutoRepository
  config/           Composição dos beans Spring
compartilhado/      Exceção-base de negócio, Jackson, handler global e schema de erro do OpenAPI
```

O serviço não depende de Spring, HTTP ou armazenamento concreto. Recebe `ProdutoRepository` e `jakarta.validation.Validator` no construtor e executa validações também em chamadas diretas, sem contexto Spring. Fora do Spring, `new ProdutoService(new ProdutoRepositoryEmMemoria())` usa o Validator padrão do Jakarta Validation.

Os DTOs HTTP são convertidos em entradas da aplicação pelo MapStruct; não são compartilhados com o serviço. Mappings HTTP e documentação Swagger ficam em `ProdutoApi`; `ProdutoController` implementa esse contrato. O domínio contém somente `codigo` (`Integer`), `preco` (`BigDecimal`) e `estoque` (`Integer`).

## Contrato HTTP

Código e preço devem ser positivos e a quantidade de reposição positiva. O cadastro recebe somente código e preço e sempre cria o produto com estoque zero. Todos os campos de cada requisição são obrigatórios. Valores monetários são expressos em reais; estoque e quantidade em unidades inteiras. Campos inteiros JSON não aceitam decimais, strings ou booleanos, mesmo strings numéricas como `"10"`; o `preco` também não aceita strings nem booleanos, como `"59.90"`.

| Operação | Entrada | Sucesso | Erros |
|---|---|---|---|
| `POST /loja/produto` | `codigo`, `preco` | 201 com `codigo`, `preco`, `estoque: 0` | 400, 409 |
| `GET /loja/produto/{codigo}/estoque` | Código na rota | 200 somente com `codigo`, `estoque` | 400, 404 |
| `PUT /loja/produto/{codigo}/entrada` | `quantidade` | 200 com `codigo`, `estoque` atualizado | 400, 404 |

```bash
curl -i http://localhost:8080/loja/produto \
  -H 'Content-Type: application/json' \
  -d '{"codigo":123,"preco":59.90}'

curl http://localhost:8080/loja/produto/123/estoque

curl -X PUT http://localhost:8080/loja/produto/123/entrada \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":10}'
```

Reposição não é idempotente: repetir a chamada soma novamente. O limite de estoque é `2147483647`; ultrapassá-lo retorna 400 sem alteração. Código duplicado retorna 409 sem sobrescrever; código ausente retorna 404.

O `GlobalHttpExceptionHandler` centraliza os erros de todos os módulos. As exceções de domínio estendem `ExcecaoNegocio`, contrato público em Java puro de `compartilhado`, e o handler converte suas categorias em status HTTP sem acessar o interior de produto.

Erros usam `application/problem+json`, com `type: about:blank`, `title`, `status`, `detail` (chave de mensagem) e `instance` (caminho da requisição). Exceções de negócio incluem `argumentos`; validações incluem `erros` com `campo` e `chave`, sem publicar valores rejeitados. No OpenAPI esse formato aparece como o schema `Problema`. A tradução das chaves fica para uma entrega futura.

## Armazenamento

`ProdutoRepository` define consulta, inserção sem sobrescrita, soma atômica e baixa em lote. Na baixa, produto ausente ou estoque insuficiente retorna `false` sem alterar nenhum item; mapa vazio retorna `true`. A porta recebe produtos válidos, códigos positivos, quantidades positivas e nenhum valor nulo.

O adaptador em memória usa um único monitor em todas as operações, inclusive leituras, impedindo observar uma baixa parcial. Atualizações substituem produtos imutáveis. Um futuro adaptador de banco deverá preservar os mesmos contratos e garantir atomicidade entre instâncias.

**Os dados são perdidos ao reiniciar. Cada instância da API possui seu próprio estoque.** Venda, persistência durável, listagem, edição e exclusão não estão incluídas nesta entrega.

## Frontend

O front é feito em Nuxt 4 e usa o [Bun](https://bun.sh) 1.4. Para subir em modo de desenvolvimento:

```bash
just instalar
just front
```

Sem o Bun, `npm install` e `npm run dev` dentro de `frontend/` também funcionam.

A loja abre em `http://localhost:3000`.

As chamadas a `/loja/**` são repassadas pelo próprio servidor do front para a API em `http://localhost:8080`; para apontar para outro endereço, defina a variável `NUXT_API_BASE`.

Outros comandos úteis: `just verificar-front` roda o lint (padrão e formatação), a checagem de tipos, os testes e o build de produção em `frontend/.output/`; `just formatar-front` corrige o que o lint consegue.

## Padrões do projeto

O CI (`.github/workflows/ci.yml`) roda em todo push e PR para `develop`, `main` e `release/**`. Antes de abrir um PR, `just verificar` roda localmente as mesmas checagens.

- **Camadas:** em cada módulo, `dominio` e `aplicacao` são Java puro e não conhecem os adaptadores; o `adaptador.api` fala só com o service, nunca com o repositório. Um módulo só usa o que outro expõe. Quem garante isso são o ArchUnit e o Spring Modulith, no `ArquiteturaTests`.
- **Cobertura:** cada pacote de `dominio` e `aplicacao` precisa de 80% de linhas e de ramos cobertos (JaCoCo). O relatório fica em `backend/ecommerce/target/site/jacoco/index.html` e também é publicado pelo CI.
- **Documentação dos módulos:** o `Documenter` do Modulith gera os diagramas C4/PlantUML e o canvas de cada módulo em `backend/ecommerce/target/spring-modulith-docs`, publicados pelo CI.
- **Formatação:** Java no google-java-format (estilo AOSP, 4 espaços, imports sem `*`), conferido pelo Spotless; front no ESLint Stylistic; o resto segue o `.editorconfig`.
- **Hook de pre-commit:** formata os arquivos do commit e barra o que não tem correção automática. O `just instalar` ativa o hook.
- **Branches:** GitFlow com `main`, `develop`, `feature/<nº>-...`, `fix/<nº>-...` e `release/...`, sempre com o número da issue no nome, e nenhuma branch é apagada.
- **Commits:** `gitmoji tipo(escopo): descrição (#nº)`, com escopo `backend` ou `frontend` quando a mudança é de um lado só. Exemplo: `:sparkles: feat(backend): saída de estoque (#8)`.
