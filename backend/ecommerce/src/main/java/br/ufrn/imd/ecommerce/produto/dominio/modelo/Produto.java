package br.ufrn.imd.ecommerce.produto.dominio.modelo;

import java.math.BigDecimal;

/** Produto imutável; preço em reais e estoque em unidades inteiras. */
public final class Produto {
    private final Integer codigo;
    private final BigDecimal preco;
    private final Integer estoque;

    /**
     * Constrói um retrato do produto com valores previamente validados pela aplicação.
     *
     * @param codigo identificador do produto
     * @param preco preço em reais
     * @param estoque quantidade em unidades
     */
    public Produto(Integer codigo, BigDecimal preco, Integer estoque) {
        this.codigo = codigo;
        this.preco = preco;
        this.estoque = estoque;
    }

    /**
     * @return identificador do produto
     */
    public Integer getCodigo() {
        return codigo;
    }

    /**
     * @return preço em reais
     */
    public BigDecimal getPreco() {
        return preco;
    }

    /**
     * @return quantidade em unidades
     */
    public Integer getEstoque() {
        return estoque;
    }
}
