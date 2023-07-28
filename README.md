# T4 - Analisador Semântico
Marcos Cardoso Vendrame - 790725 <br/>
Carlos Eduardo Nascimento dos Santos - 791029


## 1.	Programas Necessários
Para compilar e executar o programa, é necessário ter instalado na máquina o Java, o Maven e o Antlr4. 
Nas compilações e execuções realizadas nesse trabalho, as versões utilizadas para esses softwares foram:

- Java (JDK 17.0.1 e JDK 17.0.6)
- Apache Maven 3.9.2
- ANTLR 4.11.0

É possível que outras versões também sejam compatíveis, então verifique a compatibilidade caso já os tenha instalado na máquina.

## 2.	Compilação e Execução do Programa
Para compilar o código, acesse o diretório em que foi salvo o projeto em sua máquina e execute o comando ```mvn package``` no terminal para que o Maven crie os arquivos e o programa com extensão *.jar* necessários.
  
   ![image](https://github.com/mvtehutd/la-semantico-t4/assets/100847921/8c5abb98-0b79-4f59-aab8-8306139882ed)

<p align="center"><i><b>Imagem 1:</b> Realizando a Compilação</i></p>

![image](https://github.com/mvtehutd/la-semantico-t4/assets/100847921/4b7b1c16-607a-4295-b94f-cb01421807d9)

<p align="center"><i><b>Imagem 2:</b> Mensagem Indicando Sucesso na Compilação</i></p>

Com a compilação finalizada, é possível executar o programa. Para isso, o comando a ser realizado é:

```java -jar <caminho do compilador> <arquivo de entrada> <arquivo de saída>```

  Sendo que: </br>
-	```<caminho do compilador>``` é o caminho completo até o arquivo de extensão *.jar* criado, lembrando de escolher o com as dependências. Ele está localizado na pasta target:
 
 ![image](https://github.com/mvtehutd/la-semantico-t3/assets/100847921/c9a17efa-81d1-4eb0-af6f-17085d9be03c)

<p align="center"><i><b>Imagem 3:</b> Localizando o Compilador no Projeto</i></p>

-	```<arquivo de entrada>``` é o caminho completo até o arquivo de extensão *.txt* com o algoritmo a ser analisado.

 ![image](https://github.com/mvtehutd/la-semantico-t4/assets/100847921/25b84521-7f0e-4a63-9f4c-d751b943142b)

<p align="center"><i><b>Imagem 4:</b> Exemplo de Arquivo de Entrada</i></p>

-	```<arquivo de saída>``` é o caminho completo até o arquivo de extensão *.txt* na qual serão salvos os resultados da análise.

 ![image](https://github.com/mvtehutd/la-semantico-t4/assets/100847921/2381aa83-9689-4f21-836b-580626b90286)

<p align="center"><i><b>Imagem 5:</b> Exemplo de Arquivo de Saída</i></p>

Exemplo de como o analisador deve rodar:
```
java -jar c:\compilador\meu-compilador.jar c:\casos-de-teste\arquivo1.txt c:\temp\saida.txt
```
