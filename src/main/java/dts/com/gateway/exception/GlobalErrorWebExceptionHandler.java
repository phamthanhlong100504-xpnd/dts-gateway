package dts.com.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Bắt toàn bộ lỗi ở cấp độ Gateway (ví dụ: Service con bị sập -> 503 Service Unavailable, lỗi 404 Not Found, v.v.)
 * và trả về định dạng JSON chuẩn thay vì giao diện HTML trắng mặc định của Spring Boot.
 */
@Component
@Order(-1) // Ưu tiên chạy trước các Exception Handler mặc định của Spring
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway Error: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal Server Error";

        // Phân loại các lỗi thường gặp ở Gateway
        if (ex instanceof ResponseStatusException) {
            status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            message = ex.getMessage();
        } else if (ex instanceof java.net.ConnectException || ex.getMessage().contains("Connection refused")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service is temporarily unavailable or down";
        }

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // Thiết lập Header trả về JSON
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Định dạng body chuẩn
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        errorResponse.put("timestamp", Instant.now().toString());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response", e);
            return Mono.error(e);
        }
    }
}
