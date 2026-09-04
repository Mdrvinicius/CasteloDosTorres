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

        for (Visita visita : visitas) {
            // --- comissão do guia ---
            double comissaoGuia = calcularValor(
                    visita.getGuiaTipoRemuneracao(),
                    visita.getGuiaValorRemuneracao(),
                    visita
            );
            acumular(acumulado, visita.getGuiaId(), visita.getNomeGuia(), "GUIA", comissaoGuia);

            // --- comissão da recepcionista (se houver) ---
            if (visita.getNomeRecepcionista() != null && visita.getRecepcionistaTipoRemuneracao() != null
                    && visita.getRecepcionistaId() != null) {
                double comissaoRecep = calcularValor(
                        visita.getRecepcionistaTipoRemuneracao(),
                        visita.getRecepcionistaValorRemuneracao(),
                        visita
                );
                acumular(acumulado, visita.getRecepcionistaId(), visita.getNomeRecepcionista(), "RECEPCIONISTA", comissaoRecep);
            }
        }

        return new ArrayList<>(acumulado.values());
    }

    private double calcularValor(String tipo, double valorRemuneracao, Visita visita) { // MÉTODO: aplica a fórmula certa
        double resultado;
        if ("PERCENTUAL".equals(tipo)) {
            double liquido = visita.getValorTotal() - visita.getValorReembolsado();
            resultado = liquido * (valorRemuneracao / 100.0);
        } else { // FIXO_POR_PESSOA
            int pagantes = visita.getQuantidadeInteira() + visita.getQuantidadeMeia();
            resultado = pagantes * valorRemuneracao;
        }
        return Math.round(resultado * 100.0) / 100.0;
    }

    private void acumular(Map<String, ComissaoFuncionario> mapa, int funcionarioId, String nome, String papel, double valor) { // MÉTODO: soma no acumulado
        String chave = papel + ":" + funcionarioId; // chave única por funcionário+papel (por id, robusto)
        if (mapa.containsKey(chave)) {
            mapa.get(chave).adicionar(valor);
        } else {
            mapa.put(chave, new ComissaoFuncionario(funcionarioId, nome, papel, valor));
        }
    }
}