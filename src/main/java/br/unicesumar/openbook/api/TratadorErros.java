package br.unicesumar.openbook.api;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorErros {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, String>> api(ApiException e) { return ResponseEntity.status(e.status()).body(Map.of("erro", e.getMessage())); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> integridade() { return ResponseEntity.status(409).body(Map.of("erro", "Operação impedida: e-mail já cadastrado ou registro possui vínculos.")); }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> formato() { return ResponseEntity.badRequest().body(Map.of("erro", "Dados em formato inválido.")); }
}
