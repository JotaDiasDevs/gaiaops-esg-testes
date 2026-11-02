# -*- coding: utf-8 -*-
"""
=============================================================================
GaiaOps ESG - FIAP | Testes Automatizados
tools/gerar_documento.py

Gera a documentacao da entrega:  docs/GaiaOps-ESG-Testes.docx
(converta para PDF com tools/gerar_pdf.ps1).

- Os cenarios Gherkin, contratos e trechos de configuracao sao LIDOS DOS
  ARQUIVOS REAIS do projeto: o documento nunca fica defasado.
- O resumo e o log da execucao local vem de docs/evidencias/ (saida real).
- Os prints de docs/prints/ entram automaticamente; os que faltam aparecem
  como um quadro laranja dizendo o que capturar.

Uso:  py tools/gerar_documento.py
=============================================================================
"""

import datetime
import glob
import os
import re

from documento_base import RAIZ, VERDE_ESCURO, VERDE_MEDIO, Documento, existe_print
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt

INTEGRANTES = [
    ("João Victor Semente Dias", "RM562065"),
]
REPOSITORIO = "https://github.com/JotaDiasDevs/gaiaops-esg-testes"
SAIDA_DOCX = os.path.join(RAIZ, "docs", "GaiaOps-ESG-Testes.docx")
RODAPE = "GaiaOps ESG · Testes automatizados (BDD + API) · FIAP"

SECOES = [
    "1. Objetivo e contexto ESG",
    "2. Estratégia de testes",
    "3. Cenários BDD em Gherkin",
    "4. Testes de API: status, corpo JSON e contrato",
    "5. Configuração",
    "6. Como executar os testes",
    "7. Evidências da execução local",
    "8. Evidências da execução no GitHub Actions",
    "9. Bug encontrado e desafios",
    "10. Tecnologias e atendimento aos requisitos",
]

PRINTS_CI = [
    ("ci-01-execucao", "Figura - Execução do workflow no GitHub Actions",
     "Actions > Testes automatizados > execução: os dois jobs verdes (Testes automatizados e Build da imagem Docker)."),
    ("ci-02-resumo", "Figura - Resumo da execução (Job Summary)",
     "Página da execução, aba Summary: tabelas de totais, cenários BDD e testes de API."),
    ("ci-03-passo-bdd", "Figura - Log do passo de cenários BDD e testes de API",
     "Job Testes automatizados > passo 2 expandido, mostrando os cenários do Cucumber e 'Tests run: 69'."),
    ("ci-04-relatorio-cucumber", "Figura - Relatório do Cucumber gerado no pipeline",
     "Artefato relatorio-cucumber > cucumber.html aberto no navegador: 100% dos cenários aprovados."),
]


# =============================================================================
# LEITURA DOS ARQUIVOS DO PROJETO
# =============================================================================

def ler(relativo):
    with open(os.path.join(RAIZ, relativo), encoding="utf-8") as f:
        return f.read()


def trecho(texto, inicio, fim=None, incluir_fim=False):
    i = texto.index(inicio)
    if fim is None:
        return texto[i:].rstrip()
    j = texto.index(fim, i + len(inicio))
    return texto[i:j + (len(fim) if incluir_fim else 0)].rstrip()


def sem_comentarios_java(texto):
    """Remove blocos /** */ e // de linha inteira, para listagens enxutas."""
    texto = re.sub(r"/\*\*.*?\*/\n", "", texto, flags=re.S)
    linhas = [l for l in texto.splitlines() if not l.strip().startswith("//")]
    return "\n".join(linhas)


def cenarios_da_feature(texto):
    """(tipo, titulo) de cada Cenario / Esquema do Cenario, com base na tag @positivo/@negativo."""
    resultado, tags = [], ""
    for linha in texto.splitlines():
        s = linha.strip()
        if s.startswith("@"):
            tags = s
        elif s.startswith("Cenário:") or s.startswith("Esquema do Cenário:"):
            tipo = "negativo" if "@negativo" in tags else "positivo"
            esquema = s.startswith("Esquema")
            titulo = s.split(":", 1)[1].strip()
            resultado.append((tipo, titulo + (" (Esquema do Cenário)" if esquema else "")))
            tags = ""
    return resultado


def tabelas_markdown(texto):
    """Converte as tabelas de um .md em listas de linhas (cabecalho incluso)."""
    tabelas, atual = [], []
    for linha in texto.splitlines():
        if linha.startswith("|"):
            celulas = [c.strip() for c in linha.strip().strip("|").split("|")]
            if all(re.fullmatch(r":?-{3,}:?", c) for c in celulas if c):
                continue
            atual.append([c.replace(":white_check_mark:", "OK").replace(":x:", "FALHA").replace("`", "")
                          for c in celulas])
        elif atual:
            tabelas.append(atual)
            atual = []
    if atual:
        tabelas.append(atual)
    return tabelas


# =============================================================================
# CONTEUDO
# =============================================================================

def capa(d):
    for _ in range(5):
        d.doc.add_paragraph()
    for texto, tamanho, cor, negrito in (
            ("GaiaOps ESG", 34, VERDE_ESCURO, True),
            ("Testes automatizados com BDD e testes de API", 17, VERDE_MEDIO, False),
            ("Cucumber (Gherkin) · REST Assured · JSON Schema · GitHub Actions", 12, None, False)):
        p = d.doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(texto)
        r.font.size, r.bold = Pt(tamanho), negrito
        if cor is not None:
            r.font.color.rgb = cor
    for _ in range(3):
        d.doc.add_paragraph()
    p = d.doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("FIAP · Fase Testes Automatizados")
    r.bold, r.font.size = True, Pt(12)
    d.doc.add_paragraph()
    d.tabela(["Integrante", "RM"], INTEGRANTES, larguras=[11, 4])
    p = d.doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run("Repositório: ").bold = True
    p.add_run(REPOSITORIO)
    p = d.doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run(datetime.date.today().strftime("%d/%m/%Y"))


def sumario(d):
    d.quebra_pagina()
    d.doc.add_heading("Sumário", level=1)
    for secao in SECOES:
        d.doc.add_paragraph(secao).paragraph_format.space_after = Pt(4)


def secao_objetivo(d):
    d.h1(SECOES[0])
    d.par("O **GaiaOps ESG** é a API de gestão ESG do município fictício de **Vila Nova do Horizonte**, "
          "desenvolvida em Java 21 + Spring Boot 3.5 + MongoDB nas atividades anteriores. Nesta fase, a API "
          "recebeu uma suíte de testes automatizados que valida suas funcionalidades críticas e, com elas, "
          "a contribuição da aplicação para cada pilar ESG.")
    d.tabela(["Exemplo do enunciado", "Como aparece no GaiaOps", "Onde é testado"], [
        ["Eficiência energética e sustentabilidade",
         "Medições de energia e água, alertas de perdas na rede; contrato do painel ESG",
         "02_ambiental_alertas_sensores.feature, MedicaoApiIT, IndicadorApiIT"],
        ["Gestão de resíduos e reciclagem",
         "Coleta seletiva, destinação e taxa de desvio de aterro (83.702 kg, 49% desviados)",
         "03_ambiental_gestao_residuos.feature, ResiduoApiIT"],
        ["Inclusão e diversidade",
         "Programas sociais, recurso de acessibilidade para pessoa com deficiência, beneficiários sem dados pessoais (LGPD)",
         "04_social_inclusao_programas.feature, ProgramaSocialApiIT, contrato programa-social"],
        ["Governança e compliance ambiental",
         "Integridade do cadastro, licenças vencidas, rastreabilidade da versão implantada",
         "01_governanca_cadastro_equipamentos.feature, 05_governanca_conformidade.feature"],
    ], larguras=[4, 7, 5.5])


def secao_estrategia(d):
    d.h1(SECOES[1])
    d.tabela(["Camada", "Ferramentas", "O que valida", "Quantidade"], [
        ["Cenários BDD", "Cucumber 7, Gherkin em português, REST Assured",
         "Comportamento de negócio ponta a ponta: caminhos felizes e de falha", "23 cenários (9 negativos)"],
        ["Testes de API", "REST Assured, JSON Schema Validator",
         "Todas as rotas: status code, corpo JSON e contrato", "46 testes"],
        ["Testes unitários", "JUnit 5, Mockito, MockMvc", "Regras de alerta e camada web isoladas", "17 testes"],
    ], larguras=[3, 4.5, 6.3, 2.7])
    d.h2("2.1 Arquitetura da execução")
    d.codigo(
        "   Cenários Gherkin (.feature)        Testes de API (classes ...ApiIT)\n"
        "              |                                    |\n"
        "     step definitions (Java)                       |\n"
        "              +---------------+--------------------+\n"
        "                              |  HTTP real (REST Assured)\n"
        "                              v\n"
        "            API GaiaOps ESG (Spring Boot, porta aleatória)\n"
        "                              |\n"
        "                              v\n"
        "      MongoDB real: container mongo:8.0 (Testcontainers)\n"
        "                    ou MongoDB local (TEST_MONGODB_URI)",
        "Figura 1 - Cenários BDD e testes de API usam a aplicação completa e um banco real")
    d.bullets([
        "**Comunicação real entre os serviços.** A API sobe completa num servidor HTTP em porta aleatória, e "
        "todas as chamadas passam pela rede, pelo Spring MVC, pela serialização JSON e pelo MongoDB. Não há mocks "
        "entre a API e o banco.",
        "**Um contexto para tudo.** Cenários BDD e testes de API herdam da mesma classe (BaseIntegracao). O "
        "Spring reaproveita uma única aplicação e um único banco durante toda a execução.",
        "**Dados isolados.** Antes de cada cenário e de cada teste, a base volta aos 100 documentos da carga "
        "inicial. Nenhum teste depende da ordem nem do que outro gravou.",
        "**Tempo controlado.** O relógio da aplicação é substituído por um relógio de teste (01/10/2026). Regras "
        "como \"licença vencida há 16 dias\" têm resultado estável em qualquer data, e um cenário pode ajustar "
        "a data (\"Dado que a data de referência é 01/09/2027\").",
    ])


def secao_bdd(d):
    d.h1(SECOES[2])
    d.par("Os cenários foram escritos em Gherkin, em português (`# language: pt`), do ponto de vista de quem "
          "usa a plataforma: gestor de governança, vigilância ambiental, coordenação da coleta seletiva, "
          "assistência social e auditoria. Cada funcionalidade combina **caminhos felizes** (`@positivo`) e "
          "**caminhos de falha** (`@negativo`), e as tags também indicam o pilar ESG (`@ambiental`, `@social`, "
          "`@governanca`).")
    arquivos = sorted(glob.glob(os.path.join(RAIZ, "src", "test", "resources", "features", "*.feature")))
    total = sum(len(cenarios_da_feature(ler(os.path.relpath(a, RAIZ)))) for a in arquivos)
    d.par("São **%d funcionalidades** e **%d cenários escritos**. Na execução, os *Esquemas do Cenário* se "
          "expandem em um cenário por linha de exemplos, totalizando 23 cenários executados." % (len(arquivos), total))

    for n, arquivo in enumerate(arquivos, 1):
        relativo = os.path.relpath(arquivo, RAIZ).replace("\\", "/")
        texto = ler(relativo)
        nome = re.search(r"Funcionalidade:\s*(.+)", texto).group(1).strip()
        d.h2("3.%d %s" % (n, nome))
        d.tabela(["Tipo", "Cenário"], [[t, c] for t, c in cenarios_da_feature(texto)], larguras=[2.3, 14.2])
        d.codigo(texto.strip(), "Listagem - %s" % relativo)

    d.h2("3.%d Como as frases viram código (step definitions)" % (len(arquivos) + 1))
    d.par("Cada frase do Gherkin corresponde a um método Java anotado. O \"Quando\" faz a chamada HTTP real e "
          "guarda a resposta no contexto do cenário; os \"Então\" verificam essa resposta. Trecho de "
          "`MedicaoSteps.java`:")
    passos = ler("src/test/java/br/com/fiap/gaiaops/bdd/MedicaoSteps.java")
    d.codigo(sem_comentarios_java(trecho(passos, '    @Quando("o sensor do equipamento {string} envia uma leitura de qualidade',
                                         '    @Entao("a leitura é registrada sem alerta")')),
             "Listagem - step definitions de medições")


def secao_api(d):
    d.h1(SECOES[3])
    d.par("Os testes de API ficam nas classes terminadas em `ApiIT` (uma por recurso). Cada teste faz uma chamada "
          "HTTP real e verifica o **status code**, o **corpo JSON** (valores, quantidades, ordenação e efeitos de "
          "uma gravação nas consultas seguintes) e o **contrato JSON Schema** da resposta.")
    d.tabela(["Rota", "Métodos", "Status testados", "Contratos"], [
        ["/api/equipamentos", "GET (filtros, limite), POST", "200, 201, 400, 409, 422", "equipamentos, equipamento, problema, problema-validacao"],
        ["/api/equipamentos/{codigo}", "GET, PUT, DELETE", "200, 204, 400, 404", "equipamento, problema, problema-validacao"],
        ["/api/medicoes", "GET (filtros, limite), POST", "200, 201, 400, 404, 422", "medicoes, medicao, problema, problema-validacao"],
        ["/api/medicoes/alertas", "GET (nível, abertos)", "200", "medicoes"],
        ["/api/residuos", "GET (categoria)", "200", "residuos"],
        ["/api/residuos/resumo", "GET", "200", "residuos-resumo"],
        ["/api/programas-sociais", "GET (eixo)", "200", "programas-sociais"],
        ["/api/programas-sociais/{codigo}", "GET", "200, 404", "programa-social, problema"],
        ["/api/conformidade", "GET (tipo)", "200", "conformidades"],
        ["/api/conformidade/licencas/vencimentos", "GET (dias)", "200", "licencas-vencimento"],
        ["/api/indicadores", "GET", "200", "painel-esg"],
        ["/actuator/health, /readiness, /liveness, /actuator/info", "GET", "200", "health, info"],
        ["/v3/api-docs, /, rota inexistente", "GET", "200, 404", "problema"],
    ], larguras=[5.2, 3.6, 3.1, 4.6])
    d.nota("**Garantia de que todas as APIs estão cobertas:** o teste `PlataformaApiIT.cobreTodasAsRotas` lê a "
           "documentação OpenAPI gerada pela própria aplicação e compara as rotas com a lista coberta pela suíte. "
           "Um endpoint novo sem teste faz esse teste falhar.")

    d.h2("4.1 Exemplo de teste de API")
    medicao = ler("src/test/java/br/com/fiap/gaiaops/api/MedicaoApiIT.java")
    d.codigo(trecho(medicao, "        @Test\n        @DisplayName(\"201: IQAr 151", "        @Test\n        @DisplayName(\"201: ruido"),
             "Listagem - MedicaoApiIT: status, corpo JSON e contrato numa só verificação")

    d.h2("4.2 Contratos JSON Schema")
    linhas = []
    for arquivo in sorted(glob.glob(os.path.join(RAIZ, "src", "test", "resources", "schemas", "*.json"))):
        titulo = re.search(r'"title":\s*"([^"]+)"', open(arquivo, encoding="utf-8").read()).group(1)
        linhas.append(["`%s`" % os.path.basename(arquivo), titulo])
    d.tabela(["Contrato", "Descreve"], linhas, larguras=[5, 11.5])
    d.par("Destaques: o contrato do painel ESG é **fechado** (`additionalProperties: false`), porque consumidores "
          "dependem exatamente daqueles indicadores. O contrato de equipamento exige os campos de governança e "
          "**aceita atributos livres** por tipo, preservando o esquema flexível do MongoDB. O contrato de programa "
          "social **proíbe dados pessoais** dos beneficiários, transformando uma regra da LGPD em verificação "
          "automática:")
    d.codigo(trecho(ler("src/test/resources/schemas/programa-social.json"), '"beneficiarios": {'),
             "Listagem - programa-social.json: protocolo pseudonimizado e campos pessoais proibidos")


def secao_configuracao(d):
    d.h1(SECOES[4])
    d.h2("5.1 Dependências de teste (pom.xml)")
    pom = ler("pom.xml")
    d.codigo(trecho(pom, "        <!-- BDD:", "    </dependencies>"), "Listagem - pom.xml: Cucumber, REST Assured e JSON Schema")
    d.codigo(trecho(pom, "        <cucumber.version>", "    </properties>"), "Listagem - pom.xml: versões e propriedades")
    d.h2("5.2 Runner do Cucumber")
    d.codigo(trecho(ler("src/test/java/br/com/fiap/gaiaops/bdd/CucumberIT.java"), "@Suite"),
             "Listagem - CucumberIT.java: features, glue e relatórios")
    d.h2("5.3 Ambiente de integração comum")
    d.codigo(sem_comentarios_java(trecho(ler("src/test/java/br/com/fiap/gaiaops/suporte/BaseIntegracao.java"),
                                         "@SpringBootTest")),
             "Listagem - BaseIntegracao.java: API em porta aleatória, MongoDB real, relógio e restauração da base")
    d.codigo(sem_comentarios_java(trecho(ler("src/test/java/br/com/fiap/gaiaops/suporte/MongoDeTeste.java"),
                                         "    public static synchronized String uri()", "    /** Impede")),
             "Listagem - MongoDeTeste.java: container descartável ou MongoDB local")
    d.h2("5.4 Pipeline de CI")
    d.codigo(trecho(ler(".github/workflows/testes.yml"), "jobs:"), "Listagem - .github/workflows/testes.yml")


def secao_execucao(d):
    d.h1(SECOES[5])
    d.par("Pré-requisito: JDK 21. O Maven vem embutido no projeto (Maven Wrapper). Para o banco de dados, basta "
          "**Docker** (o MongoDB sobe sozinho em container) **ou** um **MongoDB instalado**.")
    d.tabela(["Objetivo", "Comando"], [
        ["Tudo, com Docker (igual ao pipeline)", "`./mvnw verify`"],
        ["Tudo, com MongoDB local (Linux/macOS/Git Bash)", "`export TEST_MONGODB_URI=mongodb://localhost:27017/gaiaops_testes` e `./mvnw verify`"],
        ["Tudo, com MongoDB local (PowerShell)", "`$env:TEST_MONGODB_URI = \"mongodb://localhost:27017/gaiaops_testes\"` e `.\\mvnw.cmd verify`"],
        ["Só testes unitários", "`./mvnw test`"],
        ["Só cenários BDD e testes de API", "`./mvnw verify -DskipUnitTests=true`"],
        ["Só os cenários BDD", "`./mvnw verify -DskipUnitTests=true -Dit.test=CucumberIT`"],
        ["Só cenários de uma tag", "`./mvnw verify -DskipUnitTests=true -Dit.test=CucumberIT \"-Dcucumber.filter.tags=@negativo\"`"],
        ["Só os testes de uma API", "`./mvnw verify -DskipUnitTests=true -Dit.test=MedicaoApiIT`"],
    ], larguras=[5, 11.5])
    d.nota("Por segurança, no modo MongoDB local o nome do banco precisa conter \"teste\": os testes apagam e "
           "recarregam as collections a cada cenário, e essa trava impede apontá-los para uma base com dados reais.")
    d.tabela(["Relatório gerado", "Conteúdo"], [
        ["target/cucumber-reports/cucumber.html", "Relatório navegável do Cucumber: cenários, passos, resultado e tempo"],
        ["target/cucumber-reports/cucumber.json e .xml", "Mesmo resultado para ferramentas de CI"],
        ["target/failsafe-reports e surefire-reports", "Resultados JUnit dos testes de API e unitários"],
        ["target/site/jacoco/index.html", "Cobertura de código da aplicação"],
    ], larguras=[6.5, 10])


def secao_evidencias_local(d):
    d.h1(SECOES[6])
    d.par("Execução completa (`./mvnw clean verify`) em uma estação Windows 11 com JDK 21 e MongoDB 8 local, no "
          "modo `TEST_MONGODB_URI=mongodb://localhost:27017/gaiaops_testes`. As tabelas abaixo foram geradas pelo "
          "script de resumo a partir dos relatórios reais dessa execução.")
    resumo = os.path.join(RAIZ, "docs", "evidencias", "resumo-execucao-local.md")
    if os.path.exists(resumo):
        tabelas = tabelas_markdown(open(resumo, encoding="utf-8").read())
        totais = tabelas[0]
        d.tabela(totais[0], totais[1:], larguras=[4.5, 5, 2.4, 2.2, 2.4])
        m = re.search(r"JaCoCo\): \*\*([\d.]+)%", open(resumo, encoding="utf-8").read())
        if m:
            d.par("Cobertura de linhas da aplicação (JaCoCo): **%s%%**" % m.group(1).replace(".", ","))

    log = os.path.join(RAIZ, "docs", "evidencias", "execucao-local-mvn-verify.log")
    if os.path.exists(log):
        linhas = [l.replace("[INFO] ", "").replace("[WARNING] ", "").rstrip()
                  for l in open(log, encoding="utf-8", errors="replace")
                  if re.search(r"Tests run: \d+, Failures|BUILD (SUCCESS|FAILURE)|Total time", l)]
        d.codigo("\n".join(linhas), "Listagem - saída real do Maven (docs/evidencias/execucao-local-mvn-verify.log)")

    d.h2("7.1 Relatório do Cucumber")
    for nome, legenda in (
            ("local-01-cucumber-visao-geral", "Visão geral: 100% (23/23) dos cenários aprovados"),
            ("local-02-cucumber-governanca-equipamentos", "Cadastro íntegro dos equipamentos públicos"),
            ("local-03-cucumber-ambiental-alertas", "Alertas automáticos da telemetria urbana"),
            ("local-04-cucumber-ambiental-residuos", "Gestão de resíduos e reciclagem"),
            ("local-05-cucumber-social-programas", "Inclusão, diversidade e privacidade"),
            ("local-06-cucumber-governanca-conformidade", "Conformidade regulatória e rastreabilidade")):
        d.print_tela(nome, "Figura - Relatório do Cucumber: " + legenda, "Execute ./mvnw verify e capture o cucumber.html.")


def secao_evidencias_ci(d):
    d.h1(SECOES[7])
    d.par("O workflow `Testes automatizados` roda a cada push na branch main e sob demanda. A execução abaixo usa "
          "um runner Linux do GitHub, com o MongoDB 8.0 em container criado pelo Testcontainers.")
    for nome, legenda, instrucao in PRINTS_CI:
        d.print_tela(nome, legenda, instrucao)


def secao_desafios(d):
    d.h1(SECOES[8])
    d.h2("9.1 Bug real encontrado pelos testes")
    d.par("**Problema.** Na primeira execução, os testes de API mostraram que o mapa `erros` das respostas de "
          "validação usava o nome do atributo Java (`populacaoAtendida`, `equipamentoId`) em vez do nome do campo "
          "no JSON (`populacao_atendida`, `equipamento_id`). O cliente enviava um campo e o erro apontava para "
          "outro, inexistente no contrato da API.")
    d.par("**Correção.** O `TratadorDeErros` passou a traduzir o nome com a mesma estratégia de nomes do Jackson, "
          "e um teste unitário novo (`errosDeValidacaoUsamOsNomesDeCampoDoJson`) impede a regressão.")
    d.codigo(trecho(ler("src/main/java/br/com/fiap/gaiaops/comum/TratadorDeErros.java"),
                    "    private String nomeNoJson(String campoJava) {", "    private static ProblemDetail"),
             "Listagem - correção em TratadorDeErros.java")
    d.h2("9.2 Desafios e soluções")
    d.tabela(["Desafio", "Solução"], [
        ["Contrato assumia que todo beneficiário tinha protocolo BNF-; os dados reais têm também turmas (TRM-6A-ALZ)",
         "Contrato revisado com base nos dados: qualquer pseudônimo no formato XXX-..., mantendo proibidos nome, CPF, RG, e-mail e telefone"],
        ["Cucumber 7.34 é compilado para JUnit 5.14, e o Spring Boot 3.5 traz o 5.12",
         "Versão do JUnit alinhada no pom.xml (junit-jupiter.version), evitando erros de compatibilidade da JUnit Platform"],
        ["O conversor de inteiros do Cucumber lê \"09\" (setembro) como número octal",
         "Data do cenário lida como texto e convertida com DateTimeFormatter (dd/MM/yyyy)"],
        ["REST Assured lê decimais do JSON como Float, e 83702.0 não \"batia\" com o Double esperado",
         "Configuração global do REST Assured para ler decimais como Double"],
        ["Estação de desenvolvimento sem Docker",
         "Modo duplo: Testcontainers no pipeline e MongoDB local via TEST_MONGODB_URI, com trava que exige um banco descartável"],
        ["Resultados que dependem da data de execução (licenças vencidas)",
         "Relógio de teste injetado na aplicação, com data fixa e ajustável por cenário"],
        ["Testes que interferem uns nos outros",
         "Restauração da carga inicial antes de cada cenário e de cada teste"],
        ["Acentos dos cenários ilegíveis no console do Windows",
         "Somente visual: relatórios em UTF-8 corretos; README orienta usar chcp 65001"],
    ], larguras=[7.5, 9])


def secao_tecnologias(d):
    d.h1(SECOES[9])
    d.tabela(["Camada", "Tecnologias"], [
        ["Aplicação", "Java 21, Spring Boot 3.5 (Web, Validation, Data MongoDB, Actuator), springdoc-openapi"],
        ["Banco de dados", "MongoDB 8.0"],
        ["BDD", "Cucumber 7 (cucumber-java, cucumber-spring, cucumber-junit-platform-engine), Gherkin em português"],
        ["Testes de API", "REST Assured 5, REST Assured JSON Schema Validator (draft-04)"],
        ["Base de testes", "JUnit 5 + JUnit Platform Suite, Spring Boot Test, Testcontainers, Mockito, AssertJ/Hamcrest"],
        ["Cobertura", "JaCoCo"],
        ["Build e CI", "Maven Wrapper, GitHub Actions, Docker, Docker Compose"],
    ], larguras=[3.5, 13])
    d.h2("10.1 Atendimento aos requisitos")
    d.tabela(["Requisito", "Como foi atendido"], [
        ["No mínimo 3 cenários BDD em Gherkin, positivos e negativos",
         "5 funcionalidades e 17 cenários escritos (23 executados), sendo 9 de falha, todos em Gherkin (seção 3)"],
        ["Testes de API para todas as APIs", "11 rotas /api e as APIs de plataforma; cobertura total verificada contra o OpenAPI (seção 4)"],
        ["Validação de status code", "Todos os testes de API e cenários BDD verificam o status HTTP"],
        ["Validação do corpo de resposta (JSON)", "Valores, quantidades, ordenação e efeitos colaterais verificados com REST Assured"],
        ["Testes de contrato com JSON Schema", "17 contratos em src/test/resources/schemas, aplicados a todas as respostas"],
        ["Execução simples e documentada, local ou em CI",
         "./mvnw verify (com Docker ou MongoDB local) e workflow do GitHub Actions; README e seção 6"],
        ["Código-fonte em .zip com testes, Dockerfiles, scripts e README", "Pacote gerado por tools/empacotar-entrega.ps1"],
        ["Documentação com cenários Gherkin, configurações e evidências", "Este documento (seções 3, 5, 7 e 8)"],
    ], larguras=[6, 10.5])


def main():
    d = Documento(RODAPE)
    capa(d)
    sumario(d)
    secao_objetivo(d)
    secao_estrategia(d)
    secao_bdd(d)
    secao_api(d)
    secao_configuracao(d)
    secao_execucao(d)
    secao_evidencias_local(d)
    secao_evidencias_ci(d)
    secao_desafios(d)
    secao_tecnologias(d)
    d.salvar(SAIDA_DOCX)

    pendentes = [n for n, _, _ in PRINTS_CI if not existe_print(n)]
    print("Documento gerado: %s" % os.path.relpath(SAIDA_DOCX, RAIZ))
    if pendentes:
        print("Prints pendentes (%d):" % len(pendentes))
        for nome in pendentes:
            print("  - docs/prints/%s.png" % nome)
    else:
        print("Todos os prints presentes.")


if __name__ == "__main__":
    main()
