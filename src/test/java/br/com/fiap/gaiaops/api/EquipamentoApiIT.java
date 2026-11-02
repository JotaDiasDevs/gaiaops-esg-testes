package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/equipamentos: GET (lista e item), POST, PUT e DELETE. */
@DisplayName("API /api/equipamentos")
class EquipamentoApiIT extends BaseApi {

    private static final String NOVA_ESCOLA = """
            {
              "codigo": "EQP-ESC-002",
              "nome": "EMEF Jardim das Flores",
              "tipo": "escola",
              "distrito": "Morro Alto",
              "populacao_atendida": 640,
              "atributos": {
                "escola": { "turnos": ["manha", "tarde"], "salas": 12 },
                "ativo": false,
                "_id": "tentativa-de-sobrescrever"
              }
            }""";

    @Nested
    @DisplayName("GET /api/equipamentos")
    class Listar {

        @Test
        @DisplayName("200: lista os 12 equipamentos da carga inicial, ordenados por codigo, no contrato")
        void listaTodos() {
            api().get("/api/equipamentos").then()
                    .statusCode(200)
                    .body(contrato("equipamentos"))
                    .body("$", hasSize(12))
                    .body("[0].codigo", equalTo("EQP-ECO-001"))
                    .body("codigo", hasItems("EQP-ESC-001", "EQP-SAN-001", "EQP-TER-001"));
        }

        @Test
        @DisplayName("200: filtros por tipo, distrito e situacao")
        void filtra() {
            api().queryParam("tipo", "saneamento").get("/api/equipamentos").then()
                    .statusCode(200)
                    .body(contrato("equipamentos"))
                    .body("$", hasSize(2))
                    .body("tipo", everyItem(equalTo("saneamento")));

            api().queryParam("distrito", "Centro").get("/api/equipamentos").then()
                    .statusCode(200)
                    .body("$", hasSize(4))
                    .body("distrito", everyItem(equalTo("Centro")));

            api().queryParam("ativo", false).get("/api/equipamentos").then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        @DisplayName("200: respeita o limite de itens")
        void limita() {
            api().queryParam("limite", 3).get("/api/equipamentos").then()
                    .statusCode(200)
                    .body("$", hasSize(3));
        }
    }

    @Nested
    @DisplayName("GET /api/equipamentos/{codigo}")
    class Buscar {

        @Test
        @DisplayName("200: devolve o documento completo, inclusive atributos especificos do tipo")
        void buscaExistente() {
            api().get("/api/equipamentos/{codigo}", "EQP-ESC-001").then()
                    .statusCode(200)
                    .body(contrato("equipamento"))
                    .body("codigo", equalTo("EQP-ESC-001"))
                    .body("tipo", equalTo("escola"))
                    .body("escola.salas", equalTo(18))
                    .body("local.type", equalTo("Point"));
        }

        @Test
        @DisplayName("404: codigo inexistente devolve Problem Details")
        void buscaInexistente() {
            api().get("/api/equipamentos/{codigo}", "EQP-XXX-999").then()
                    .statusCode(404)
                    .body(contrato("problema"))
                    .body("status", equalTo(404))
                    .body("title", equalTo("Recurso nao encontrado"))
                    .body("detail", containsString("EQP-XXX-999"));
        }
    }

    @Nested
    @DisplayName("POST /api/equipamentos")
    class Cadastrar {

        @Test
        @DisplayName("201: cadastra com Location, atributos livres gravados e campos controlados protegidos")
        void cadastra() {
            api().body(NOVA_ESCOLA).post("/api/equipamentos").then()
                    .statusCode(201)
                    .header("Location", endsWith("/api/equipamentos/EQP-ESC-002"))
                    .body(contrato("equipamento"))
                    .body("ativo", equalTo(true))
                    .body("populacao_atendida", equalTo(640))
                    .body("escola.salas", equalTo(12))
                    .body("_id", not(equalTo("tentativa-de-sobrescrever")))
                    .body("criado_em", startsWith("2026-10-01T12:00:00"));

            api().get("/api/equipamentos/{codigo}", "EQP-ESC-002").then().statusCode(200);
        }

        @Test
        @DisplayName("400: corpo vazio aponta todos os campos obrigatorios")
        void corpoVazio() {
            api().body("{}").post("/api/equipamentos").then()
                    .statusCode(400)
                    .body(contrato("problema-validacao"))
                    .body("erros", hasKey("codigo"))
                    .body("erros", hasKey("nome"))
                    .body("erros", hasKey("tipo"))
                    .body("erros", hasKey("distrito"));
        }

        @Test
        @DisplayName("400: codigo fora do padrao e populacao negativa")
        void valoresInvalidos() {
            api().body("""
                            { "codigo": "escola-2", "nome": "EMEF Nova", "tipo": "escola",
                              "distrito": "Centro", "populacao_atendida": -1 }""")
                    .post("/api/equipamentos").then()
                    .statusCode(400)
                    .body(contrato("problema-validacao"))
                    .body("erros.codigo", containsString("EQP-XXX-NNN"))
                    .body("erros", hasKey("populacao_atendida"));
        }

        @Test
        @DisplayName("400: JSON malformado devolve Problem Details")
        void jsonMalformado() {
            api().body("{ \"codigo\": ").post("/api/equipamentos").then()
                    .statusCode(400)
                    .body(contrato("problema"));
        }

        @Test
        @DisplayName("409: codigo ja cadastrado")
        void duplicado() {
            api().body(NOVA_ESCOLA.replace("EQP-ESC-002", "EQP-ESC-001")).post("/api/equipamentos").then()
                    .statusCode(409)
                    .body(contrato("problema"))
                    .body("detail", containsString("EQP-ESC-001"));
        }

        @Test
        @DisplayName("422: atributo com nome proibido pelo MongoDB ($ no inicio)")
        void atributoInvalido() {
            api().body("""
                            { "codigo": "EQP-ESC-005", "nome": "EMEF Nova", "tipo": "escola",
                              "distrito": "Centro", "atributos": { "$where": "1" } }""")
                    .post("/api/equipamentos").then()
                    .statusCode(422)
                    .body(contrato("problema"))
                    .body("detail", containsString("$where"));
        }
    }

    @Nested
    @DisplayName("PUT /api/equipamentos/{codigo}")
    class Atualizar {

        @Test
        @DisplayName("200: atualiza com $set e preserva atributos nao enviados")
        void atualiza() {
            api().body("""
                            { "nome": "EMEF Profa. Alzira Bittencourt", "tipo": "escola",
                              "distrito": "Jardim Aurora", "atributos": { "metas_esg": { "energia_kwh_mes": 8500 } } }""")
                    .put("/api/equipamentos/{codigo}", "EQP-ESC-001").then()
                    .statusCode(200)
                    .body(contrato("equipamento"))
                    .body("nome", equalTo("EMEF Profa. Alzira Bittencourt"))
                    .body("metas_esg.energia_kwh_mes", equalTo(8500))
                    .body("escola.salas", equalTo(18))
                    .body("atualizado_em", notNullValue());
        }

        @Test
        @DisplayName("400: atualizacao sem nome")
        void atualizacaoInvalida() {
            api().body("{ \"tipo\": \"escola\", \"distrito\": \"Centro\" }")
                    .put("/api/equipamentos/{codigo}", "EQP-ESC-001").then()
                    .statusCode(400)
                    .body(contrato("problema-validacao"))
                    .body("erros", hasKey("nome"));
        }

        @Test
        @DisplayName("404: atualizar equipamento inexistente")
        void atualizaInexistente() {
            api().body("{ \"nome\": \"Fantasma\", \"tipo\": \"praca\", \"distrito\": \"Centro\" }")
                    .put("/api/equipamentos/{codigo}", "EQP-XXX-999").then()
                    .statusCode(404)
                    .body(contrato("problema"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/equipamentos/{codigo}")
    class Desativar {

        @Test
        @DisplayName("204: desativa (exclusao logica) e o documento continua consultavel")
        void desativa() {
            api().delete("/api/equipamentos/{codigo}", "EQP-ILU-001").then()
                    .statusCode(204)
                    .body(emptyString());

            api().get("/api/equipamentos/{codigo}", "EQP-ILU-001").then()
                    .statusCode(200)
                    .body(contrato("equipamento"))
                    .body("ativo", equalTo(false))
                    .body("desativado_em", startsWith("2026-10-01T12:00:00"));
        }

        @Test
        @DisplayName("404: desativar equipamento inexistente")
        void desativaInexistente() {
            api().delete("/api/equipamentos/{codigo}", "EQP-XXX-999").then()
                    .statusCode(404)
                    .body(contrato("problema"));
        }
    }
}
