package br.ufmg.dcc.imagerank.main;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.lac.converter.LACQueryFileConverter;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImageRankOnlineQueryProcessor
{
	public static void main(String[] args)
	{
		try
		{
			/*
			 * Passos Batch:
			 * 1. Extrair descritores da imagem de consulta.
			 * 2. Gerar arquivos de pares aleatórios e normalizados.
			 * 3. Converter arquivo de pares para formato weka.
			 * 4. Discretizar arquivos de pares.
			 * 5. Gerar arquivo de treino no formato do LAC.
			 */

			/*
			 * Passos Online:
			 * 1. Extrair descritores da imagem de consulta.
			 * 2. Gerar arquivo de pares de consulta, normalizado, com imagem de consulta + 500 imagens aleatórias para comparação.
			 * 3. Converter arquivo de pares de consulta para formato weka.
			 * 4. Discretizar arquivos de pares de consulta.
			 * 5. Gerar arquivo de teste no formato do LAC.
			 * 6. Rodar o algoritmo do LAC.
			 * 7. Gerar saída com top-K imagens similares (nome da imagem, score).
			 */

			String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
			String diretorioSaida = "lac_dataset";
			String arquivoWeka = diretorioBase + "/pares_discretizados.arff";
			String arquivoOriginal = diretorioBase + "/pares/" + ImageRankConstants.PAIR_OUTPUT_FILENAME;

			LACQueryFileConverter converter = new LACQueryFileConverter(diretorioBase, diretorioSaida, arquivoWeka, arquivoOriginal);
			converter.convert(false);
		}
		catch (ProcessorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
