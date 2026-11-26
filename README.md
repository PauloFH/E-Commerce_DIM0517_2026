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
| [Docker](https://docs.docker.com/get-docker/) | com Compose v2 | `docker compose up` e os testes do adaptador PostgreSQL (Testcontainers) |

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
    postgres/       Implementação durável de ProdutoRepository (profile postgres)
  config/           Composição dos beans Spring
  estoque/         BaixaDeEstoque e ItemBaixado, contratos públicos para venda
  EstoqueEntrou e EstoqueSaiu  Eventos públicos de estoque
venda/
  dominio/excecao/  Exceção de negócio da venda
  aplicacao/
    dto/            Entradas com Jakarta Validation e resultado da venda
    servico/        VendaService: valida, agrupa por código e baixa o estoque de uma vez
  adaptador/api/    Interface Swagger, controller, DTOs HTTP e mapper MapStruct
  config/           Composição dos beans Spring
auditoria/
  AuditoriaListener  Consumidor dos eventos de estoque e venda
  registro/        Modelo e armazenamento dos movimentos
compartilhado/
  api/             Handler global e schema de erro do OpenAPI
  config/          Jackson, idioma e transações em memória
  evento/          Porta de publicação e adaptador Spring
  ExcecaoNegocio   Exceção-base de negócio
```

O serviço não depende de Spring, HTTP ou armazenamento concreto. Recebe `ProdutoRepository` e `jakarta.validation.Validator` no construtor e executa validações também em chamadas diretas, sem contexto Spring. Fora do Spring, `new ProdutoService(new ProdutoRepositoryEmMemoria())` usa o Validator padrão do Jakarta Validation.

Os DTOs HTTP são convertidos em entradas da aplicação pelo MapStruct; não são compartilhados com o serviço. Mappings HTTP e documentação Swagger ficam em `ProdutoApi`; `ProdutoController` implementa esse contrato. O domínio contém somente `codigo` (`Integer`), `preco` (`BigDecimal`) e `estoque` (`Integer`).

## Contrato HTTP

Código e preço devem ser positivos e a quantidade de entrada e de saída positiva. O cadastro recebe somente código e preço e sempre cria o produto com estoque zero. Todos os campos de cada requisição são obrigatórios. Valores monetários são expressos em reais; estoque e quantidade em unidades inteiras. Campos inteiros JSON não aceitam decimais, strings ou booleanos, mesmo strings numéricas como `"10"`; o `preco` também não aceita strings nem booleanos, como `"59.90"`.

| Operação | Entrada | Sucesso | Erros |
|---|---|---|---|
| `POST /loja/produto` | `codigo`, `preco` | 201 com `codigo`, `preco`, `estoque: 0` | 400, 409 |
| `GET /loja/produto/{codigo}/estoque` | Código na rota | 200 somente com `codigo`, `estoque` | 400, 404 |
| `PUT /loja/produto/{codigo}/entrada` | `quantidade` | 200 com `codigo`, `estoque` atualizado | 400, 404 |
| `PUT /loja/produto/{codigo}/saida` | `quantidade` | 200 com `codigo`, `estoque` atualizado | 400, 404 |
| `POST /loja/venda` | `itens`: lista de `codigo`, `quantidade` | 201 com `total` e `itens` (`codigo`, `quantidade`, `subtotal`, `estoque`) | 400, 404 |

```bash
curl -i http://localhost:8080/loja/produto \
  -H 'Content-Type: application/json' \
  -d '{"codigo":123,"preco":59.90}'

curl http://localhost:8080/loja/produto/123/estoque

curl -X PUT http://localhost:8080/loja/produto/123/entrada \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":10}'

curl -X PUT http://localhost:8080/loja/produto/123/saida \
  -H 'Content-Type: application/json' \
  -d '{"quantidade":10}'
```

```bash
curl -i http://localhost:8080/loja/venda \
  -H 'Content-Type: application/json' \
  -d '{"itens":[{"codigo":123,"quantidade":2},{"codigo":456,"quantidade":1}]}'
```

Com os produtos 123 (R$ 59,90, estoque 15) e 456 (R$ 69,90, estoque 8), a venda acima responde 201, sem `Location`:

```json
{"total":189.70,"itens":[{"codigo":123,"quantidade":2,"subtotal":119.80,"estoque":13},{"codigo":456,"quantidade":1,"subtotal":69.90,"estoque":7}]}
```

A venda baixa o estoque de cada item e calcula `subtotal = preço × quantidade` e `total = Σ subtotal`, em reais com duas casas (arredondamento `HALF_EVEN`). Ela é atômica: se qualquer item for inválido (lista ausente ou vazia, campo ausente, `"abc"`, `2.5`, quantidade ou código menor ou igual a zero), a quantidade passar do estoque (400, `produto.estoque.insuficiente`, com `argumentos` `[disponível, código]`) ou o produto não existir (404), nenhum estoque muda. Itens com o mesmo código têm as quantidades somadas e aparecem uma única vez na resposta, na ordem da primeira aparição, então o estoque nunca fica negativo. Erros de validação trazem `erros[].campo` com a linha, como `itens[1].quantidade`. A venda não é idempotente: repetir a chamada baixa o estoque novamente. O módulo `venda` usa somente `BaixaDeEstoque`, contrato público de `produto.estoque`; `ProdutoService` implementa essa interface e a baixa de todos os itens ocorre em uma única chamada ao repositório.

Reposição não é idempotente: repetir a chamada soma novamente. O limite de estoque é `2147483647`; ultrapassá-lo retorna 400 sem alteração. A saída também não é idempotente: repetir a chamada subtrai novamente. Quantidade maior que o estoque retorna 400 (`produto.estoque.insuficiente`) sem alteração, então o estoque nunca fica negativo; levar o estoque a zero é permitido. Código duplicado retorna 409 sem sobrescrever; código ausente retorna 404.

O `GlobalHttpExceptionHandler` centraliza os erros de todos os módulos. As exceções de domínio estendem `ExcecaoNegocio`, contrato público em Java puro de `compartilhado`, e o handler converte suas categorias em status HTTP sem acessar o interior de produto.

Erros usam `application/problem+json`, com `type: about:blank`, `title`, `status`, `detail` e `instance` (caminho da requisição). `title` e `detail` vêm traduzidos; `codigoErro` traz a chave estável da mensagem (por exemplo `produto.nao.encontrado`), para o cliente reagir sem depender do texto. Exceções de negócio incluem `argumentos`; validações incluem `erros`, ordenado por campo e chave, com `campo`, `chave` e `mensagem` (traduzida), sem publicar valores rejeitados. No OpenAPI esse formato aparece como o schema `Problema`. O cabeçalho `Accept-Language` seleciona português brasileiro (`pt-BR`) ou inglês (`en`); sem idioma compatível, a API usa português brasileiro. As mensagens ficam em `messages.properties` e `messages_en.properties`, e um teste garante que toda chave usada no código existe nos dois arquivos.

## Armazenamento

`ProdutoRepository` define consulta, inserção sem sobrescrita, soma atômica e baixa em lote. A baixa retorna um `ResultadoBaixa`: `Baixado` com os produtos atualizados, `ProdutoInexistente` ou `EstoqueInsuficiente` (com o estoque disponível). Se algum código não existe, esse resultado prevalece sobre estoque insuficiente; em ambos nada é alterado. Mapa vazio retorna `Baixado` sem itens. A porta recebe produtos válidos, códigos positivos, quantidades positivas e nenhum valor nulo.

Há dois adaptadores. O padrão é o **em memória**: usa um único monitor em todas as operações, inclusive leituras, impedindo observar uma baixa parcial, e substitui produtos imutáveis a cada atualização. Os dados são perdidos ao reiniciar e cada instância da API possui seu próprio estoque.

O adaptador **PostgreSQL** é ativado pelo profile `postgres` e guarda os dados de forma durável e compartilhada entre instâncias. A tabela `produto` (`codigo`, `preco`, `estoque`) é criada pelo Flyway (`src/main/resources/db/migration`) quando a API sobe, com `CHECK` para preço positivo e estoque não negativo. A consistência fica com o banco, sem lock na aplicação nem coluna de versão: a inserção usa `ON CONFLICT DO NOTHING`, a soma é um único `UPDATE ... RETURNING` (o estouro do `integer` vira `ArithmeticException`) e a baixa trava os produtos com `SELECT ... ORDER BY codigo FOR UPDATE` na mesma transação, o que evita deadlock entre baixas em ordem diferente, e só então decide o resultado na ordem do mapa recebido e atualiza. Em recusa nada é alterado.

Sem o profile, a API sobe sem datasource e não abre conexão com banco. A mesma suíte de testes (`ProdutoRepositoryContrato`) roda contra os dois adaptadores; o do PostgreSQL usa Testcontainers (`postgres:18-alpine`) e por isso exige o Docker rodando.

### Auditoria de estoque

Entradas, saídas e vendas concluídas publicam eventos de domínio. O módulo `auditoria` processa os eventos depois da confirmação da operação, registra código, quantidade em unidades, estoque resultante e instante em UTC, e escreve os movimentos no log em nível INFO. A venda gera um movimento por código, somando as quantidades repetidas. Operações recusadas não publicam eventos.

No perfil padrão, o histórico fica em memória e o processamento é de melhor esforço. Tanto o histórico quanto a entrega de eventos dependem da instância em execução.

No perfil `postgres`, o estoque e o registro da publicação (`event_publication`) são gravados na mesma transação. A auditoria grava todos os movimentos de uma operação em `movimento_estoque` e conclui a publicação em uma segunda transação. Se essa etapa falhar, a alteração do estoque continua confirmada e a publicação pendente é reenviada quando a aplicação reiniciar. As tabelas são criadas pela migration V2 do Flyway. O histórico está disponível no banco e no log.

### Docker Compose

```bash
docker compose up --build
```

Sobe o PostgreSQL, a API com o profile `postgres` e o front. O front fica em `http://localhost:3000` e a API em `http://localhost:8080`; os dados ficam no volume `dados-banco` e sobrevivem a reiniciar a API (`docker compose restart api`) e a `docker compose down`. Para apagar também os dados, use `docker compose down -v`.

Portas e credenciais, todas de desenvolvimento, podem ser trocadas por variáveis de ambiente:

| Variável | Padrão | Uso |
|---|---|---|
| `API_PORTA` | `8080` | Porta da API no host |
| `FRONT_PORTA` | `3000` | Porta do front no host |
| `BANCO_PORTA` | `5432` | Porta do PostgreSQL no host |
| `BANCO_USUARIO` | `kompraki` | Usuário do banco |
| `BANCO_SENHA` | `kompraki` | Senha do banco |

Para rodar a API fora do Docker contra um banco local, suba só o banco com `docker compose up -d banco`, exporte `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` com os valores de `BANCO_USUARIO` e `BANCO_SENHA` (os padrões estão no `docker-compose.yml`; o `application.properties` guarda só a URL local) e rode `SPRING_PROFILES_ACTIVE=postgres just api`. Venda, listagem, edição e exclusão não estão incluídas nesta entrega.

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
