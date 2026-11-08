package br.unicesumar.openbook.domain;
public class EquipamentoApoio extends ItemAcademico {
    private final int prazo;
    public EquipamentoApoio(int prazo) { this.prazo = prazo; }
    @Override public int prazoEmDias() { return prazo; }
}
