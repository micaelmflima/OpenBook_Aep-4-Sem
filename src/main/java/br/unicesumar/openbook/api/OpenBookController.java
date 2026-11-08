package br.unicesumar.openbook.api;

import br.unicesumar.openbook.api.Models.*;
import br.unicesumar.openbook.service.OpenBookService;
import br.unicesumar.openbook.domain.UsuarioComMovimentacoes;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OpenBookController {
    private final OpenBookService service;
    public OpenBookController(OpenBookService service) { this.service = service; }
    @GetMapping("/usuarios") public List<Usuario> usuarios() { return service.usuarios(); }
    @GetMapping("/usuarios/{id}") public Usuario usuario(@PathVariable long id) { return service.usuario(id); }
    @GetMapping("/usuarios/{id}/movimentacoes") public UsuarioComMovimentacoes movimentacoesUsuario(@PathVariable long id) { return service.movimentacoesUsuario(id); }
    @PostMapping("/usuarios") @ResponseStatus(HttpStatus.CREATED) public Usuario criarUsuario(@RequestBody Usuario u) { return service.salvarUsuario(null,u); }
    @PutMapping("/usuarios/{id}") public Usuario atualizarUsuario(@PathVariable long id, @RequestBody Usuario u) { return service.salvarUsuario(id,u); }
    @DeleteMapping("/usuarios/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirUsuario(@PathVariable long id) { service.excluirUsuario(id); }
    @GetMapping("/itens") public List<Item> itens(@RequestParam(required=false) String tipo, @RequestParam(required=false) String disciplina, @RequestParam(required=false) Boolean disponivel) { return service.itens(tipo,disciplina,disponivel); }
    @GetMapping("/itens/{id}") public Item item(@PathVariable long id) { return service.item(id); }
    @PostMapping("/itens") @ResponseStatus(HttpStatus.CREATED) public Item criarItem(@RequestBody Item i) { return service.salvarItem(null,i); }
    @PutMapping("/itens/{id}") public Item atualizarItem(@PathVariable long id, @RequestBody Item i) { return service.salvarItem(id,i); }
    @DeleteMapping("/itens/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluirItem(@PathVariable long id) { service.excluirItem(id); }
    @GetMapping("/movimentacoes") public List<Movimentacao> movimentacoes() { return service.movimentacoes(); }
    @GetMapping("/movimentacoes/{id}") public Movimentacao movimentacao(@PathVariable long id) { return service.movimentacao(id); }
    @PostMapping("/movimentacoes") @ResponseStatus(HttpStatus.CREATED) public Movimentacao criarMovimentacao(@RequestBody NovaMovimentacao m) { return service.criarMovimentacao(m); }
    @PatchMapping("/movimentacoes/{id}/status") public Movimentacao atualizarStatus(@PathVariable long id, @RequestBody AtualizacaoStatus s) { return service.atualizarStatus(id,s.status()); }
}
