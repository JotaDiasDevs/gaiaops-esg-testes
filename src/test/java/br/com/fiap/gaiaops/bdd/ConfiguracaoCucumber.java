package br.com.fiap.gaiaops.bdd;

import br.com.fiap.gaiaops.suporte.BaseIntegracao;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Liga o Cucumber ao Spring: os cenarios rodam contra a aplicacao completa,
 * no mesmo ambiente (API real + MongoDB real) dos testes de API.
 */
@CucumberContextConfiguration
public class ConfiguracaoCucumber extends BaseIntegracao {
}
