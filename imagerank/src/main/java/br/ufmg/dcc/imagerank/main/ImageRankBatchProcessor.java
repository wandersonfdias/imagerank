package br.ufmg.dcc.imagerank.main;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.lac.converter.LACQueryFileConverter;
import br.ufmg.dcc.imagerank.pairs.generator.RandomPairGenerator;
import br.ufmg.dcc.imagerank.weka.util.DiscretizeDataSet;
import br.ufmg.dcc.imagerank.weka.util.WekaConversor;

/**
 * Realiza geração da base de treino para processamento de imagens.
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImageRankBatchProcessor
{
	private static final Log LOG = LogFactory.getLog(ImageRankBatchProcessor.class);

	/**
	 * Realiza geração da base de treino para processamento de imagens.<br><br>
	 * Passos do processamento:<br>
	 * 1. Extrair descritores da imagem de consulta.<br>
	 * 2. Gerar arquivos de pares aleatórios e normalizados.<br>
	 * 3. Converter arquivo de pares para formato weka.<br>
	 * 4. Discretizar arquivos de pares.<br>
	 * 5. Gerar arquivo de treino no formato do LAC.<br>
	 * @param args
	 * @throws ProcessorException
	 */
	public static void main(String[] args) throws ProcessorException
	{
		try
		{
			if (args == null || args.length < 5)
			{
				String parameters = "\n\tParâmetro 1: diretorioBase (Path raiz para o subdiretório de imagens. Ex: /opt/extrai_descritores)"
						+ "\n\tParâmetro 2: diretorioImagens (Nome do subdiretório de imagens. Ex: imagens)"
						+ "\n\tParâmetro 3: diretorioDescritores (Nome do subdiretório de descritores. Ex: descritores)"
						+ "\n\tParâmetro 4: diretorioPares (Nome do subdiretório para gravação do arquivos de pares. Ex: pares)"
						+ "\n\tParâmetro 5: diretorioLACDataset (Nome do subdiretório para gravação do arquivo de treino do LAC. Ex: lac_dataset)"
						+ "\n\tParâmetro 6: arquivoParesOriginal (Path completo para o arquivo de pares original. Ex: /opt/pares/arquivo-pares.dat)"
						+ "\n\tParâmetro 7 <opcional>: totalParesGerar (Total de pares para geração. Ex: 500000 - Default: 100000 )"
						+ "\n"
						;
				throw new ProcessorException("Parâmetros obrigatórios não informados." + parameters);
			}

			// parâmetros de entrada
			String diretorioBase = args[0];
			String diretorioImagens = args[1];
			String diretorioDescritores = args[2];
			String diretorioPares = args[3];
			String diretorioLACDataset = args[4];
			String arquivoParesOriginal = args[5];
			int totalParesGerar = 0;

			if (args.length >= 7)
			{
				try
				{
					totalParesGerar = new Integer(args[6]);
				}
				catch (Exception e)
				{
					throw new ProcessorException(String.format("Parâmetros totalParesGerar inválido. Favor informar um valor válido. Valor informado: '%s'.", args[5]));
				}
			}

			/*
			 * Passos Batch:
			 * 1. Extrair descritores da imagem de consulta.
			 * 2. Gerar arquivos de pares aleatórios e normalizados.
			 * 3. Converter arquivo de pares para formato weka.
			 * 4. Discretizar arquivos de pares.
			 * 5. Gerar arquivo de treino no formato do LAC.
			 */

			// arquivos de pares
			String arquivoPares = new StringBuilder(diretorioBase).append('/').append(diretorioPares).append('/').append(ImageRankConstants.PAIR_OUTPUT_FILENAME).toString();
			String arquivoParesWeka = new StringBuilder(diretorioBase).append('/').append(diretorioPares).append('/').append(ImageRankConstants.WEKA_PAIR_OUTPUT_FILENAME).toString();
			String arquivoParesDiscretizados = new StringBuilder(diretorioBase).append('/').append(diretorioPares).append('/').append(ImageRankConstants.DISCRETIZED_WEKA_PAIR_OUTPUT_FILENAME).toString();

			// gera os pares aleatórios e normalizados
			LOG.info("[GERAÇÃO ARQUIVO DE PARES] - INICIO");
			RandomPairGenerator.generatePairs(diretorioBase, diretorioImagens, diretorioDescritores, diretorioPares, arquivoParesOriginal, totalParesGerar);
			LOG.info("[GERAÇÃO ARQUIVO DE PARES] - FIM");

			try
			{
				LOG.info("[CONVERSÃO ARQUIVO FORMATO WEKA] - INICIO");

				// converte pares para o formato do weka
				convertToWekaFile(arquivoPares, arquivoParesWeka);

				LOG.info("[CONVERSÃO ARQUIVO FORMATO WEKA] - FIM");
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Erro ao converter o arquivo de pares '%s' no formato do weka.", arquivoPares), e);
			}

			try
			{
				LOG.info("[DISCRETIZAÇÃO ARQUIVO WEKA] - INICIO");

				// discretiza o arquivo convertido no formato do weka
				discretizeWekaFile(arquivoParesWeka, arquivoParesDiscretizados);

				LOG.info("[DISCRETIZAÇÃO ARQUIVO WEKA] - FIM");
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Erro ao discretizar o arquivo '%s'.", arquivoParesWeka), e);
			}

			// gera arquivo de treino no formato do LAC
			LOG.info("[GERAÇÃO ARQUIVO TREINO FORMATO LAC] - INICIO");

			LACQueryFileConverter converter = new LACQueryFileConverter(diretorioBase, diretorioLACDataset, arquivoParesDiscretizados, arquivoPares);
			converter.convert(true, false);

			LOG.info("[GERAÇÃO ARQUIVO TREINO FORMATO LAC] - FIM");
		}
		catch (ProcessorException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Converte arquivo de pares em arquivo no formato weka (.arff)
	 * @param arquivoPares
	 * @param arquivoParesWeka
	 * @throws Exception
	 */
	private static void convertToWekaFile(String arquivoPares, String arquivoParesWeka) throws Exception
	{
		String[] columnsToIgnore = {"pair_id"};
		String classColumn = "class";

		WekaConversor.generateWekaFile(arquivoPares, arquivoParesWeka, Arrays.asList(columnsToIgnore), classColumn);
	}

	/**
	 * Discretiza um arquivo do formato weka (.arff)
	 * @param arquivoParesWeka
	 * @param arquivoParesDiscretizados
	 * @throws Exception
	 */
	private static void discretizeWekaFile(String arquivoParesWeka, String arquivoParesDiscretizados) throws Exception
	{
		DiscretizeDataSet.discretize(arquivoParesWeka, arquivoParesDiscretizados);
	}
}
