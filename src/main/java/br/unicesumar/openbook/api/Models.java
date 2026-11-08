package br.unicesumar.openbook.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Models {
    private Models() {}
    public record Usuario(Long id, String nome, String email, String curso, String telefone) {}
    public record Item(Long id, Long usuarioId, String titulo, String tipo, String disciplina,
                       Boolean disponivel, String numeracao, String edicao, Integer numeroPaginas,
                       String tipoEquipamento, String estadoConservacao) {}
    public record NovaMovimentacao(Long usuarioId, Long itemId, String tipo) {}
    public record AtualizacaoStatus(String status) {}
    public record Evento(Long id, String status, LocalDateTime registradoEm) {}
    public record Movimentacao(Long id, Long usuarioId, Long itemId, String tipo, String status,
                               LocalDate dataSolicitacao, LocalDate prazoDevolucao, List<Evento> historico) {}
}
