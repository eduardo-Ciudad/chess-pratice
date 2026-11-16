package com.eduar.chess.adapter.in.web;

import com.eduar.chess.application.exception.InvalidPgnException;
import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.exception.InvalidLineException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepertoireNotFoundException.class)
    public ProblemDetail handleNotFound(RepertoireNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Repertório não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictingRepertoireMoveException.class)
    public ProblemDetail handleConflict(ConflictingRepertoireMoveException ex) {
        return problem(HttpStatus.CONFLICT, "Conflito no repertório", ex.getMessage());
    }

    @ExceptionHandler({IllegalMoveException.class, InvalidLineException.class, InvalidPgnException.class})
    public ProblemDetail handleChessRule(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Lance ou linha inválida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "Um ou mais campos são inválidos");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleUnreadable(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "O corpo ou os parâmetros da requisição não puderam ser lidos");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
