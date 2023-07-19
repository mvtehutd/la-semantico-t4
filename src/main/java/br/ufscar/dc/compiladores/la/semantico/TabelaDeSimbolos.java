package br.ufscar.dc.compiladores.la.semantico;

import java.util.HashMap;
import java.util.Map;

public class TabelaDeSimbolos {
    public enum TipoLa {
        LITERAL,
        INTEIRO,
        REAL,
        LOGICO,
        INVALIDO,
        REGISTRO
    }

    class EntradaTabelaDeSimbolos {
        String nome;
        TipoLa tipo;
        Boolean ponteiro;
        TabelaDeSimbolos registro;

        private EntradaTabelaDeSimbolos(String nome, TipoLa tipo, Boolean ponteiro, TabelaDeSimbolos registro) {
            this.nome = nome;
            this.tipo = tipo;
            this.ponteiro = ponteiro;
            this.registro = registro;
        }
    }

    private final Map<String, EntradaTabelaDeSimbolos> tabela;

    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }

    public void adicionar(String nome, TipoLa tipo, Boolean ponteiro, TabelaDeSimbolos registro) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo, ponteiro, registro));
    }

    public boolean existe(String nome) {
        System.out.println("Verificando se existe: "+ nome);
        if (!tabela.containsKey(nome)) {
            for (Map.Entry<String, EntradaTabelaDeSimbolos> entry : tabela.entrySet()) {
                String key = entry.getKey();
                TabelaDeSimbolos tabelaAdicional = tabela.get(key).registro;
                if (tabelaAdicional != null && tabelaAdicional.existe(nome)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean existeRegistro(String nome, String nomeVariavel) {
        return tabela.get(nome).registro.existe(nomeVariavel);
    }

    public TipoLa verificar(String nome) {
        System.out.println("verificar: " + nome);
        return tabela.get(nome).tipo;
    }

    public boolean verificarPonteiro(String nome) {
        System.out.println("nome: " + nome);
        return tabela.get(nome).ponteiro;
    }

    public boolean verificarPonteiroRegistro(String nome, String nomeVariavel) {
        System.out.println("nome: " + nome);
        return tabela.get(nome).registro.verificarPonteiro(nomeVariavel);
    }

    public TabelaDeSimbolos recuperaRegistro(String nome){
        return tabela.get(nome).registro;
    }

    public TipoLa verificarTipoRegistro(String nome, String nomeVariavel) {
        return tabela.get(nome).registro.verificar(nomeVariavel);
    }

}