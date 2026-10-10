package com.bidding.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：把各类异常统一转成 Result 结构，避免到处 try/catch。
 * 面试高频考点：@RestControllerAdvice + @ExceptionHandler 如何统一处理异常。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：返回其携带的 code 与 message */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getCode()).body(Result.error(e.getCode(), e.getMessage()));
    }

    /** 参数校验失败（@Validated + 注解）：取第一个字段错误提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode())
                .body(Result.error(ResultCode.BAD_REQUEST.getCode(), message));
    }

    /** 无权限（@PreAuthorize 拒绝时抛出的异常） */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(ResultCode.FORBIDDEN.getCode())
                .body(Result.error(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage()));
    }

    /** 兜底：未预期的异常，记录日志，对外只给通用提示（不暴露内部细节） */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未预期异常", e);
        return ResponseEntity.status(ResultCode.INTERNAL_ERROR.getCode())
                .body(Result.error(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage()));
    }
}
