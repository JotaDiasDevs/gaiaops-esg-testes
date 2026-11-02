package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

/**
 * Testes das APIs de plataforma: saude, informacoes da versao, documentacao
 * OpenAPI e pagina inicial. Inclui a verificacao de que TODAS as rotas da API
 * estao cobertas por esta suite.
 */
@DisplayName("APIs de plataforma (Actuator, OpenAPI e pagina inicial)")
class PlataformaApiIT extends BaseApi {

    /**
     * Todas as rotas /api da aplicacao, cada uma testada em uma classe *ApiIT.
     * Se alguem criar um endpoint novo sem teste, cobreTodasAsRotas() falha.
     */
    private static final String[] ROTAS_COBERTAS = {
        "/api/equipamentos", "/api/equipamentos/{codigo}",
        "/api/medicoes", "/api/medicoes/alertas",
        "/api/residuos", "/api/residuos/resumo",
        "/api/programas-sociais", "/api/programas-sociais/{codigo}",
        "/api/conformidade", "/api/conformidade/licencas/vencimentos",
        "/api/indicadores"
    };

    @Test
    @DisplayName("GET /actuator/health -> 200: aplicacao UP e MongoDB conectado")
    void health() {
        api().get("/actuator/health").then()
                .statusCode(200)
                .body(contrato("health"))
                .body("status", equalTo("UP"))
                .body("components.mongo.status", equalTo("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/readiness e /liveness -> 200: UP")
    void probes() {
        api().get("/actuator/health/readiness").then()
                .statusCode(200)
                .body(contrato("health"))
                .body("status", equalTo("UP"));
        api().get("/actuator/health/liveness").then()
                .statusCode(200)
                .body(contrato("health"))
                .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("GET /actuator/info -> 200: nome, versao, ambiente e commit (rastreabilidade)")
    void info() {
        api().get("/actuator/info").then()
                .statusCode(200)
                .body(contrato("info"))
                .body("app.nome", equalTo("GaiaOps ESG"))
                .body("app.versao", equalTo("1.0.0"))
                .body("build.artifact", equalTo("gaiaops-esg"));
    }

    @Test
    @DisplayName("GET /v3/api-docs -> 200: OpenAPI 3 lista exatamente as rotas cobertas pelos testes")
    void cobreTodasAsRotas() {
        var resposta = api().get("/v3/api-docs");
        resposta.then()
                .statusCode(200)
                .body("openapi", startsWith("3."))
                .body("info.title", equalTo("GaiaOps ESG API"));

        Map<String, Object> rotas = resposta.jsonPath().getMap("paths");
        assertThat("rotas documentadas x rotas cobertas pelos testes", rotas.keySet(), containsInAnyOrder(ROTAS_COBERTAS));
    }

    @Test
    @DisplayName("GET / -> 200: pagina inicial publicada")
    void paginaInicial() {
        api().accept(ContentType.HTML).get("/").then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("GaiaOps ESG"));
    }

    @Test
    @DisplayName("GET rota inexistente -> 404 no formato Problem Details")
    void rotaInexistente() {
        api().get("/api/rota-que-nao-existe").then()
                .statusCode(404)
                .body(contrato("problema"));
    }
}
