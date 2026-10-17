package dts.com.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ResultInternalRouteGuardTest {
    @Test void internalResultProxyIsClosedWithoutAffectingHistory() {
        var guard=new ResultInternalRouteGuard();var chain=mock(GatewayFilterChain.class);
        var internal=MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/api/result/internal/results/users/any/overview"));
        guard.filter(internal,chain).block();assertEquals(HttpStatus.NOT_FOUND,internal.getResponse().getStatusCode());
        verifyNoInteractions(chain);
        var history=MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/api/result/v1/results/me/history"));
        when(chain.filter(any())).thenReturn(Mono.empty());guard.filter(history,chain).block();verify(chain).filter(history);
    }
}
