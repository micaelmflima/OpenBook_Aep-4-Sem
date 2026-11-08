package br.unicesumar.openbook.domain;

import br.unicesumar.openbook.api.Models.Movimentacao;
import br.unicesumar.openbook.api.Models.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioComMovimentacoes {
    private final Usuario usuario;
    private final List<Movimentacao> movimentacoes = new ArrayList<>();
    public UsuarioComMovimentacoes(Usuario usuario) { this.usuario = usuario; }
    public void adicionar(Movimentacao movimentacao) {
        if (!usuario.id().equals(movimentacao.usuarioId())) throw new IllegalArgumentException("Movimentação pertence a outro usuário.");
        movimentacoes.add(movimentacao);
    }
    public Usuario getUsuario() { return usuario; }
    public List<Movimentacao> getMovimentacoes() { return List.copyOf(movimentacoes); }
}
