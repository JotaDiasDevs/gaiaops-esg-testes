package br.com.fiap.gaiaops.api;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de API de /api/residuos e /api/residuos/resumo. */
@DisplayName("API /api/residuos")
class ResiduoApiIT extends BaseApi {

    @Test
    @DisplayName("GET /api/residuos -> 200: 12 coletas no contrato")
    void listaTodas() {
        api().get("/api/residuos").then()
                .statusCode(200)
                .body(contrato("residuos"))
                .body("$", hasSize(12));
    }

    @Test
    @DisplayName("GET /api/residuos?categoria -> 200: so a categoria pedida; categoria sem coletas devolve lista vazia")
    void filtraPorCategoria() {
        api().queryParam("categoria", "reciclavel").get("/api/residuos").then()
                .statusCode(200)
                .body(contrato("residuos"))
                .body("$", hasSize(3))
                .body("categoria", everyItem(equalTo("reciclavel")));

        api().queryParam("categoria", "radioativo").get("/api/residuos").then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("GET /api/residuos/resumo -> 200: agregacao por categoria e destinacao, maior peso primeiro")
    void resumo() {
        var resposta = api().get("/api/residuos/resumo");
        resposta.then()
                .statusCode(200)
                .body(contrato("residuos-resumo"))
                .body("$", hasSize(7))
                .body("[0].categoria", equalTo("rejeito"))
                .body("[0].destinacao", equalTo("aterro_sanitario"))
                .body("[0].peso_kg", equalTo(42200));

        // O resumo nao perde nem inventa residuo: a soma bate com o total coletado.
        List<Integer> pesos = resposta.jsonPath().getList("peso_kg", Integer.class);
        assertThat(pesos.stream().mapToInt(Integer::intValue).sum(), equalTo(83702));
    }
}
