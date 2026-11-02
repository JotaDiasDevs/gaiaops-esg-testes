package br.com.fiap.gaiaops.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Pilar S: inclusao, acessibilidade e privacidade (LGPD) nos programas sociais. */
public class ProgramaSocialSteps {

    private final ContextoDoCenario contexto;

    public ProgramaSocialSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("consulto o programa social {string}")
    public void consultoOProgramaSocial(String codigo) {
        contexto.registrar(contexto.api().get("/api/programas-sociais/{codigo}", codigo));
    }

    @Quando("consulto todos os programas sociais")
    public void consultoTodosOsProgramasSociais() {
        contexto.registrar(contexto.api().get("/api/programas-sociais"));
    }

    @Entao("o programa atende um beneficiário com deficiência com o recurso {string}")
    public void oProgramaAtendeBeneficiarioComDeficiencia(String recurso) {
        contexto.validar().body("beneficiarios.findAll { it.pcd == true }.acessibilidade.recurso", hasItem(recurso));
    }

    /** Recebe a lista de campos proibidos separada por virgulas, ex.: "nome, cpf". */
    @Entao("nenhum beneficiário expõe os campos {string}")
    public void nenhumBeneficiarioExpoeOsCampos(String camposProibidos) {
        List<Map<String, Object>> beneficiarios = todosOsBeneficiarios();
        for (String campo : Arrays.stream(camposProibidos.split(",")).map(String::trim).toList()) {
            assertThat("campo pessoal '" + campo + "' exposto", beneficiarios, everyItem(not(hasKey(campo))));
        }
    }

    @Entao("todo beneficiário é identificado apenas por protocolo")
    public void todoBeneficiarioEIdentificadoPorProtocolo() {
        List<String> protocolos = todosOsBeneficiarios().stream()
                .map(b -> String.valueOf(b.get("protocolo")))
                .toList();
        // Pseudonimo no formato XXX-..., ex.: BNF-2026-0311 (pessoa) ou TRM-6A-ALZ (turma).
        assertThat(protocolos, everyItem(matchesPattern("[A-Z]{3}-[A-Z0-9-]+")));
    }

    @Entao("o painel social soma {int} beneficiários em {int} programas ativos")
    public void oPainelSocialSoma(int beneficiarios, int programas) {
        contexto.validar()
                .body("social.beneficiarios_acompanhados", equalTo(beneficiarios))
                .body("social.programas_ativos", equalTo(programas));
    }

    @Entao("a execução orçamentária dos programas é de {double}%")
    public void aExecucaoOrcamentariaE(double percentual) {
        contexto.validar().body("social.execucao_orcamentaria_pct", closeTo(percentual, 0.01));
    }

    private List<Map<String, Object>> todosOsBeneficiarios() {
        return contexto.resposta().jsonPath().getList("beneficiarios.flatten()");
    }
}
