package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/medicoes: listagem, alertas e ingestao de leituras. */
@DisplayName("API /api/medicoes")
class MedicaoApiIT extends BaseApi {

    @Nested
    @DisplayName("GET /api/medicoes")
    class Listar {

        @Test
        @DisplayName("200: limite padrao de 50 itens, no contrato")
        void listaComLimitePadrao() {
            api().get("/api/medicoes").then()
                    .statusCode(200)
                    .body(contrato("medicoes"))
                    .body("$", hasSize(50));
        }

        @Test
        @DisplayName("200: limite maior devolve as 53 medicoes da carga inicial")
        void listaTodas() {
            api().queryParam("limite", 500).get("/api/medicoes").then()
                    .statusCode(200)
                    .body(contrato("medicoes"))
                    .body("$", hasSize(53));
        }

        @Test
        @DisplayName("200: filtros por equipamento e por tipo de sensor")
        void filtra() {
            api().queryParam("tipo", "energia").get("/api/medicoes").then()
                    .statusCode(200)
                    .body(contrato("medicoes"))
                    .body("$", hasSize(14))
                    .body("tipo", everyItem(equalTo("energia")));

            api().queryParam("equipamento_id", "EQP-PRA-002").get("/api/medicoes").then()
                    .statusCode(200)
                    .body("equipamento_id", everyItem(equalTo("EQP-PRA-002")));
        }
    }

    @Nested
    @DisplayName("GET /api/medicoes/alertas")
    class Alertas {

        @Test
        @DisplayName("200: todas as leituras com alerta (7), cada uma com o bloco alerta")
        void todosOsAlertas() {
            api().get("/api/medicoes/alertas").then()
                    .statusCode(200)
                    .body(contrato("medicoes"))
                    .body("$", hasSize(7))
                    .body("alerta", everyItem(notNullValue()));
        }

        @Test
        @DisplayName("200: somente alertas abertos (6) e filtro por nivel")
        void abertosEPorNivel() {
            api().queryParam("somente_abertos", true).get("/api/medicoes/alertas").then()
                    .statusCode(200)
                    .body("$", hasSize(6))
                    .body("alerta.reconhecido", everyItem(equalTo(false)));

            api().queryParam("nivel", "critico").get("/api/medicoes/alertas").then()
                    .statusCode(200)
                    .body(contrato("medicoes"))
                    .body("$", hasSize(1))
                    .body("[0].equipamento_id", equalTo("EQP-PRA-002"));
        }
    }

    @Nested
    @DisplayName("POST /api/medicoes")
    class Registrar {

        @Test
        @DisplayName("201: IQAr 151 gera alerta critico; data e competencia vem do relogio")
        void registraComAlerta() {
            api().body("""
                            { "equipamento_id": "EQP-PRA-002", "tipo": "qualidade_ar",
                              "sensor": { "id": "SEN-AR-004" },
                              "leitura": { "mp10_ug_m3": 95, "indice_iqar": 151 } }""")
                    .post("/api/medicoes").then()
                    .statusCode(201)
                    .body(contrato("medicao"))
                    .body("alerta.nivel", equalTo("critico"))
                    .body("alerta.regra", containsString("130"))
                    .body("alerta.valor_medido", equalTo(151))
                    .body("data", startsWith("2026-10-01T12:00:00"))
                    .body("competencia", equalTo("2026-10"));
        }

        @Test
        @DisplayName("201: ruido noturno abaixo do limite nao gera alerta; data informada define a competencia")
        void registraSemAlerta() {
            api().body("""
                            { "equipamento_id": "EQP-TER-001", "tipo": "ruido", "data": "2026-08-15T23:00:00Z",
                              "leitura": { "laeq_db": 50.5, "periodo": "noturno" },
                              "referencia": { "limite_noturno_db": 55 } }""")
                    .post("/api/medicoes").then()
                    .statusCode(201)
                    .body(contrato("medicao"))
                    .body("alerta", nullValue())
                    .body("competencia", equalTo("2026-08"));
        }

        @Test
        @DisplayName("400: tipo de sensor desconhecido, codigo invalido e leitura vazia")
        void invalida() {
            api().body("{ \"equipamento_id\": \"praca-2\", \"tipo\": \"radiacao\", \"leitura\": {} }")
                    .post("/api/medicoes").then()
                    .statusCode(400)
                    .body(contrato("problema-validacao"))
                    .body("erros", hasKey("equipamento_id"))
                    .body("erros", hasKey("tipo"))
                    .body("erros", hasKey("leitura"));
        }

        @Test
        @DisplayName("404: equipamento inexistente")
        void equipamentoInexistente() {
            api().body("{ \"equipamento_id\": \"EQP-XYZ-999\", \"tipo\": \"energia\", \"leitura\": { \"kwh\": 10 } }")
                    .post("/api/medicoes").then()
                    .statusCode(404)
                    .body(contrato("problema"));
        }

        @Test
        @DisplayName("422: equipamento desativado nao recebe medicoes")
        void equipamentoDesativado() {
            api().delete("/api/equipamentos/{codigo}", "EQP-ILU-002").then().statusCode(204);

            api().body("{ \"equipamento_id\": \"EQP-ILU-002\", \"tipo\": \"energia\", \"leitura\": { \"kwh\": 10 } }")
                    .post("/api/medicoes").then()
                    .statusCode(422)
                    .body(contrato("problema"))
                    .body("detail", containsString("desativado"));
        }
    }
}
