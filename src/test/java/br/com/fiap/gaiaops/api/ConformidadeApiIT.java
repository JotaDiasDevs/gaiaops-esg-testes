package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/conformidade e /api/conformidade/licencas/vencimentos. */
@DisplayName("API /api/conformidade")
class ConformidadeApiIT extends BaseApi {

    @Test
    @DisplayName("GET /api/conformidade -> 200: 12 registros de governanca no contrato")
    void listaTodos() {
        api().get("/api/conformidade").then()
                .statusCode(200)
                .body(contrato("conformidades"))
                .body("$", hasSize(12));
    }

    @Test
    @DisplayName("GET /api/conformidade?tipo -> 200: so o tipo pedido")
    void filtraPorTipo() {
        api().queryParam("tipo", "ouvidoria").get("/api/conformidade").then()
                .statusCode(200)
                .body(contrato("conformidades"))
                .body("$", hasSize(3))
                .body("tipo", everyItem(equalTo("ouvidoria")));
    }

    @Test
    @DisplayName("GET /licencas/vencimentos -> 200: em 01/10/2026 so a LP vencida aparece (prazo padrao de 180 dias)")
    void vencimentosNoPrazoPadrao() {
        api().get("/api/conformidade/licencas/vencimentos").then()
                .statusCode(200)
                .body(contrato("licencas-vencimento"))
                .body("$", hasSize(1))
                .body("[0].numero", equalTo("LP-11.204/2025"))
                .body("[0].vencida", equalTo(true))
                .body("[0].dias_para_vencer", equalTo(-16));
    }

    @Test
    @DisplayName("GET /licencas/vencimentos?dias=500 -> 200: inclui a LO, ordenada pela validade")
    void vencimentosComPrazoMaior() {
        api().queryParam("dias", 500).get("/api/conformidade/licencas/vencimentos").then()
                .statusCode(200)
                .body(contrato("licencas-vencimento"))
                .body("numero", contains("LP-11.204/2025", "LO-45.678/2023"))
                .body("[1].vencida", equalTo(false));
    }

    @Test
    @DisplayName("GET /licencas/vencimentos -> 200: o calculo acompanha a data de referencia (01/09/2027)")
    void vencimentosEmOutraData() {
        relogio.ajustar(Instant.parse("2027-09-01T00:00:00Z"));

        api().get("/api/conformidade/licencas/vencimentos").then()
                .statusCode(200)
                .body(contrato("licencas-vencimento"))
                .body("find { it.numero == 'LO-45.678/2023' }.dias_para_vencer", equalTo(90));
    }
}
