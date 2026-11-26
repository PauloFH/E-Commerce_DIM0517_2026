package br.ufrn.imd.ecommerce.auditoria.registro;

import java.util.List;

/** Armazena todos os movimentos de uma operação como uma unidade. */
public interface Movimentos {
    void registrar(List<MovimentoEstoque> movimentos);
}
