# language: pt
@ambiental @medicoes
Funcionalidade: Alertas automáticos da telemetria urbana
  Como equipe de vigilância ambiental do município
  Quero que leituras fora dos limites legais gerem alertas automaticamente
  Para agir rápido em situações que afetam a saúde da população

  Contexto:
    Dado que a base ESG do município está carregada com 12 equipamentos

  @positivo
  Esquema do Cenário: Classificar o alerta de qualidade do ar pelo IQAr (<situacao>)
    Quando o sensor do equipamento "EQP-PRA-002" envia uma leitura de qualidade do ar com IQAr <iqar>
    Então a API responde com o status 201
    E a resposta segue o contrato "medicao"
    E a leitura gera um alerta de nível "<nivel>"

    Exemplos:
      | situacao          | iqar | nivel   |
      | ar ruim           | 151  | critico |
      | ar moderado       | 95   | medio   |

  @positivo
  Cenário: Leitura dentro dos limites é registrada sem alerta
    Quando o sensor do equipamento "EQP-PRA-002" envia uma leitura de qualidade do ar com IQAr 60
    Então a API responde com o status 201
    E a resposta segue o contrato "medicao"
    E a leitura é registrada sem alerta

  @positivo
  Cenário: Córrego acima da cota de alerta dispara alerta crítico e entra no painel
    Quando o sensor do equipamento "EQP-PRA-001" envia uma leitura de "hidrologia" com:
      | bloco      | campo            | valor |
      | leitura    | nivel_cm         | 251   |
      | referencia | nivel_atencao_cm | 180   |
      | referencia | nivel_alerta_cm  | 240   |
    Então a API responde com o status 201
    E a leitura gera um alerta de nível "critico"
    E o alerta aparece entre os alertas abertos de nível "critico"
    E o painel ESG passa a ter 7 alertas abertos

  @negativo
  Cenário: Equipamento desativado não recebe leituras
    Dado que o equipamento "EQP-TER-001" foi desativado
    Quando o sensor do equipamento "EQP-TER-001" envia uma leitura de "ruido" com:
      | bloco   | campo   | valor |
      | leitura | laeq_db | 60    |
    Então a API responde com o status 422
    E a resposta segue o contrato "problema"
    E a mensagem de erro contém "desativado"

  @negativo
  Cenário: Leitura de sensor de equipamento inexistente é recusada
    Quando o sensor do equipamento "EQP-XYZ-999" envia uma leitura de qualidade do ar com IQAr 80
    Então a API responde com o status 404
    E a resposta segue o contrato "problema"
    E o painel ESG passa a ter 6 alertas abertos
