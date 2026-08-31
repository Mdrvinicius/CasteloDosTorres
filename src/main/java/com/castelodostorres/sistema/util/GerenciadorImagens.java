package com.castelodostorres.sistema.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class GerenciadorImagens {

    // pasta onde ficam as imagens dos produtos (junto do banco, no home do usuário)
    private static final Path PASTA_IMAGENS =
            Paths.get(System.getProperty("user.home"), "CasteloDosTorres", "imagens_produtos");

    public static String copiarImagem(File origem, int produtoId) throws IOException { // MÉTODO: copia a imagem escolhida pra pasta do sistema, retorna o nome do arquivo
        garantirPasta();

        // pega a extensão do arquivo original (.jpg, .png, etc.)
        String nomeOriginal = origem.getName();
        String extensao = "";
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto >= 0) {
            extensao = nomeOriginal.substring(ponto); // inclui o ponto
        }

        // nome único baseado no id do produto (ex: "produto_7.jpg")
        String nomeArquivo = "produto_" + produtoId + extensao;
        Path destino = PASTA_IMAGENS.resolve(nomeArquivo);

        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        return nomeArquivo; // é isso que vai pro banco
    }

    public static File caminhoImagem(String nomeArquivo) { // MÉTODO: monta o caminho completo pra exibir (null se não houver)
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            return null;
        }
        return PASTA_IMAGENS.resolve(nomeArquivo).toFile();
    }

    public static boolean imagemExiste(String nomeArquivo) { // MÉTODO: confere se o arquivo realmente está lá
        File f = caminhoImagem(nomeArquivo);
        return f != null && f.exists();
    }

    private static void garantirPasta() throws IOException { // MÉTODO: cria a pasta se ainda não existir
        if (!Files.exists(PASTA_IMAGENS)) {
            Files.createDirectories(PASTA_IMAGENS);
        }
    }
}