package br.unicesumar.openbook.service;

import br.unicesumar.openbook.api.ApiException;
import br.unicesumar.openbook.api.Models.*;
import br.unicesumar.openbook.domain.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenBookService {
    private final JdbcTemplate db;
    private final int livro, apostila, equipamento;
    public OpenBookService(JdbcTemplate db, @Value("${openbook.prazo.livro}") int livro,
                           @Value("${openbook.prazo.apostila}") int apostila,
                           @Value("${openbook.prazo.equipamento}") int equipamento) {
        if (livro < 1 || apostila < 1 || equipamento < 1) throw new IllegalArgumentException("Os prazos devem ser positivos.");
        this.db = db; this.livro = livro; this.apostila = apostila; this.equipamento = equipamento;
    }
    private static String campo(String value, String nome, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) throw ApiException.invalido(nome + " é obrigatório e deve ter no máximo " + max + " caracteres.");
        return value.trim();
    }
    private static String opcional(String value, String nome, int max) {
        if (value == null || value.isBlank()) return null;
        return campo(value, nome, max);
    }
    private Usuario mapUsuario(ResultSet rs, int n) throws SQLException {
        return new Usuario(rs.getLong("id"), rs.getString("nome"), rs.getString("email"), rs.getString("curso"), rs.getString("telefone"));
    }
    private Item mapItem(ResultSet rs, int n) throws SQLException {
        return new Item(rs.getLong("id"), rs.getLong("usuario_id"), rs.getString("titulo"), rs.getString("tipo"), rs.getString("disciplina"), rs.getBoolean("disponivel"), rs.getString("numeracao"), rs.getString("edicao"), (Integer) rs.getObject("numero_paginas"), rs.getString("tipo_equipamento"), rs.getString("estado_conservacao"));
    }
    public List<Usuario> usuarios() { return db.query("SELECT * FROM usuarios ORDER BY id", this::mapUsuario); }
    public Usuario usuario(long id) { return db.query("SELECT * FROM usuarios WHERE id=?", this::mapUsuario, id).stream().findFirst().orElseThrow(() -> ApiException.ausente("Usuário não encontrado.")); }
    public Usuario salvarUsuario(Long id, Usuario u) {
        String nome = campo(u.nome(), "Nome", 150), email = campo(u.email(), "E-mail", 150), curso = campo(u.curso(), "Curso", 100), telefone = campo(u.telefone(), "Telefone", 30);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw ApiException.invalido("Informe um e-mail válido.");
        if (id == null) {
            Long novo = db.queryForObject("INSERT INTO usuarios(nome,email,curso,telefone) VALUES (?,?,?,?) RETURNING id", Long.class, nome, email, curso, telefone);
            return usuario(novo);
        }
        if (db.update("UPDATE usuarios SET nome=?,email=?,curso=?,telefone=? WHERE id=?", nome,email,curso,telefone,id) == 0) throw ApiException.ausente("Usuário não encontrado.");
        return usuario(id);
    }
    public void excluirUsuario(long id) { if (db.update("DELETE FROM usuarios WHERE id=?", id) == 0) throw ApiException.ausente("Usuário não encontrado."); }
    public List<Item> itens(String tipo, String disciplina, Boolean disponivel) {
        StringBuilder sql = new StringBuilder("SELECT * FROM itens WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (tipo != null && !tipo.isBlank()) { sql.append(" AND tipo=?"); args.add(tipo); }
        if (disciplina != null && !disciplina.isBlank()) { sql.append(" AND LOWER(disciplina) LIKE LOWER(?)"); args.add("%" + disciplina + "%"); }
        if (disponivel != null) { sql.append(" AND disponivel=?"); args.add(disponivel); }
        return db.query(sql.append(" ORDER BY id").toString(), this::mapItem, args.toArray());
    }
    public Item item(long id) { return db.query("SELECT * FROM itens WHERE id=?", this::mapItem, id).stream().findFirst().orElseThrow(() -> ApiException.ausente("Item não encontrado.")); }
    public Item salvarItem(Long id, Item i) {
        if (i.usuarioId() == null) throw ApiException.invalido("Informe o responsável pelo item.");
        usuario(i.usuarioId());
        String titulo = campo(i.titulo(), "Título", 150), tipo = campo(i.tipo(), "Tipo", 20), disciplina = campo(i.disciplina(), "Disciplina", 100);
        if (!Set.of("LIVRO", "APOSTILA", "EQUIPAMENTO").contains(tipo)) throw ApiException.invalido("Tipo de item inválido.");
        String numeracao = opcional(i.numeracao(), "Numeração", 40), edicao = opcional(i.edicao(), "Edição", 40);
        String tipoEquipamento = opcional(i.tipoEquipamento(), "Tipo de equipamento", 100), estado = opcional(i.estadoConservacao(), "Estado de conservação", 100);
        if (tipo.equals("LIVRO") && (numeracao == null || edicao == null)) throw ApiException.invalido("Informe numeração e edição do livro.");
        if (tipo.equals("APOSTILA") && (i.numeroPaginas() == null || i.numeroPaginas() < 1)) throw ApiException.invalido("Informe um número positivo de páginas.");
        if (tipo.equals("EQUIPAMENTO") && (tipoEquipamento == null || estado == null)) throw ApiException.invalido("Informe tipo e estado do equipamento.");
        if (id == null) {
            Long novo = db.queryForObject("INSERT INTO itens(usuario_id,titulo,tipo,disciplina,numeracao,edicao,numero_paginas,tipo_equipamento,estado_conservacao) VALUES (?,?,?,?,?,?,?,?,?) RETURNING id", Long.class, i.usuarioId(),titulo,tipo,disciplina,numeracao,edicao,i.numeroPaginas(),tipoEquipamento,estado);
            return item(novo);
        }
        if (db.update("UPDATE itens SET usuario_id=?,titulo=?,tipo=?,disciplina=?,numeracao=?,edicao=?,numero_paginas=?,tipo_equipamento=?,estado_conservacao=? WHERE id=?", i.usuarioId(),titulo,tipo,disciplina,numeracao,edicao,i.numeroPaginas(),tipoEquipamento,estado,id) == 0) throw ApiException.ausente("Item não encontrado.");
        return item(id);
    }
    public void excluirItem(long id) { if (db.update("DELETE FROM itens WHERE id=?", id) == 0) throw ApiException.ausente("Item não encontrado."); }
    private List<Evento> historico(long id) { return db.query("SELECT * FROM historico_movimentacoes WHERE movimentacao_id=? ORDER BY id", (rs,n) -> new Evento(rs.getLong("id"),rs.getString("status"),rs.getTimestamp("registrado_em").toLocalDateTime()), id); }
    private Movimentacao movimento(ResultSet rs, int n) throws SQLException {
        long id = rs.getLong("id");
        return new Movimentacao(id,rs.getLong("usuario_id"),rs.getLong("item_id"),rs.getString("tipo"),rs.getString("status"),rs.getObject("data_solicitacao", LocalDate.class),rs.getObject("prazo_devolucao", LocalDate.class),historico(id));
    }
    public List<Movimentacao> movimentacoes() { return db.query("SELECT * FROM movimentacoes ORDER BY id DESC", this::movimento); }
    public UsuarioComMovimentacoes movimentacoesUsuario(long id) {
        UsuarioComMovimentacoes resultado = new UsuarioComMovimentacoes(usuario(id));
        db.query("SELECT * FROM movimentacoes WHERE usuario_id=? ORDER BY id DESC", this::movimento, id).forEach(resultado::adicionar);
        return resultado;
    }
    public Movimentacao movimentacao(long id) { return db.query("SELECT * FROM movimentacoes WHERE id=?", this::movimento, id).stream().findFirst().orElseThrow(() -> ApiException.ausente("Movimentação não encontrada.")); }
    @Transactional
    public Movimentacao criarMovimentacao(NovaMovimentacao m) {
        if (m.usuarioId() == null || m.itemId() == null) throw ApiException.invalido("Informe usuário e item.");
        if (m.tipo() == null || !Set.of("EMPRESTIMO", "DOACAO").contains(m.tipo())) throw ApiException.invalido("Tipo de movimentação inválido.");
        usuario(m.usuarioId());
        Item i = db.query("SELECT * FROM itens WHERE id=? FOR UPDATE", this::mapItem, m.itemId()).stream().findFirst().orElseThrow(() -> ApiException.ausente("Item não encontrado."));
        if (!i.disponivel()) throw ApiException.conflito("O item não está disponível.");
        LocalDate hoje = LocalDate.now();
        ItemAcademico modelo = switch (i.tipo()) { case "LIVRO" -> new LivroDidatico(livro); case "APOSTILA" -> new Apostila(apostila); default -> new EquipamentoApoio(equipamento); };
        boolean emprestimo = m.tipo().equals("EMPRESTIMO");
        String status = emprestimo ? "EMPRESTADO" : "DOADO";
        Long id = db.queryForObject("INSERT INTO movimentacoes(usuario_id,item_id,tipo,status,data_solicitacao,prazo_devolucao) VALUES (?,?,?,?,?,?) RETURNING id", Long.class, m.usuarioId(),m.itemId(),m.tipo(),status,hoje,emprestimo ? modelo.calcularPrazoDevolucao(hoje) : null);
        db.update("UPDATE itens SET disponivel=FALSE WHERE id=?", i.id());
        db.update("INSERT INTO historico_movimentacoes(movimentacao_id,status) VALUES (?,?)", id,status);
        return movimentacao(id);
    }
    @Transactional
    public Movimentacao atualizarStatus(long id, String status) {
        Movimentacao m = db.query("SELECT * FROM movimentacoes WHERE id=? FOR UPDATE", this::movimento, id).stream().findFirst().orElseThrow(() -> ApiException.ausente("Movimentação não encontrada."));
        if (!m.tipo().equals("EMPRESTIMO") || !Set.of("EMPRESTADO", "ATRASADO").contains(m.status()) || !("DEVOLVIDO".equals(status) || ("ATRASADO".equals(status) && m.status().equals("EMPRESTADO")))) throw ApiException.conflito("Transição de status não permitida.");
        db.update("UPDATE movimentacoes SET status=? WHERE id=?", status,id);
        db.update("INSERT INTO historico_movimentacoes(movimentacao_id,status) VALUES (?,?)", id,status);
        if (status.equals("DEVOLVIDO")) db.update("UPDATE itens SET disponivel=TRUE WHERE id=?", m.itemId());
        return movimentacao(id);
    }
}
