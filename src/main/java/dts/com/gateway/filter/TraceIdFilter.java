package dts.com.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Lấy trace-id từ request gửi lên (nếu có), nếu chưa có thì tạo mới UUID
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // Log đường dẫn request kèm Trace ID
        log.info("[Trace-ID: {}] Request: {} {}", traceId, request.getMethod(), request.getURI().getPath());

        // Chèn Trace ID vào Request Header để truyền tiếp tới microservice đằng sau
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // Đồng thời thêm X-Trace-Id vào Response Header trả về cho client/frontend
        modifiedExchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);

        return chain.filter(modifiedExchange);
    }

    @Override
    public int getOrder() {
        // Đảm bảo Filter này chạy đầu tiên trước tất cả các Filter khác
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
