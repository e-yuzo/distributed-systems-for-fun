3)  Faça um servidor para processar as seguintes mensagens dos clientes. O
servidor deve suportar mensagens de múltiplos clientes. Use o TCP.
As mensagens estão no formato String UTF:

TIME
* Retorna a hora do sistema como uma String UTF no formato HH:MM:SS

DATE
* Retorna a data do sistema como uma String UTF no formato DD/MM/AAAA

FILES
* Retorna os arquivos da pasta definida por padrao (p. ex. /home/user/shared)
* retorna um inteiro indicando o número de arquivos
* envia o nome de um arquivo por vez como uma String UTF

DOWN nome-arquivo
* Faz o download do arquivo nome-arquivo
* retorna 0 se nome não existe ou retorna o tamanho do arquivo
* lê o número de bytes indicado por tamanho do arquivo e grava em um diretório
padrão

EXIT
* Finaliza a conexão
