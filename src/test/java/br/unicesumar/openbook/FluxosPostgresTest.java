package br.unicesumar.openbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "OPENBOOK_TEST_DB", matches = "true")
class FluxosPostgresTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private JsonNode criar(String url, String body) throws Exception {
        MvcResult r = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }
    private void alterar(long id, String status, int esperado) throws Exception {
        mvc.perform(patch("/api/movimentacoes/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(esperado));
    }
    @Test void crudMovimentacoesEIntegridade() throws Exception {
        long usuario = criar("/api/usuarios", "{\"nome\":\"Teste\",\"email\":\"teste@example.edu.br\",\"curso\":\"Computacao\",\"telefone\":\"123\"}").get("id").asLong();
        long item = criar("/api/itens", "{\"usuarioId\":"+usuario+",\"titulo\":\"Algoritmos\",\"tipo\":\"LIVRO\",\"disciplina\":\"Codigo\",\"numeracao\":\"1\",\"edicao\":\"2\"}").get("id").asLong();
        mvc.perform(put("/api/itens/" + item).contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":"+usuario+",\"titulo\":\"Algoritmos 2\",\"tipo\":\"LIVRO\",\"disciplina\":\"Codigo\",\"numeracao\":\"1\",\"edicao\":\"2\"}")).andExpect(status().isOk());
        JsonNode filtrados = json.readTree(mvc.perform(get("/api/itens").param("tipo", "LIVRO").param("disciplina", "codi").param("disponivel", "true")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, filtrados.size());
        assertEquals(item, filtrados.get(0).get("id").asLong());
        String corpo = "{\"usuarioId\":"+usuario+",\"itemId\":"+item+",\"tipo\":\"EMPRESTIMO\"}";
        JsonNode mov = criar("/api/movimentacoes", corpo);
        assertEquals(LocalDate.now().plusDays(14).toString(), mov.get("prazoDevolucao").asText());
        mvc.perform(post("/api/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isConflict());
        alterar(mov.get("id").asLong(), "ATRASADO", 200);
        alterar(mov.get("id").asLong(), "DEVOLVIDO", 200);
        alterar(mov.get("id").asLong(), "DEVOLVIDO", 409);
        JsonNode lido = json.readTree(mvc.perform(get("/api/movimentacoes/" + mov.get("id").asLong())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(3, lido.get("historico").size());
        JsonNode doacao = criar("/api/movimentacoes", "{\"usuarioId\":"+usuario+",\"itemId\":"+item+",\"tipo\":\"DOACAO\"}");
        assertTrue(doacao.get("prazoDevolucao").isNull());
        alterar(doacao.get("id").asLong(), "DEVOLVIDO", 409);
        mvc.perform(delete("/api/itens/" + item)).andExpect(status().isConflict());
    }
    @Test void validacaoECrudSemVinculos() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}")).andExpect(status().isBadRequest());
        long u = criar("/api/usuarios", "{\"nome\":\"Outra\",\"email\":\"outra@example.edu.br\",\"curso\":\"ADS\",\"telefone\":\"123\"}").get("id").asLong();
        mvc.perform(put("/api/usuarios/"+u).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Alterado\",\"email\":\"outra@example.edu.br\",\"curso\":\"ADS\",\"telefone\":\"123\"}")).andExpect(status().isOk());
        long i = criar("/api/itens", "{\"usuarioId\":"+u+",\"titulo\":\"Apostila\",\"tipo\":\"APOSTILA\",\"disciplina\":\"POO\",\"numeroPaginas\":20}").get("id").asLong();
        mvc.perform(delete("/api/itens/"+i)).andExpect(status().isNoContent());
        mvc.perform(get("/api/itens/"+i)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/usuarios/"+u)).andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/"+u)).andExpect(status().isNotFound());
    }
    @Test void prazoDeApostilaEEquipamento() throws Exception {
        long u = criar("/api/usuarios", "{\"nome\":\"Prazos\",\"email\":\"prazos@example.edu.br\",\"curso\":\"ADS\",\"telefone\":\"123\"}").get("id").asLong();
        long apostila = criar("/api/itens", "{\"usuarioId\":"+u+",\"titulo\":\"Apostila\",\"tipo\":\"APOSTILA\",\"disciplina\":\"POO\",\"numeroPaginas\":20}").get("id").asLong();
        long equipamento = criar("/api/itens", "{\"usuarioId\":"+u+",\"titulo\":\"Notebook\",\"tipo\":\"EQUIPAMENTO\",\"disciplina\":\"POO\",\"tipoEquipamento\":\"Notebook\",\"estadoConservacao\":\"Bom\"}").get("id").asLong();
        JsonNode a = criar("/api/movimentacoes", "{\"usuarioId\":"+u+",\"itemId\":"+apostila+",\"tipo\":\"EMPRESTIMO\"}");
        JsonNode e = criar("/api/movimentacoes", "{\"usuarioId\":"+u+",\"itemId\":"+equipamento+",\"tipo\":\"EMPRESTIMO\"}");
        assertEquals(LocalDate.now().plusDays(14).toString(), a.get("prazoDevolucao").asText());
        assertEquals(LocalDate.now().plusDays(7).toString(), e.get("prazoDevolucao").asText());
        mvc.perform(get("/api/usuarios/"+u+"/movimentacoes")).andExpect(status().isOk());
        mvc.perform(delete("/api/usuarios/"+u)).andExpect(status().isConflict());
    }
}
