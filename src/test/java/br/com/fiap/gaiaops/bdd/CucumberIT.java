package br.com.fiap.gaiaops.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Ponto de entrada dos cenarios BDD.
 *
 * Executa todos os arquivos .feature de src/test/resources/features com o
 * Cucumber, na fase "verify" do Maven (o sufixo IT faz o failsafe executa-la).
 *
 * Relatorios gerados em target/cucumber-reports/:
 *   cucumber.html - relatorio navegavel (evidencia de execucao)
 *   cucumber.json - usado pelo resumo do GitHub Actions
 *   cucumber.xml  - formato JUnit, lido por ferramentas de CI
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.fiap.gaiaops.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,"
        + " html:target/cucumber-reports/cucumber.html,"
        + " json:target/cucumber-reports/cucumber.json,"
        + " junit:target/cucumber-reports/cucumber.xml")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberIT {
}
