package br.unicesumar.openbook.domain;
public class LivroDidatico extends ItemAcademico {
    private final int prazo;
    public LivroDidatico(int prazo) { this.prazo = prazo; }
    @Override public int prazoEmDias() { return prazo; }
}
