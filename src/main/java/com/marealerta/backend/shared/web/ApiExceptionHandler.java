package com.marealerta.backend.shared.web;

import com.marealerta.backend.auth.service.EmailAlreadyRegisteredException;
import com.marealerta.backend.auth.service.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import com.marealerta.backend.risco.service.ExternalRiskServiceException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> {
                    String def = error.getDefaultMessage();
                    if (def != null && !def.isBlank()) {
                        return Character.toUpperCase(def.charAt(0)) + def.substring(1);
                    }
                    return "Verifique os campos preenchidos e tente novamente.";
                })
                .orElse("Verifique os campos preenchidos e tente novamente.");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleEmailConflict(EmailAlreadyRegisteredException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Este e-mail já está cadastrado. Faça login ou utilize outro endereço.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        String raw = exception.getMostSpecificCause() != null ? exception.getMostSpecificCause().getMessage() : "";
        if (raw != null && raw.toLowerCase().contains("email")) {
            return error(HttpStatus.CONFLICT, "Este e-mail já está cadastrado. Faça login ou utilize outro endereço.", request);
        }
        if (raw != null && raw.toLowerCase().contains("documento")) {
            return error(HttpStatus.CONFLICT, "Este CPF ou CNPJ já está vinculado a outro estabelecimento.", request);
        }
        return error(HttpStatus.CONFLICT, "Estes dados já estão registrados no sistema.", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos. Confira os dados e tente novamente.", request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return error(status, exception.getReason() != null ? exception.getReason() : status.getReasonPhrase(), request);
    }

    @ExceptionHandler(ExternalRiskServiceException.class)
    public ResponseEntity<ApiError> handleExternalRisk(ExternalRiskServiceException exception, HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
