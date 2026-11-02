"""
GaiaOps ESG - resumo da execucao dos testes (Job Summary do GitHub Actions).

Le os relatorios gerados pelo Maven e escreve tabelas em Markdown:
  - target/surefire-reports/*.xml   testes unitarios
  - target/failsafe-reports/*.xml   testes de API (*ApiIT) e a suite BDD (CucumberIT)
  - target/cucumber-reports/cucumber.json   resultado de cada cenario Gherkin
  - target/site/jacoco/jacoco.csv   cobertura de linhas

No GitHub Actions o texto vai para $GITHUB_STEP_SUMMARY; fora dele, para o terminal.

Uso (na raiz do projeto, depois de ./mvnw verify):
    python3 .github/scripts/resumo_testes.py
"""
import csv
import glob
import json
import os
import xml.etree.ElementTree as ET
from collections import OrderedDict

CUCUMBER = "target/cucumber-reports/cucumber.json"
COBERTURA = "target/site/jacoco/jacoco.csv"
OK, FALHA, IGNORADO = ":white_check_mark:", ":x:", ":fast_forward:"


def casos_junit(padrao):
    """Lista de (classe de topo, nome do teste, situacao) a partir dos XML do JUnit."""
    casos = []
    for arquivo in sorted(glob.glob(padrao)):
        for caso in ET.parse(arquivo).getroot().iter("testcase"):
            classe = caso.get("classname", "").split(".")[-1].split("$")[0]
            if caso.find("failure") is not None or caso.find("error") is not None:
                situacao = "falha"
            elif caso.find("skipped") is not None:
                situacao = "ignorado"
            else:
                situacao = "ok"
            casos.append((classe, caso.get("name", ""), situacao))
    return casos


def agrupar(casos):
    grupos = OrderedDict()
    for classe, _, situacao in casos:
        g = grupos.setdefault(classe, {"ok": 0, "falha": 0, "ignorado": 0})
        g[situacao] += 1
    return grupos


def cenarios_cucumber():
    """(funcionalidade, cenario, tags, situacao) de cada cenario executado."""
    if not os.path.exists(CUCUMBER):
        return []
    with open(CUCUMBER, encoding="utf-8") as f:
        funcionalidades = json.load(f)
    linhas = []
    for funcionalidade in funcionalidades:
        for elemento in funcionalidade.get("elements", []):
            if elemento.get("type") != "scenario":
                continue
            passos = elemento.get("steps", []) + [h for h in elemento.get("before", []) + elemento.get("after", [])]
            estados = {p.get("result", {}).get("status") for p in passos}
            situacao = "falha" if estados & {"failed", "undefined", "ambiguous"} else (
                "ignorado" if estados & {"skipped", "pending"} else "ok")
            tags = " ".join(t["name"] for t in elemento.get("tags", []))
            linhas.append((funcionalidade.get("name", ""), elemento.get("name", ""), tags, situacao))
    return linhas


def cobertura():
    if not os.path.exists(COBERTURA):
        return None
    cobertas = perdidas = 0
    with open(COBERTURA, encoding="utf-8") as f:
        for linha in csv.DictReader(f):
            cobertas += int(linha["LINE_COVERED"])
            perdidas += int(linha["LINE_MISSED"])
    total = cobertas + perdidas
    return 100 * cobertas / total if total else 0.0


def icone(situacao):
    return {"ok": OK, "falha": FALHA, "ignorado": IGNORADO}[situacao]


def main():
    unitarios = casos_junit("target/surefire-reports/TEST-*.xml")
    integracao = casos_junit("target/failsafe-reports/TEST-*.xml")
    api = [c for c in integracao if c[0] != "CucumberIT"]
    cenarios = cenarios_cucumber()

    def totais(itens, indice):
        return (sum(1 for i in itens if i[indice] == "ok"),
                sum(1 for i in itens if i[indice] == "falha"),
                sum(1 for i in itens if i[indice] == "ignorado"))

    out = ["## Testes automatizados - GaiaOps ESG", "",
           "| Tipo | Ferramentas | Aprovados | Falhas | Ignorados |",
           "|---|---|---:|---:|---:|"]
    for nome, ferramentas, itens, indice in (
            ("Cenarios BDD (Gherkin)", "Cucumber + REST Assured", cenarios, 3),
            ("Testes de API", "REST Assured + JSON Schema", api, 2),
            ("Testes unitarios", "JUnit 5 + Mockito + MockMvc", unitarios, 2)):
        ok, falha, ignorado = totais(itens, indice)
        out.append(f"| {nome} | {ferramentas} | {ok} | {falha} | {ignorado} |")

    cob = cobertura()
    if cob is not None:
        out += ["", f"Cobertura de linhas da aplicacao (JaCoCo): **{cob:.1f}%**"]

    if cenarios:
        out += ["", "### Cenarios BDD", "", "| | Funcionalidade | Cenario | Tags |", "|---|---|---|---|"]
        for funcionalidade, cenario, tags, situacao in cenarios:
            out.append(f"| {icone(situacao)} | {funcionalidade} | {cenario} | `{tags}` |")

    if api:
        out += ["", "### Testes de API (status, corpo JSON e contrato JSON Schema)", "",
                "| | Classe | Aprovados | Falhas | Ignorados |", "|---|---|---:|---:|---:|"]
        for classe, g in agrupar(api).items():
            situacao = "falha" if g["falha"] else ("ignorado" if g["ok"] == 0 else "ok")
            out.append(f"| {icone(situacao)} | `{classe}` | {g['ok']} | {g['falha']} | {g['ignorado']} |")

    if unitarios:
        out += ["", "### Testes unitarios", "", "| | Classe | Aprovados | Falhas |", "|---|---|---:|---:|"]
        for classe, g in agrupar(unitarios).items():
            out.append(f"| {icone('falha' if g['falha'] else 'ok')} | `{classe}` | {g['ok']} | {g['falha']} |")

    texto = "\n".join(out) + "\n"
    destino = os.environ.get("GITHUB_STEP_SUMMARY")
    if destino:
        with open(destino, "a", encoding="utf-8") as f:
            f.write(texto)
    print(texto)


if __name__ == "__main__":
    main()
