package br.com.fiap.gaiaops.api;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.fiap.gaiaops.suporte.BaseDeDadosDeTeste;
import br.com.fiap.gaiaops.suporte.BaseIntegracao;
import br.com.fiap.gaiaops.suporte.RelogioDeTeste;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Base dos testes de API: cada teste faz chamadas HTTP reais para a API em execucao
 * e valida o status, o corpo JSON e o contrato (JSON Schema) da resposta.
 */
abstract class BaseApi extends BaseIntegracao {

    @LocalServerPort
    private int porta;

    @Autowired
    private BaseDeDadosDeTeste base;

    @Autowired
    protected RelogioDeTeste relogio;

    /** Cada teste parte da carga inicial e do relogio padrao (01/10/2026 12:00 UTC). */
    @BeforeEach
    void prepararAmbiente() {
        base.restaurar();
        relogio.restaurar();
    }

    /** Requisicao HTTP real para a API em execucao. */
    protected RequestSpecification api() {
        return given()
                .port(porta)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
}
