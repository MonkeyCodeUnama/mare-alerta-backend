package com.marealerta.backend.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(2, 2, 60);
    }

    @Test
    @DisplayName("Deve permitir requisições dentro do limite do bucket")
    void devePermitirRequisicoesDentroDoLimite() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/alertas");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve bloquear com HTTP 429 e Retry-After quando exceder o limite de requisições")
    void deveBloquearAoExcederLimite() throws ServletException, IOException {
        String clientIp = "192.168.1.200";

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/alertas");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(req, resp, filterChain);
            assertThat(resp.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest blockedReq = new MockHttpServletRequest("GET", "/alertas");
        blockedReq.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedResp = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(blockedReq, blockedResp, filterChain);

        assertThat(blockedResp.getStatus()).isEqualTo(429);
        assertThat(blockedResp.getHeader("Retry-After")).isEqualTo("60");
        assertThat(blockedResp.getContentAsString()).contains("Limite de requisições excedido");
        verify(filterChain, never()).doFilter(blockedReq, blockedResp);
        verify(filterChain, times(2)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar filtro para rotas do Swagger e OpenAPI")
    void deveIgnorarFiltroParaSwagger() {
        MockHttpServletRequest swaggerReq = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletRequest openApiReq = new MockHttpServletRequest("GET", "/v3/api-docs");

        assertThat(rateLimitFilter.shouldNotFilter(swaggerReq)).isTrue();
        assertThat(rateLimitFilter.shouldNotFilter(openApiReq)).isTrue();
    }
}
