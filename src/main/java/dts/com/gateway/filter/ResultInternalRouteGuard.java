package dts.com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Internal source-to-source calls remain direct; the public Result proxy exposes /v1 only. */
@Component
public class ResultInternalRouteGuard implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain) {
        String path=exchange.getRequest().getURI().getPath();
        if ((path.equals("/api/result") || path.startsWith("/api/result/"))
                && (!path.startsWith("/api/result/v1/") || path.contains("..") || path.contains(";") || path.contains("\\") || path.contains("//"))) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE+2; }
}
