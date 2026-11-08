package br.unicesumar.openbook.domain;
public class Apostila extends ItemAcademico {
    private final int prazo;
    public Apostila(int prazo) { this.prazo = prazo; }
    @Override public int prazoEmDias() { return prazo; }
}
