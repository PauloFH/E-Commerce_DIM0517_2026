package br.ufrn.imd.ecommerce.venda.aplicacao.servico;

import br.ufrn.imd.ecommerce.compartilhado.evento.PublicadorDeEventos;
import br.ufrn.imd.ecommerce.produto.estoque.BaixaDeEstoque;
import br.ufrn.imd.ecommerce.produto.estoque.ItemBaixado;
import br.ufrn.imd.ecommerce.venda.VendaRealizada;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVenda;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVendido;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ResultadoVenda;
import br.ufrn.imd.ecommerce.venda.dominio.excecao.VendaInvalidaException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Casos de uso de venda, independentes de Spring e do armazenamento de produtos. */
public final class VendaService {
    private final BaixaDeEstoque baixaDeEstoque;
    private final Validator validator;
    private final PublicadorDeEventos publicador;

    /**
     * Cria o serviço fora do Spring com o Validator padrão do Jakarta Validation, compartilhado por
     * todas as instâncias criadas assim.
     *
     * @param baixaDeEstoque API pública do módulo produto
     */
    public VendaService(BaixaDeEstoque baixaDeEstoque) {
        this(baixaDeEstoque, ValidatorPadrao.INSTANCIA);
    }

    /** Uma única factory por processo, criada só no primeiro uso do construtor sem Validator. */
    private static final class ValidatorPadrao {
        private static final Validator INSTANCIA =
                Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Recebe as dependências usadas também em chamadas fora do Spring.
     *
     * @param baixaDeEstoque API pública do módulo produto
     * @param validator executor das restrições declaradas nos DTOs
     */
    public VendaService(BaixaDeEstoque baixaDeEstoque, Validator validator) {
        this(baixaDeEstoque, validator, evento -> {});
    }

    /** Cria o serviço com publicador de eventos. */
    public VendaService(
            BaixaDeEstoque baixaDeEstoque, Validator validator, PublicadorDeEventos publicador) {
        this.baixaDeEstoque = Objects.requireNonNull(baixaDeEstoque);
        this.validator = Objects.requireNonNull(validator);
        this.publicador = Objects.requireNonNull(publicador);
    }

    /** Lista de itens validada como um todo, para os erros apontarem a linha, como itens[1]. */
    private record Pedido(
            @NotEmpty(message = "venda.itens.obrigatorios")
                    List<@NotNull(message = "venda.item.obrigatorio") @Valid ItemVenda> itens) {}

    /**
     * Baixa o estoque de todos os itens e calcula o total. É atômica: se qualquer item for
     * inválido, inexistente ou sem estoque, nenhum estoque muda. Códigos repetidos têm as
     * quantidades somadas.
     *
     * @param itens códigos e quantidades da venda
     * @return total e um item por código, na ordem da primeira aparição
     * @throws ConstraintViolationException se a lista for vazia ou algum item for inválido
     * @throws VendaInvalidaException se a soma de um código ultrapassar o limite inteiro
     */
    public ResultadoVenda vender(List<ItemVenda> itens) {
        var violacoes = validator.validate(new Pedido(itens));

        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }

        Map<Integer, Integer> quantidades = new LinkedHashMap<>();

        for (ItemVenda item : itens) {
            try {
                quantidades.merge(item.codigo(), item.quantidade(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new VendaInvalidaException("venda.quantidade.limite.excedido", item.codigo());
            }
        }

        Map<Integer, ItemBaixado> baixados = baixaDeEstoque.baixarItens(quantidades);
        List<ItemVendido> vendidos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        for (var quantidade : quantidades.entrySet()) {
            ItemBaixado baixado = baixados.get(quantidade.getKey());
            BigDecimal subtotal =
                    baixado.preco()
                            .multiply(BigDecimal.valueOf(quantidade.getValue()))
                            .setScale(2, RoundingMode.HALF_EVEN);
            total = total.add(subtotal);
            vendidos.add(
                    new ItemVendido(
                            quantidade.getKey(),
                            quantidade.getValue(),
                            subtotal,
                            baixado.estoque()));
        }

        publicador.publicar(
                new VendaRealizada(
                        total,
                        vendidos.stream()
                                .map(item ->
                                        new VendaRealizada.Item(
                                                item.codigo(), item.quantidade(), item.estoque()))
                                .toList()));
        return new ResultadoVenda(total, List.copyOf(vendidos));
    }
}
