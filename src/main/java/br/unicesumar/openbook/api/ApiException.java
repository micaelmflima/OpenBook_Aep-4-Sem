package br.unicesumar.openbook.api;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus status() { return status; }
    public static ApiException invalido(String message) { return new ApiException(HttpStatus.BAD_REQUEST, message); }
    public static ApiException ausente(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    public static ApiException conflito(String message) { return new ApiException(HttpStatus.CONFLICT, message); }
}
