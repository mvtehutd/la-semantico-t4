package br.ufscar.dc.compiladores.la.semantico;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import br.ufscar.dc.compiladores.parser.LaLexer;
import br.ufscar.dc.compiladores.parser.LaParser;
import br.ufscar.dc.compiladores.parser.LaParser.ProgramaContext;

public class Principal {
    public static void main(String args[]) throws IOException {
        // args[0] é o primeiro argumento da linha de comando que representa o CAMINHO PARA O ARQUIVO DE ENTRADA COM A LINGUAGEM
        CharStream cs = CharStreams.fromFileName(args[0]);
        LaLexer lexer = new LaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LaParser parser = new LaParser(tokens);
        ProgramaContext arvore = parser.programa();
        LaSemantico as = new LaSemantico();
        as.visitPrograma(arvore);
        // Pega o segundo argumento da linha de comando que representa o CAMINHO PARA O ARQUIVO DE SAIDA DA ANALISE SEMÂNTICA
        // E Cria um objeto para escrever no arquivo
        try (PrintWriter pw = new PrintWriter(new File(args[1]))) {
            // Verifica se há erros
            System.out.println("Iniciando Lista de Erros");
            for (String string : LaSemanticoUtils.errosSemanticos) {
                pw.println(string);
                System.out.println(string);
            }
            if(!LaSemanticoUtils.errosSemanticos.isEmpty()){
                pw.println("Fim da compilacao");
                System.out.println("Fim da compilacao");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}