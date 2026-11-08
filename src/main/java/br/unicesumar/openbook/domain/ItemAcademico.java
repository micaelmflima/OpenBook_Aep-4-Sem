package br.unicesumar.openbook.domain;
import java.time.LocalDate;
public abstract class ItemAcademico {
    public abstract int prazoEmDias();
    public LocalDate calcularPrazoDevolucao(LocalDate data) { return data.plusDays(prazoEmDias()); }
}
