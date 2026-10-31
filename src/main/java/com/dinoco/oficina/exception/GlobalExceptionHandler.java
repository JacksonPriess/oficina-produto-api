package com.dinoco.oficina.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoDto> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de campos",
                erros
        );

        log.warn("Falha de validação recebida. Campos com erro: {}", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroPadrao);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ErroPadraoDto> handleBusinessErrors(RuntimeException ex) {

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Regra de negócio violada",
                List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroPadrao);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadraoDto> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                List.of(ex.getMessage())
        );

        log.info("Recurso não encontrado: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erroPadrao);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDto> handleGenericException(Exception ex) {

        log.error("Erro interno inesperado no servidor", ex);

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno no Servidor",
                List.of("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde ou contate o suporte.")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erroPadrao);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroPadraoDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String nomeDoParametro = ex.getName();
        String valorEnviado = ex.getValue() != null ? ex.getValue().toString() : "nulo";
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";

        String mensagem = String.format("O parâmetro '%s' recebeu o valor '%s', que é de um tipo inválido. Deveria ser do tipo '%s'.",
                nomeDoParametro, valorEnviado, tipoEsperado);

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro Inválido",
                List.of(mensagem)
        );

        log.warn("Erro de conversão de tipo: {}", mensagem);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroPadrao);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroPadraoDto> handleNoResourceFound(NoResourceFoundException ex) {
        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Rota não encontrada",
                List.of("O endpoint '" + ex.getResourcePath() + "' não existe nesta API.")
        );

        log.warn("Tentativa de acesso a rota inexistente: {}", ex.getResourcePath());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erroPadrao);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroPadraoDto> handleBadCredentials(BadCredentialsException ex) {
        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Falha na autenticação",
                List.of("Usuário ou senha inválidos.")
        );

        log.warn("Falha de autenticação: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erroPadrao);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadraoDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        String mensagemErro = "Corpo da requisição mal formatado ou com tipos de dados inválidos.";
        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx) {
            if (invalidFormatEx.getTargetType() != null && invalidFormatEx.getTargetType().isEnum()) {
                String valorEnviado = invalidFormatEx.getValue().toString();
                String valoresAceitos = Arrays.toString(invalidFormatEx.getTargetType().getEnumConstants());
                mensagemErro = String.format("O valor '%s' é inválido. Valores aceitos: %s", valorEnviado, valoresAceitos);
            }
        }

        ErroPadraoDto erroPadrao = new ErroPadraoDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Requisição",
                List.of(mensagemErro)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroPadrao);
    }


}
