package com.castelodostorres.sistema.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GeradorPdf {

    private PDDocument documento;
    private PDPage pagina;
    private PDPageContentStream fluxo;
    private float y; // posição vertical atual (do topo pra baixo)

    private static final float MARGEM = 50;
    private static final float LARGURA = PDRectangle.A4.getWidth();
    private static final float ALTURA = PDRectangle.A4.getHeight();

    public GeradorPdf() throws IOException { // CONSTRUTOR: inicia documento e primeira página
        documento = new PDDocument();
        novaPagina();
    }

    private void novaPagina() throws IOException { // MÉTODO: cria uma página nova e posiciona o cursor no topo
        if (fluxo != null) fluxo.close();
        pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);
        fluxo = new PDPageContentStream(documento, pagina);
        y = ALTURA - MARGEM;
    }

    private void garantirEspaco(float necessario) throws IOException { // MÉTODO: quebra pra próxima página se não couber
        if (y - necessario < MARGEM) {
            novaPagina();
        }
    }

    private void escreverTexto(String texto, float x, float posY, PDType1Font fonte, float tamanho) throws IOException { // MÉTODO: escreve um texto em posição ABSOLUTA
        fluxo.beginText();
        fluxo.setFont(fonte, tamanho);
        fluxo.newLineAtOffset(x, posY);
        fluxo.showText(texto == null ? "" : texto);
        fluxo.endText();
    }

    public void cabecalho(String tituloRelatorio, String subtitulo) throws IOException { // MÉTODO: cabeçalho padrão do sistema
        escreverTexto("Castelo dos Torres", MARGEM, y, PDType1Font.HELVETICA_BOLD, 20);
        y -= 26;

        escreverTexto(tituloRelatorio, MARGEM, y, PDType1Font.HELVETICA_BOLD, 14);
        y -= 18;

        if (subtitulo != null && !subtitulo.isBlank()) {
            escreverTexto(subtitulo, MARGEM, y, PDType1Font.HELVETICA, 11);
            y -= 16;
        }

        y -= 6;
        fluxo.moveTo(MARGEM, y);
        fluxo.lineTo(LARGURA - MARGEM, y);
        fluxo.stroke();
        y -= 16;
    }

    public void secao(String titulo) throws IOException { // MÉTODO: título de seção
        garantirEspaco(30);
        y -= 6;
        escreverTexto(titulo, MARGEM, y, PDType1Font.HELVETICA_BOLD, 12);
        y -= 18;
    }

    public void linha(String rotulo, String valor) throws IOException { // MÉTODO: uma linha "rótulo: valor"
        garantirEspaco(16);
        escreverTexto(rotulo + ": " + valor, MARGEM, y, PDType1Font.HELVETICA, 11);
        y -= 16;
    }

    public void espaco(float px) { // MÉTODO: pula um espaço vertical
        y -= px;
    }

    public void tabela(String[] cabecalhos, List<String[]> linhas, float[] largurasCol) throws IOException { // MÉTODO: desenha uma tabela por posição absoluta
        float alturaLinha = 16;

        // calcula a posição X de cada coluna (acumulando as larguras)
        float[] xColunas = new float[cabecalhos.length];
        float xAtual = MARGEM;
        for (int i = 0; i < cabecalhos.length; i++) {
            xColunas[i] = xAtual;
            xAtual += largurasCol[i];
        }

        // cabeçalho da tabela
        garantirEspaco(alturaLinha);
        for (int i = 0; i < cabecalhos.length; i++) {
            escreverTexto(cortar(cabecalhos[i], largurasCol[i]), xColunas[i], y, PDType1Font.HELVETICA_BOLD, 10);
        }
        y -= alturaLinha;

        // linhas de dados
        for (String[] linha : linhas) {
            garantirEspaco(alturaLinha);
            for (int i = 0; i < linha.length && i < xColunas.length; i++) {
                escreverTexto(cortar(linha[i], largurasCol[i]), xColunas[i], y, PDType1Font.HELVETICA, 10);
            }
            y -= alturaLinha;
        }
    }

    private String cortar(String texto, float larguraCol) { // MÉTODO: corta o texto pra não invadir a coluna vizinha (aproximado)
        if (texto == null) return "";
        int maxChars = (int) (larguraCol / 5.5f); // ~5.5px por caractere no tamanho 10
        if (texto.length() <= maxChars) return texto;
        if (maxChars <= 1) return texto;
        return texto.substring(0, Math.max(1, maxChars - 1)) + "…";
    }

    public void salvarComo(File destino) throws IOException { // MÉTODO: fecha e salva o PDF
        if (fluxo != null) fluxo.close();
        documento.save(destino);
        documento.close();
    }
}