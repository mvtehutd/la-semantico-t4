package br.ufscar.dc.compiladores.la.semantico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.EntradaTabelaDeSimbolos;
import br.ufscar.dc.compiladores.la.semantico.TabelaDeSimbolos.TipoLa;
import br.ufscar.dc.compiladores.parser.LaBaseVisitor;
import br.ufscar.dc.compiladores.parser.LaParser;
import br.ufscar.dc.compiladores.parser.LaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.parser.LaParser.CmdContext;
import br.ufscar.dc.compiladores.parser.LaParser.CorpoContext;
import br.ufscar.dc.compiladores.parser.LaParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.parser.LaParser.Declaracao_localContext;
import br.ufscar.dc.compiladores.parser.LaParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.parser.LaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.parser.LaParser.ParametroContext;
import br.ufscar.dc.compiladores.parser.LaParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.parser.LaParser.TipoContext;
import br.ufscar.dc.compiladores.parser.LaParser.VariavelContext;

public class LaSemantico extends LaBaseVisitor<Void> {

    TabelaDeSimbolos tabela;

    // Cria o visitante do Programa que cria a tabela de símbolos
    @Override
    public Void visitPrograma(LaParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitCorpo(CorpoContext ctx) {
        // Verifica dos comandos e se for "retorne" adiciona o erro de que nao pode ser
        // usado dentro do corpo do programa
        for (CmdContext cmdContext : ctx.cmd()) {
            if (cmdContext.cmdRetorne() != null) {
                LaSemanticoUtils.adicionarErroSemantico(cmdContext.cmdRetorne().retorne,
                        "comando retorne nao permitido nesse escopo");
            }
        }
        return super.visitCorpo(ctx);
    }

    @Override
    public Void visitParcela_unario(Parcela_unarioContext ctx) {
        // Verifica se é uma função sendo chamada
        if (ctx.IDENT() != null) {
            // Verifica se os parametros declarados na funcao são o mesmo tanto que os
            // passados na chamada
            if (ctx.expressao().size() != tabela.retornaNumeroDeElementosDaSubTabela(ctx.IDENT().getText())) {
                LaSemanticoUtils.adicionarErroSemantico(ctx.IDENT().getSymbol(),
                        "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
            } else {
                // se a quantidade esta correta, verifica o tipo de cada parametro com os
                // parametros salvos na tabela de simbolos para a funcao
                TipoLa tipoExpressao = null;
                TipoLa tipoParametro = null;
                Collection<EntradaTabelaDeSimbolos> parametrosDaFuncao = tabela
                        .retornarParametrosDaFuncao(ctx.IDENT().getText());
                Integer i = 0;
                for (EntradaTabelaDeSimbolos parametro : parametrosDaFuncao) {
                    tipoExpressao = LaSemanticoUtils.verificarTipo(tabela, ctx.expressao(i++));
                    tipoParametro = parametro.tipo;

                    if (!tipoExpressao.equals(tipoParametro)) {
                        LaSemanticoUtils.adicionarErroSemantico(ctx.IDENT().getSymbol(),
                                "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
                    }
                }
            }
        }
        return super.visitParcela_unario(ctx);
    }

    @Override
    public Void visitDeclaracao_global(Declaracao_globalContext ctx) {

        // Adiciona na tabela de simbolos uma variavel com nome da funcao/procedimento
        // com uma subt tabela contendo os parametros
        TipoLa tipovar = TipoLa.INVALIDO;
        Boolean tipoPonteiro = false;
        TabelaDeSimbolos tabelaDoRegistro = null;
        TabelaDeSimbolos tabelaAdicional = new TabelaDeSimbolos();
        for (ParametroContext parametroContext : ctx.parametros().parametro()) {
            String tipo = parametroContext.tipo_estendido().getText();
            if (tipo.startsWith("^", 0)) {
                tipoPonteiro = true;
                tipo = tipo.substring(1, tipo.length());
            }
            // verifica se é algum dos tipos padrão
            tipovar = LaSemanticoUtils.retornaTipoLaDoIdentificador(tabela,
                    parametroContext.identificador(0).IDENT(0).getSymbol(), tipo);
            if (tipovar.equals(TipoLa.REGISTRO)) {
                tabelaDoRegistro = tabela.recuperaRegistro(tipo);
            }
            // Se ele já existe na tabela de símbolos, então erro de já declarado, senão
            // adiciona na tabela de símbolos
            for (IdentificadorContext variavelIdent : parametroContext.identificador()) {
                String variavel = variavelIdent.getText();
                insereVariavelNaTabelaSeNaoExistir(tabelaAdicional, variavel, tipovar, tipoPonteiro,
                        tabelaDoRegistro,
                        variavelIdent.IDENT(0).getSymbol());
            }
        }

        // Se for funcao, verifica o tipo de retorno da funcao
        if (ctx.getStart().getText().equals("funcao")) {
            tipovar = LaSemanticoUtils.retornaTipoLaDoIdentificador(tabela, ctx.IDENT().getSymbol(),
                    ctx.tipo_estendido().getText());
        } else {
            // se for procedimento verifica se existe um "retorne" e adiciona um erro
            // semantico caso exista
            tipovar = null;
            for (CmdContext cmdContext : ctx.cmd()) {
                if (cmdContext.cmdRetorne() != null) {
                    LaSemanticoUtils.adicionarErroSemantico(cmdContext.cmdRetorne().retorne,
                            "comando retorne nao permitido nesse escopo");
                }
            }

        }

        // Adiciona a funcao/procedimento na tabela
        String variavel = ctx.IDENT().getText();
        insereVariavelNaTabelaSeNaoExistir(tabela, variavel, tipovar, tipoPonteiro, tabelaAdicional,
                ctx.IDENT().getSymbol());

        return super.visitDeclaracao_global(ctx);
    }

    // Visitante da Declaração Local que confere erros de declaração
    @Override
    public Void visitDeclaracao_local(Declaracao_localContext ctx) {
        List<VariavelContext> listaVariaveis = new ArrayList<VariavelContext>();
        TipoContext tipoContextDaVariavel = null;
        Boolean ehConstante = false;
        TipoLa tipovar = TipoLa.INVALIDO;
        // Verifica o tipo de declaracao local feita para inicializar as variaveis com
        // os parametros corretos
        switch (ctx.getStart().getText()) {
            case "declare":
                listaVariaveis = Arrays.asList(ctx.variavel());
                tipoContextDaVariavel = ctx.variavel().tipo();
                break;
            case "constante":
                tipovar = LaSemanticoUtils.retornaTipoLaDoIdentificador(tabela,
                        ctx.IDENT().getSymbol(), ctx.tipo_basico().getText());
                ehConstante = true;
                break;
            case "tipo":
                tipoContextDaVariavel = ctx.tipo();
                break;

            default:
                break;
        }

        Boolean tipoPonteiro = false;
        Boolean registro = false;
        TabelaDeSimbolos tabelaDoRegistro = null;
        TabelaDeSimbolos tabelaAdicional = tabela;
        // Verifica se a variavel é um registro para criar uma subtabela para as
        // declaracoes do registro
        if (!ehConstante && tipoContextDaVariavel.registro() != null) {
            listaVariaveis = tipoContextDaVariavel.registro().variavel();
            registro = true;
            tabelaAdicional = new TabelaDeSimbolos();
        }
        // Percorre todas as variaveis para armazenar seus valores
        for (VariavelContext variavelContext : listaVariaveis) {

            String tipo = variavelContext.tipo().getText();
            if (tipo.startsWith("^", 0)) {
                tipoPonteiro = true;
                tipo = tipo.substring(1, tipo.length());
            }
            tipovar = LaSemanticoUtils.retornaTipoLaDoIdentificador(tabela,
                    variavelContext.identificador(0).IDENT(0).getSymbol(), tipo);
            if (tipovar == TipoLa.REGISTRO) {
                tabelaDoRegistro = tabela.recuperaRegistro(tipo);
            }

            // Se ele já existe na tabela de símbolos, então erro de já declarado, senão
            // adiciona na tabela de símbolos
            for (IdentificadorContext variavelIdent : variavelContext.identificador()) {
                String variavel = variavelIdent.getText();
                for (Exp_aritmeticaContext tamanhoDoArray : variavelIdent.dimensao().exp_aritmetica()) {
                    variavel = variavelIdent.IDENT(0).getText();
                }
                insereVariavelNaTabelaSeNaoExistir(tabelaAdicional, variavel, tipovar, tipoPonteiro,
                        tabelaDoRegistro, variavelIdent.IDENT(0).getSymbol());
            }
        }

        String variavel = null;

        // Armazena de forma diferente dependendo se a variavel é um registro declarado
        // como variavel ou como tipo.
        if (registro) {
            switch (ctx.getStart().getText()) {
                case "declare":
                    for (IdentificadorContext variavelIdent : ctx.variavel().identificador()) {
                        variavel = variavelIdent.getText();
                        insereVariavelNaTabelaSeNaoExistir(tabela, variavel, TipoLa.REGISTRO,
                                tipoPonteiro,
                                tabelaAdicional, variavelIdent.IDENT(0).getSymbol());
                    }
                    break;
                case "tipo":
                    variavel = ctx.IDENT().getText();
                    insereVariavelNaTabelaSeNaoExistir(tabela, variavel, TipoLa.REGISTRO, tipoPonteiro,
                            tabelaAdicional, ctx.IDENT().getSymbol());
                    break;

                default:
                    break;
            }
        }

        // Verifica se é constante para armazenar o valor
        if (ehConstante) {
            variavel = ctx.IDENT().getText();
            insereVariavelNaTabelaSeNaoExistir(tabela, variavel, tipovar, tipoPonteiro, null,
                    ctx.IDENT().getSymbol());
        }

        return super.visitDeclaracao_local(ctx);
    }

    // Visitante do Identificador confere se não está na tabela de símbolos, gerando
    // erro de não declarado
    @Override
    public Void visitIdentificador(IdentificadorContext ctx) {
        for (TerminalNode ident : ctx.IDENT()) {
            if (!tabela.existe(ident.getText())) {
                LaSemanticoUtils.adicionarErroSemantico(ctx.IDENT(0).getSymbol(),
                        "identificador " + ctx.getText() + " nao declarado");

            }
        }
        return super.visitIdentificador(ctx);
    }

    // Visitante do comando de atribuição verifica se a atribuição é compatível com
    // os tipos declarados
    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        TipoLa tipoExpressao = LaSemanticoUtils.verificarTipo(tabela, ctx.expressao());
        String nomeVariavel = ctx.identificador().getText();
        String nomeVariavelModificada = ctx.identificador().getText();

        // Formata o nome da variavel se ela possuir uma dimensao"[]"
        if (!ctx.identificador().dimensao().isEmpty()) {
            nomeVariavelModificada = ctx.identificador().IDENT(0).getText();
        }
        // Confere se a variavel nao existe, se nao existe retorna antes de conferir os
        // tipos para gerar um erro semantico.
        if (!tabela.existe(nomeVariavelModificada)) {
            return super.visitCmdAtribuicao(ctx);
        }

        // Verifica se os tipos da expressao e valido
        // Se for invalido gera um erro semantico
        if (tipoExpressao != TipoLa.INVALIDO) {
            TipoLa tipoVariavel = null;

            // verifica se é um registro para alterar a forma de verificar o tipo da variavel
            if (ctx.identificador().ponto != null) {
                tipoVariavel = tabela.verificarTipoRegistro(ctx.identificador().IDENT(0).getText(),
                        ctx.identificador().IDENT(1).getText());
            } else {
                tipoVariavel = LaSemanticoUtils.verificarTipo(tabela, nomeVariavelModificada);
            }

            // Verifica se os tipos da expressao e da variavel sao diferentes
            // se forem diferentes gera um erro semantico
            if (LaSemanticoUtils.tiposDiferentes(tipoVariavel, tipoExpressao)) {

                // verifica se é um registro para alterar a forma de verificar se a variavel é um ponteiro
                // Se for ponteiro formata a exibicao do nome da variavel
                if (ctx.identificador().ponto != null) {
                    if (tabela.verificarPonteiroRegistro(ctx.identificador().IDENT(0).getText(),
                            ctx.identificador().IDENT(1).getText())) {
                        nomeVariavel = "^" + nomeVariavelModificada;
                    }
                } else if (tabela.verificarPonteiro(nomeVariavelModificada)) {
                    nomeVariavel = "^" + nomeVariavelModificada;
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

    /**
     * Insere a variavel na tabela de simbolos se ela nao existir, caso ela exista gera um erro semantico.
     * 
     * @param tabelaParaInserir tabela que vai ser inserida a variavel
     * @param variavelName nome da variavel que vai ser armazenada
     * @param tipoVariavel TipoLa da variavel
     * @param ehPonteiro sinalizacao se a variavel é ou nao um ponteiro
     * @param tabelaAdicional subtabela com as variaveis pertencentes a variavel pai
     * @param token token que vai ser usado para identificar a linha quando gerar um erro semantico
     */
    private void insereVariavelNaTabelaSeNaoExistir(TabelaDeSimbolos tabelaParaInserir, String variavelName,
            TipoLa tipoVariavel, Boolean ehPonteiro, TabelaDeSimbolos tabelaAdicional, Token token) {

        if (tabelaParaInserir.existe(variavelName) && tabela.existeNaTabelaPrincipal(variavelName)) {
            LaSemanticoUtils.adicionarErroSemantico(token,
                    "identificador " + variavelName + " ja declarado anteriormente");
        } else {

            tabelaParaInserir.adicionar(variavelName, tipoVariavel, ehPonteiro, tabelaAdicional);
        }
    }

}