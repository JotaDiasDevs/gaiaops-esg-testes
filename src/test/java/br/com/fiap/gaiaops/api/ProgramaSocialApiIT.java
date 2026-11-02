package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/programas-sociais. */
@DisplayName("API /api/programas-sociais")
class ProgramaSocialApiIT extends BaseApi {

    @Test
    @DisplayName("GET /api/programas-sociais -> 200: 11 programas, ordenados por codigo, sem dados pessoais (contrato)")
    void listaTodos() {
        api().get("/api/programas-sociais").then()
                .statusCode(200)
                .body(contrato("programas-sociais"))
                .body("$", hasSize(11))
                .body("[0].codigo", equalTo("PRG-AC-001"));
    }

    @Test
    @DisplayName("GET /api/programas-sociais?eixo -> 200: so o eixo pedido")
    void filtraPorEixo() {
        api().queryParam("eixo", "acessibilidade").get("/api/programas-sociais").then()
                .statusCode(200)
                .body(contrato("programas-sociais"))
                .body("$", hasSize(2))
                .body("eixo", everyItem(equalTo("acessibilidade")));
    }

    @Test
    @DisplayName("GET /api/programas-sociais/{codigo} -> 200: programa com beneficiario PCD e recurso de acessibilidade")
    void buscaExistente() {
        api().get("/api/programas-sociais/{codigo}", "PRG-TR-001").then()
                .statusCode(200)
                .body(contrato("programa-social"))
                .body("nome", equalTo("Qualifica Vila Nova"))
                .body("beneficiarios.findAll { it.pcd == true }.acessibilidade.recurso", hasItem("interprete de libras"));
    }

    @Test
    @DisplayName("GET /api/programas-sociais/{codigo} -> 404: programa inexistente")
    void buscaInexistente() {
        api().get("/api/programas-sociais/{codigo}", "PRG-XX-999").then()
                .statusCode(404)
                .body(contrato("problema"))
                .body("detail", containsString("PRG-XX-999"));
    }
}
