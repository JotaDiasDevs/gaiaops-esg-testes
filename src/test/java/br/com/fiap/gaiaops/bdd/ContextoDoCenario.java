package br.com.fiap.gaiaops.bdd;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.fiap.gaiaops.suporte.BaseDeDadosDeTeste;
import br.com.fiap.gaiaops.suporte.RelogioDeTeste;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/**
 * Estado compartilhado entre as classes de steps durante UM cenario.
 *
 * O cucumber-spring cria uma instancia nova a cada cenario e a injeta nas demais
 * classes de steps. Guarda a ultima resposta da API, para que um "Quando" faca a
 * chamada e os "Entao" seguintes verifiquem o resultado.
 */
public class ContextoDoCenario {

    @LocalServerPort
    private int porta;

    private final BaseDeDadosDeTeste base;
    private final RelogioDeTeste relogio;

    private Response resposta;

    public ContextoDoCenario(BaseDeDadosDeTeste base, RelogioDeTeste relogio) {
        this.base = base;
        this.relogio = relogio;
    }

    /** Cada cenario comeca com a base da carga inicial e o relogio no padrao (01/10/2026). */
    @Before(order = 0)
    public void prepararCenario() {
        base.restaurar();
        relogio.restaurar();
        resposta = null;
    }

    /** Se o cenario falhar, a ultima resposta da API vai anexada ao relatorio do Cucumber. */
    @After
    public void anexarRespostaSeFalhar(Scenario cenario) {
        if (cenario.isFailed() && resposta != null) {
            cenario.attach(resposta.asString().getBytes(StandardCharsets.UTF_8),
                    "application/json", "Ultima resposta da API (HTTP " + resposta.statusCode() + ")");
        }
    }

    /** Requisicao HTTP real para a API em execucao. */
    public RequestSpecification api() {
        return RestAssured.given()
                .port(porta)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public void registrar(Response resposta) {
        this.resposta = resposta;
    }

    public Response resposta() {
        if (resposta == null) {
            throw new IllegalStateException("Nenhuma chamada a API foi feita neste cenario");
        }
        return resposta;
    }

    /** Inicia as verificacoes sobre a ultima resposta. */
    public ValidatableResponse validar() {
        return resposta().then();
    }

    public RelogioDeTeste relogio() {
        return relogio;
    }
}
