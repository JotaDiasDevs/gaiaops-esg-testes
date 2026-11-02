package br.com.fiap.gaiaops.suporte;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.com.fiap.gaiaops.carga.CargaInicial;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig.NumberReturnType;

/**
 * Ambiente comum dos testes de integracao (cenarios BDD e testes de API).
 *
 * - A API sobe completa, num servidor HTTP real em porta aleatoria: os testes
 *   fazem chamadas HTTP de verdade, como um cliente externo faria.
 * - O MongoDB e real (ver MongoDeTeste): a comunicacao API <-> banco e exercitada
 *   de ponta a ponta, sem mocks.
 * - Cenarios BDD e testes de API herdam desta mesma classe, entao o Spring
 *   reaproveita um unico contexto (e um unico servidor) para toda a execucao.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(BaseIntegracao.ConfiguracaoDeTeste.class)
public abstract class BaseIntegracao {

    static {
        // Numeros decimais do JSON lidos como Double (o padrao do REST Assured e Float,
        // o que faria 83702.0 kg nao "bater" com 83702.0 nas comparacoes).
        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(NumberReturnType.DOUBLE));
        // Se uma verificacao falhar, a requisicao e a resposta completas saem no log do teste.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    static void mongoDb(DynamicPropertyRegistry registro) {
        registro.add("spring.data.mongodb.uri", MongoDeTeste::uri);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoDeTeste {

        /** Substitui o relogio do sistema em toda a aplicacao durante os testes. */
        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }

        @Bean
        BaseDeDadosDeTeste baseDeDadosDeTeste(MongoTemplate mongo, CargaInicial cargaInicial) {
            return new BaseDeDadosDeTeste(mongo, cargaInicial);
        }
    }
}
