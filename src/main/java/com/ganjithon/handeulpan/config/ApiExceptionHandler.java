package com.ganjithon.handeulpan.config;

import com.ganjithon.handeulpan.pasture.CoreError;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 에러를 {@code {code, message}}로 내려줍니다.
 *
 * <ul>
 *   <li>{@link ApiException}: BE-B 에러 ({@link ApiError})</li>
 *   <li>{@link ResponseStatusException}: BE-A 에러. reason에 담긴 코드({@link CoreError})를 꺼내 씁니다.</li>
 *   <li>Spring MVC 표준 예외(검증 실패, JSON 파싱 오류, 없는 주소, 지원하지 않는 메서드 등):
 *       400은 {@code VALIDATION_FAILED}, 그 외는 HTTP 상태 이름(NOT_FOUND 등)을 코드로 씁니다.</li>
 *   <li>그 밖의 예상 못 한 예외: 500 {@code INTERNAL_ERROR}. 내부 메시지는 응답에 넣지 않고 로그에만 남깁니다.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApi(ApiException e) {
        ApiError error = e.getError();
        return ResponseEntity.status(error.status()).body(new ApiErrorResponse(error.name(), error.message()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException e) {
        String reason = e.getReason();
        Optional<CoreError> core = find(CoreError.values(), reason);
        if (core.isPresent()) {
            return ResponseEntity.status(core.get().status())
                    .body(new ApiErrorResponse(core.get().name(), core.get().message()));
        }
        Optional<ApiError> api = find(ApiError.values(), reason);
        if (api.isPresent()) {
            return ResponseEntity.status(api.get().status())
                    .body(new ApiErrorResponse(api.get().name(), api.get().message()));
        }
        return ResponseEntity.status(e.getStatusCode()).body(fromStatus(e.getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e) {
        log.error("처리하지 못한 예외", e);
        ApiError error = ApiError.INTERNAL_ERROR;
        return ResponseEntity.status(error.status()).body(new ApiErrorResponse(error.name(), error.message()));
    }

    /** Spring MVC 표준 예외가 모두 여기를 거치므로 본문을 {@link ApiErrorResponse}로 통일합니다. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        return super.handleExceptionInternal(ex, fromStatus(statusCode), headers, statusCode, request);
    }

    private static ApiErrorResponse fromStatus(HttpStatusCode statusCode) {
        if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
            return new ApiErrorResponse(ApiError.VALIDATION_FAILED.name(), ApiError.VALIDATION_FAILED.message());
        }
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null || status.is5xxServerError()) {
            return new ApiErrorResponse(ApiError.INTERNAL_ERROR.name(), ApiError.INTERNAL_ERROR.message());
        }
        return new ApiErrorResponse(status.name(), status.getReasonPhrase());
    }

    private static <E extends Enum<E>> Optional<E> find(E[] values, String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Arrays.stream(values).filter(v -> v.name().equals(name)).findFirst();
    }
}
