# Semantic Search and Recommendation in Series Data

### Compilação do código
Este código foi desenvolvido utilizado o IDE `IntelliJ IDEA 2016.2.5`, e facilmente será compilado nessa plataforma. No IDE, estão também definidos três comandos executáveis do projeto, para correr o código de população da ontologia (Populate), para correr o código de teste com linha de comandos (Series) e para correr o código do servidor web (Web).

### Geração dos dados
Para gerar os dados usados na população da ontologia, deve ser executado o script `data/getData.py`, que irá criar os ficheiros `data/popcorn.json` e `data/series.json`. Este último deve ser copiado para a pasta `series`, para ser usado pela aplicação.

### População da ontologia
Para popular a ontologia, deve ser executada a class `Populate`, ou usado o comando `Populate` definido no IntelliJ. Este irá usar o ficheiro JSON gerado no passo anterior, e o ficheiro `series/series.rdf`, gerado com o `Protege`.

### Testes
De forma a testar o resultado final do passo anterior, pode ser executada a class `Main`, ou usado o comando `Series` do IntelliJ. Através de uma interface CLI, são disponibilizados vários testes à ontologia.

### Servidor web
Executando a class `Web`, usando o comando `Web` do IntelliJ, ou fazendo _deploy_ do artefacto `series/out/artifacts/web_war/web_war.war` é executado o servidor spark, e a aplicação pode ser consultada em `localhost:4567`.
