package br.ufrn.imd.ecommerce.produto.adaptador.memoria;

import br.ufrn.imd.ecommerce.produto.adaptador.ProdutoRepositoryContrato;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;

class ProdutoRepositoryEmMemoriaTests extends ProdutoRepositoryContrato {
    @Override
    protected ProdutoRepository criarRepository() {
        return new ProdutoRepositoryEmMemoria();
    }
}
