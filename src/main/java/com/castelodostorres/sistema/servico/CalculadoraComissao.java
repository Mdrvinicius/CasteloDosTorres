package com.castelodostorres.sistema.servico;

import com.castelodostorres.sistema.modelo.Visita;
import com.castelodostorres.sistema.modelo.dto.ComissaoFuncionario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalculadoraComissao {

    public List<ComissaoFuncionario> calcular(List<Visita> visitas) { // MÉTODO: recebe as visitas e devolve a comissão por funcionário
        Map<String, ComissaoFuncionario> acumulado = new LinkedHashMap<>();

        for (Visita visita : visitas) { // percorre cada visita do período
            // --- comissão do guia ---
            double comissaoGuia = calcularValor(
                    visita.getGuiaTipoRemuneracao(),
                    visita.getGuiaValorRemuneracao(),
                    visita
            );
            acumular(acumulado, visita.getNomeGuia(), "GUIA", comissaoGuia);

            // --- comissão da recepcionista (se houver) ---
            if (visita.getNomeRecepcionista() != null && visita.getRecepcionistaTipoRemuneracao() != null) {
                double comissaoRecep = calcularValor(
                        visita.getRecepcionistaTipoRemuneracao(),
                        visita.getRecepcionistaValorRemuneracao(),
                        visita
                );
                acumular(acumulado, visita.getNomeRecepcionista(), "RECEPCIONISTA", comissaoRecep);
            }
        }

        return new ArrayList<>(acumulado.values()); // converte o mapa numa lista pra exibir
    }

    private double calcularValor(String tipo, double valorRemuneracao, Visita visita) { // MÉTODO: aplica a fórmula certa
        if ("PERCENTUAL".equals(tipo)) {
            double liquido = visita.getValorTotal() - visita.getValorReembolsado();
            return liquido * (valorRemuneracao / 100.0);
        } else { // FIXO_POR_PESSOA
            int pagantes = visita.getQuantidadeInteira() + visita.getQuantidadeMeia();
            return pagantes * valorRemuneracao;
        }
    }

    private void acumular(Map<String, ComissaoFuncionario> mapa, String nome, String papel, double valor) { // MÉTODO: soma no acumulado
        String chave = papel + ":" + nome; // chave única por funcionário+papel
        if (mapa.containsKey(chave)) {
            mapa.get(chave).adicionar(valor); // já existe: soma
        } else {
            mapa.put(chave, new ComissaoFuncionario(nome, papel, valor)); // primeiro: cria
        }
    }
}