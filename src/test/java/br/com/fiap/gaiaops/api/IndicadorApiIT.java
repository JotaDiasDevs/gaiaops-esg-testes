package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/indicadores (painel ESG calculado por agregacoes no MongoDB). */
@DisplayName("API /api/indicadores")
class IndicadorApiIT extends BaseApi {

    @Test
    @DisplayName("GET /api/indicadores -> 200: os tres pilares calculados sobre a carga inicial, contrato fechado")
    void painel() {
        api().get("/api/indicadores").then()
                .statusCode(200)
                .body(contrato("painel-esg"))
                .body("gerado_em", startsWith("2026-10-01T12:00:00"))
                // E
                .body("ambiental.medicoes", equalTo(53))
                .body("ambiental.alertas_abertos", equalTo(6))
                .body("ambiental.alertas_abertos_por_nivel.critico", equalTo(1))
                .body("ambiental.alertas_abertos_por_nivel.alto", equalTo(2))
                .body("ambiental.alertas_abertos_por_nivel.medio", equalTo(3))
                .body("ambiental.residuos_coletados_kg", equalTo(83702.0))
                .body("ambiental.taxa_desvio_de_aterro_pct", equalTo(49.0))
                // S
                .body("social.programas_ativos", equalTo(11))
                .body("social.beneficiarios_acompanhados", equalTo(25))
                .body("social.orcamento_previsto_brl", equalTo(7545600.0))
                .body("social.orcamento_executado_brl", equalTo(4414800.0))
                .body("social.execucao_orcamentaria_pct", equalTo(58.5))
                // G
                .body("governanca.equipamentos_ativos", equalTo(12))
                .body("governanca.equipamentos_total", equalTo(12))
                .body("governanca.licencas_vencidas", equalTo(1))
                .body("governanca.licencas_por_vencer", equalTo(0))
                .body("governanca.manifestacoes_ouvidoria", equalTo(3))
                .body("governanca.termos_ajustamento", equalTo(2));
    }

    @Test
    @DisplayName("GET /api/indicadores -> 200: reflete na hora as gravacoes feitas pelas outras APIs")
    void refleteAlteracoes() {
        api().body("""
                        { "equipamento_id": "EQP-PRA-001", "tipo": "hidrologia",
                          "leitura": { "nivel_cm": 251 },
                          "referencia": { "nivel_atencao_cm": 180, "nivel_alerta_cm": 240 } }""")
                .post("/api/medicoes").then().statusCode(201);
        api().delete("/api/equipamentos/{codigo}", "EQP-ECO-002").then().statusCode(204);

        api().get("/api/indicadores").then()
                .statusCode(200)
                .body(contrato("painel-esg"))
                .body("ambiental.medicoes", equalTo(54))
                .body("ambiental.alertas_abertos", equalTo(7))
                .body("ambiental.alertas_abertos_por_nivel.critico", equalTo(2))
                .body("governanca.equipamentos_ativos", equalTo(11))
                .body("governanca.equipamentos_total", equalTo(12));
    }
}
