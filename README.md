# E-Commerce DIM0517 2026

Sistema simples de controle de produtos e estoque de uma loja virtual, feito para a disciplina de Gerência de Configuração e Mudanças (DIM0517) da UFRN.

## Participantes

Emanuel Kywal Pinto Cabral Filho e Paulo Roberto Fernandes Holanda

## Backend

O backend é feito em Spring Boot 4 e usa o Java 25. Não é preciso instalar o Maven: o Maven Wrapper (`mvnw`) já vem no projeto. No Linux e no macOS, o `mvnw` precisa do `unzip` (no Ubuntu/WSL, `sudo apt install unzip`), porque confere o Maven baixado com o SHA-256 do `.zip`. Para subir a API:

```bash
cd backend/ecommerce
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run` no lugar de `./mvnw spring-boot:run` (os demais comandos abaixo seguem a mesma troca).

A API abre em `http://localhost:8080`, sem banco ou Docker. A primeira execução baixa o Maven e as dependências, então precisa de acesso à internet.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Outros comandos úteis: `./mvnw verify` compila e roda os testes, e `./mvnw javadoc:javadoc -Ddoclint=all` gera o Javadoc em `target/reports/apidocs/index.html`.

## Arquitetura

Cada pacote imediatamente abaixo de `br.ufrn.imd.ecommerce` representa um módulo do Spring Modulith. Os testes verificam as fronteiras entre `produto` e `compartilhado`, além das dependências entre camadas.

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
cd frontend
bun install
bun run dev
```

Também funciona com npm: `npm install` e `npm run dev` (os demais comandos abaixo valem com `npm run` no lugar de `bun run`).

A loja abre em `http://localhost:3000`.

As chamadas a `/loja/**` são repassadas pelo próprio servidor do front para a API em `http://localhost:8080`; para apontar para outro endereço, defina a variável `NUXT_API_BASE`.

Outros comandos úteis: `bun run lint` confere o padrão do código, `bun run test` roda os testes e `bun run build` gera a versão de produção em `.output/`.
