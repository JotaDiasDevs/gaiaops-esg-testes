## Testes automatizados - GaiaOps ESG

| Tipo | Ferramentas | Aprovados | Falhas | Ignorados |
|---|---|---:|---:|---:|
| Cenarios BDD (Gherkin) | Cucumber + REST Assured | 23 | 0 | 0 |
| Testes de API | REST Assured + JSON Schema | 46 | 0 | 0 |
| Testes unitarios | JUnit 5 + Mockito + MockMvc | 17 | 0 | 0 |

Cobertura de linhas da aplicacao (JaCoCo): **97.6%**

### Cenarios BDD

| | Funcionalidade | Cenario | Tags |
|---|---|---|---|
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Cadastrar uma nova escola municipal | `@governanca @equipamentos @positivo` |
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Recusar cadastro que fere as regras de integridade (código fora do padrão) | `@governanca @equipamentos @negativo` |
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Recusar cadastro que fere as regras de integridade (tipo inexistente) | `@governanca @equipamentos @negativo` |
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Recusar cadastro que fere as regras de integridade (nome curto demais) | `@governanca @equipamentos @negativo` |
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Impedir dois equipamentos com o mesmo código | `@governanca @equipamentos @negativo` |
| :white_check_mark: | Cadastro íntegro dos equipamentos públicos | Desativar um equipamento preserva o histórico | `@governanca @equipamentos @positivo @rastreabilidade` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Classificar o alerta de qualidade do ar pelo IQAr (ar ruim) | `@ambiental @medicoes @positivo` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Classificar o alerta de qualidade do ar pelo IQAr (ar moderado) | `@ambiental @medicoes @positivo` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Leitura dentro dos limites é registrada sem alerta | `@ambiental @medicoes @positivo` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Córrego acima da cota de alerta dispara alerta crítico e entra no painel | `@ambiental @medicoes @positivo` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Equipamento desativado não recebe leituras | `@ambiental @medicoes @negativo` |
| :white_check_mark: | Alertas automáticos da telemetria urbana | Leitura de sensor de equipamento inexistente é recusada | `@ambiental @medicoes @negativo` |
| :white_check_mark: | Gestão de resíduos e reciclagem | Painel ESG calcula a taxa de desvio de aterro | `@ambiental @residuos @positivo` |
| :white_check_mark: | Gestão de resíduos e reciclagem | Resumo por categoria mostra para onde vai cada resíduo | `@ambiental @residuos @positivo` |
| :white_check_mark: | Gestão de resíduos e reciclagem | Consulta de categoria sem coletas não inventa dados | `@ambiental @residuos @negativo` |
| :white_check_mark: | Inclusão, diversidade e privacidade nos programas sociais | Programa de qualificação garante recurso de acessibilidade a beneficiário com deficiência | `@social @programas @positivo @inclusao` |
| :white_check_mark: | Inclusão, diversidade e privacidade nos programas sociais | Nenhum programa expõe dados pessoais dos beneficiários | `@social @programas @positivo @lgpd` |
| :white_check_mark: | Inclusão, diversidade e privacidade nos programas sociais | Painel social consolida beneficiários e execução orçamentária | `@social @programas @positivo` |
| :white_check_mark: | Inclusão, diversidade e privacidade nos programas sociais | Consulta de programa inexistente | `@social @programas @negativo` |
| :white_check_mark: | Conformidade regulatória e rastreabilidade | Licença vencida é sinalizada mesmo com a situação desatualizada no cadastro | `@governanca @conformidade @positivo` |
| :white_check_mark: | Conformidade regulatória e rastreabilidade | Licença de operação entra no radar de renovação perto do vencimento | `@governanca @conformidade @positivo` |
| :white_check_mark: | Conformidade regulatória e rastreabilidade | Versão implantada é rastreável | `@governanca @conformidade @positivo @rastreabilidade` |
| :white_check_mark: | Conformidade regulatória e rastreabilidade | Prazo negativo não esconde licenças já vencidas | `@governanca @conformidade @negativo` |

### Testes de API (status, corpo JSON e contrato JSON Schema)

| | Classe | Aprovados | Falhas | Ignorados |
|---|---|---:|---:|---:|
| :white_check_mark: | `API /api/conformidade` | 5 | 0 | 0 |
| :white_check_mark: | `DELETE /api/equipamentos/{codigo}` | 2 | 0 | 0 |
| :white_check_mark: | `PUT /api/equipamentos/{codigo}` | 3 | 0 | 0 |
| :white_check_mark: | `POST /api/equipamentos` | 6 | 0 | 0 |
| :white_check_mark: | `GET /api/equipamentos/{codigo}` | 2 | 0 | 0 |
| :white_check_mark: | `GET /api/equipamentos` | 3 | 0 | 0 |
| :white_check_mark: | `API /api/indicadores` | 2 | 0 | 0 |
| :white_check_mark: | `POST /api/medicoes` | 5 | 0 | 0 |
| :white_check_mark: | `GET /api/medicoes/alertas` | 2 | 0 | 0 |
| :white_check_mark: | `GET /api/medicoes` | 3 | 0 | 0 |
| :white_check_mark: | `APIs de plataforma (Actuator, OpenAPI e pagina inicial)` | 6 | 0 | 0 |
| :white_check_mark: | `API /api/programas-sociais` | 4 | 0 | 0 |
| :white_check_mark: | `API /api/residuos` | 3 | 0 | 0 |

### Testes unitarios

| | Classe | Aprovados | Falhas |
|---|---|---:|---:|
| :white_check_mark: | `EquipamentoControllerTest` | 8 | 0 |
| :white_check_mark: | `RegrasDeAlertaTest` | 9 | 0 |

