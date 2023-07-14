package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.icu.util.GenderInfo.ListGenderStyle;

import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoLa;
import br.ufscar.dc.compiladores.parser.LaBaseVisitor;
import br.ufscar.dc.compiladores.parser.LaParser;
import br.ufscar.dc.compiladores.parser.LaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.parser.LaParser.Declaracao_localContext;
import br.ufscar.dc.compiladores.parser.LaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.parser.LaParser.RegistroContext;
import br.ufscar.dc.compiladores.parser.LaParser.VariavelContext;

public class LaSemantico extends LaBaseVisitor<Void> {

    TabelaDeSimbolos tabela;

    // Cria o visitante do Programa que cria a tabela de símbolos
    @Override
    public Void visitPrograma(LaParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    // Visitante da Declaração Local que confere erros de declaração
    @Override
    public Void visitDeclaracao_local(Declaracao_localContext ctx) {
        List<VariavelContext> listaVariaveis = Arrays.asList(ctx.variavel());
        TipoLa tipovar = TipoLa.INVALIDO;
        Boolean tipoPonteiro = false;
        Boolean registro = false;
        TabelaDeSimbolos tabelaAdicional = tabela;
        if (ctx.variavel().tipo().registro() != null) {
            listaVariaveis = ctx.variavel().tipo().registro().variavel();
            registro = true;
            tabelaAdicional = new TabelaDeSimbolos();
        }
        for (VariavelContext variavelContext : listaVariaveis) {
            String tipo = variavelContext.tipo().getText();
            System.out.println("tipo antes: " + tipo);
            if (tipo.startsWith("^", 0)) {
                tipoPonteiro = true;
                tipo = tipo.substring(1, tipo.length());
            }
            System.out.println("tipo depois: " + tipo);
            // verifica se é algum dos tipos padrão
            switch (tipo) {
                case
                        "inteiro":
                    tipovar = TipoLa.INTEIRO;
                    break;
                case
                        "literal":
                    tipovar = TipoLa.LITERAL;
                    break;
                case
                        "real":
                    tipovar = TipoLa.REAL;
                    break;
                case
                        "logico":
                    tipovar = TipoLa.LOGICO;
                    break;

                default:
                    LaSemanticoUtils.adicionarErroSemantico(ctx.variavel().identificador(0).IDENT(0).getSymbol(),
                            "tipo " + tipo + " nao declarado");
                    break;
            }
            // Se ele já existe na tabela de símbolos, então erro de já declarado, senão
            // adiciona na tabela de símbolos
            for (IdentificadorContext variavelIdent : variavelContext.identificador()) {
                String variavel = variavelIdent.getText();
                System.out.println("variavel: " + variavel);
                if (tabelaAdicional.existe(variavel)) {
                    LaSemanticoUtils.adicionarErroSemantico(variavelIdent.IDENT(0).getSymbol(),
                            "identificador " + variavel + " ja declarado anteriormente");
                } else {
                    System.out.println("Variavel: " + variavel + " tipo: " + tipovar + " Ponteiro: " + tipoPonteiro
                            + " Tabela Adicional: null");
                    tabelaAdicional.adicionar(variavel, tipovar, tipoPonteiro, null);
                }
            }
        }

        if (registro) {
            for (IdentificadorContext variavelIdent : ctx.variavel().identificador()) {
                String variavel = variavelIdent.getText();
                System.out.println("variavel: " + variavel);
                if (tabela.existe(variavel)) {
                    LaSemanticoUtils.adicionarErroSemantico(variavelIdent.IDENT(0).getSymbol(),
                            "identificador " + variavel + " ja declarado anteriormente");
                } else {
                    System.out.println("Variavel: " + variavel + " tipo: " + TipoLa.REGISTRO + " Ponteiro: "
                            + tipoPonteiro + " Tabela Adicional: " + tabelaAdicional);
                    tabela.adicionar(variavel, TipoLa.REGISTRO, tipoPonteiro, tabelaAdicional);
                }
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    // Visitante do Identificador confere se não está na tabela de símbolos, gerando
    // erro de não declarado
    @Override
    public Void visitIdentificador(IdentificadorContext ctx) {
        if(!tabela.existe(ctx.IDENT(0).getText())) {
            System.out.println("Adicionei erro");
            LaSemanticoUtils.adicionarErroSemantico(ctx.IDENT(0).getSymbol(),
                    "identificador " + ctx.IDENT(0).getText() + " nao declarado");
        }
        return super.visitIdentificador(ctx);
    }

    // Visitante do comando de atribuição verifica se a atribuição é compatível com
    // os tipos declarados
    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        TipoLa tipoExpressao = LaSemanticoUtils.verificarTipo(tabela, ctx.expressao());
        String nomeVariavel = ctx.identificador().getText();

        if (tipoExpressao != TipoLa.INVALIDO) {
            TipoLa tipoVariavel = null;
            if (ctx.identificador().ponto != null) {
                System.out
                        .println(ctx.identificador().IDENT(0).getText() + "." + ctx.identificador().IDENT(1).getText());
                tipoVariavel = tabela.verificarTipoRegistro(ctx.identificador().IDENT(0).getText(),
                        ctx.identificador().IDENT(1).getText());
            } else {
                tipoVariavel = LaSemanticoUtils.verificarTipo(tabela, nomeVariavel);
            }
            if (LaSemanticoUtils.tiposDiferentes(tipoVariavel, tipoExpressao)) {
                if (ctx.identificador().ponto != null) {
                    if (tabela.verificarPonteiroRegistro(ctx.identificador().IDENT(0).getText(),
                            ctx.identificador().IDENT(1).getText())) {
                        nomeVariavel = "^" + nomeVariavel;
                    }
                } else if (tabela.verificarPonteiro(nomeVariavel)) {
                    nomeVariavel = "^" + nomeVariavel;
                }
                LaSemanticoUtils.adicionarErroSemantico(ctx.identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + nomeVariavel);
            }
        } else {
            LaSemanticoUtils.adicionarErroSemantico(ctx.identificador().IDENT(0).getSymbol(),
                    "atribuicao nao compativel para " + nomeVariavel);
        }
        return super.visitCmdAtribuicao(ctx);
    }

}