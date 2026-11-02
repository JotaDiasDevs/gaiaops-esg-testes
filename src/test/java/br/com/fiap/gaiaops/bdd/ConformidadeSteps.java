package br.com.fiap.gaiaops.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Pilar G: licencas ambientais, prazos legais e rastreabilidade da versao implantada. */
public class ConformidadeSteps {

    private final ContextoDoCenario contexto;

    public ConformidadeSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Data no formato dd/mm/aaaa; o relogio da API passa a marcar o inicio desse dia (UTC).
     * Lida como texto: o conversor de inteiros do Cucumber interpreta "09" como octal.
     */
    @Dado("^que a data de referência é (\\d{2}/\\d{2}/\\d{4})$")
    public void queADataDeReferenciaE(String data) {
        LocalDate dia = LocalDate.parse(data, DATA_BR);
        contexto.relogio().ajustar(dia.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    @Quando("consulto as licenças que vencem nos próximos {int} dias")
    public void consultoAsLicencasQueVencem(int dias) {
        contexto.registrar(contexto.api().queryParam("dias", dias).get("/api/conformidade/licencas/vencimentos"));
    }

    @Quando("consulto as informações da versão implantada")
    public void consultoAsInformacoesDaVersao() {
        contexto.registrar(contexto.api().get("/actuator/info"));
    }

    @Entao("a licença {string} aparece como vencida há {int} dias")
    public void aLicencaApareceComoVencida(String numero, int dias) {
        Map<String, Object> licenca = licenca(numero);
        assertThat(licenca.get("vencida"), equalTo(true));
        assertThat(((Number) licenca.get("dias_para_vencer")).intValue(), equalTo(-dias));
    }

    @Entao("a licença {string} aparece a vencer em {int} dias")
    public void aLicencaApareceAVencer(String numero, int dias) {
        Map<String, Object> licenca = licenca(numero);
        assertThat(licenca.get("vencida"), equalTo(false));
        assertThat(((Number) licenca.get("dias_para_vencer")).intValue(), equalTo(dias));
    }

    @Entao("a licença {string} não aparece na lista")
    public void aLicencaNaoApareceNaLista(String numero) {
        contexto.validar().body("numero", not(hasItem(numero)));
    }

    @Entao("a resposta identifica a aplicação {string} na versão {string}")
    public void aRespostaIdentificaAAplicacao(String nome, String versao) {
        contexto.validar()
                .body("app.nome", equalTo(nome))
                .body("app.versao", equalTo(versao))
                .body("build.version", equalTo(versao));
    }

    private Map<String, Object> licenca(String numero) {
        List<Map<String, Object>> encontradas = contexto.resposta().jsonPath()
                .getList("findAll { it.numero == '%s' }".formatted(numero));
        assertThat("licenca " + numero + " na resposta", encontradas, hasSize(1));
        return encontradas.get(0);
    }
}
