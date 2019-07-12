1) Fazer uma aplicação P2P que consulta um padrão de nome de arquivo em um diretório padrão.  A mensagem deve possuir o seguinte formato:

Requisição:
- 1 byte para requisição ou resposta (''1" - requisição, "2" - resposta)
- 2 bytes para tipos de arquivos ( primeiro byte == 1 arquivo, segundo byte ==1 diretório, ambos marcados com 1, arquivos e diretórios).
- expressão de consulta em bytes.

Resposta:
- 1 byte para resposta (2)
- resultado da consulta: 1 bytes para tamanho do nome do arquivo seguido pelo nome do arquivo. (se tamanho for 0, significa que não há ou acabou a listagem).
