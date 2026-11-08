package br.unicesumar.openbook;

import br.unicesumar.openbook.domain.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrazosTest {
    @Test void calculaPrazoPorPolimorfismo() {
        LocalDate data = LocalDate.of(2026, 12, 25);
        ItemAcademico[] itens = { new LivroDidatico(14), new Apostila(14), new EquipamentoApoio(7) };
        assertEquals(LocalDate.of(2027, 1, 8), itens[0].calcularPrazoDevolucao(data));
        assertEquals(LocalDate.of(2027, 1, 8), itens[1].calcularPrazoDevolucao(data));
        assertEquals(LocalDate.of(2027, 1, 1), itens[2].calcularPrazoDevolucao(data));
    }
}
