5) Faça uma aplicação com um servidor que gerencia um conjunto de arquivos
remotos entre múltiplos usuários.  O servidor deve responder aos seguintes
comandos:

-> ADDFILE (1): adiciona um arquivo novo. (OPCIONAL)
-> DELETE (2):  remove um arquivo existente.
-> GETFILESLIST (3): retorna uma lista com o nome dos arquivos.
-> GETFILE (4): faz download de um arquivo.

O servidor deve registrar as ações em logs.
As solicitações possuem o seguinte formato:

1 byte: requisição(1)
1 byte: código do comando
1 byte: tamanho do nome do arquivo
variável: nome do arquivo (0-255 bytes)

As respostas possuem o seguinte formato:

1 byte: resposta(2)
1 byte: código do comando
1 byte: status code (1-SUCCESS, 2-ERROR)

----

para o GETFILESLIST adiciona-se os campos:

2 bytes: número de arquivos (big endian order)
repete-se até terminar os nomes:
1 byte: tamanho do nome (1-255)
variável: nome do arquivo

----

para o GETFILE adiciona-se os campos:
4 bytes: tamanho do arquivo (big endian order)
variável: bytes do arquivo.

----

para o ADDFILE adiciona-se os campos:
4 bytes: tamanho do arquivo (big endian order)
variável: bytes do arquivo.

* ao fazer download do arquivo, grave em uma pasta padrão.
