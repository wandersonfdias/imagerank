package br.ufmg.dcc.imagerank.main;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.lac.converter.LACQueryFileConverter;
import br.ufmg.dcc.imagerank.lac.converter.LACScoreOutputFileConverter;
import br.ufmg.dcc.imagerank.pairs.extractor.query.ExtratorParesConsulta;
import br.ufmg.dcc.imagerank.shell.util.ShellCommandRunner;
import br.ufmg.dcc.imagerank.weka.util.DiscretizeDataSet;
import br.ufmg.dcc.imagerank.weka.util.WekaConversor;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImageRankOnlineQueryProcessor
{
	private static final Log LOG = LogFactory.getLog(ImageRankOnlineQueryProcessor.class);

	/**
	 * Exceuta os seguintes passos online:<br>
	 * 1. Extrair descritores da imagem de consulta.<br>
	 * 2. Gerar arquivo de pares de consulta, normalizado, com imagem de consulta + X imagens aleatórias para comparação.<br>
	 * 3. Converter arquivo de pares de consulta para formato weka.<br>
	 * 4. Discretizar arquivos de pares de consulta.<br>
	 * 5. Gerar arquivo de teste no formato do LAC.<br>
	 * 6. Rodar o algoritmo do LAC.<br>
	 * 7. Gerar saída com top-K imagens similares (nome da imagem, score).<br>
	 * @throws ProcessorException
	 */
	public static void main(String[] args) throws ProcessorException
	{
		try
		{
			// TODO Wanderson - adicionar parâmetro de imagem de consulta e validá-lo. Validar existência da imagem e copiá-la para o diretório de processamento da consulta (parametrizar esse diretório).

			if (args == null || args.length < 10)
			{
				String parameters = "\n\tParâmetro 1: diretorioBaseCompleta (Path completo para o diretório da base de imagens completa. Ex: /imagerank/base_completa)"
						+ "\n\tParâmetro 2: diretorioBaseConsulta (Path completo para o diretório da base de imagens de consulta. Ex: /imagerank/base_consulta)"
						+ "\n\tParâmetro 3: diretorioImagens (Nome do subdiretório de imagens. Ex: imagens)"
						+ "\n\tParâmetro 4: diretorioDescritores (Nome do subdiretório de descritores. Ex: descritores)"
						+ "\n\tParâmetro 5: diretorioPares (Nome do subdiretório para gravação do arquivos de pares. Ex: pares)"
						+ "\n\tParâmetro 6: diretorioLACDataset (Nome do subdiretório para gravação do arquivo de treino/teste do LAC. Ex: lac_dataset)"
						+ "\n\tParâmetro 7: diretorioExecucaoLAC (Path completo para execução do LAC. Ex: /imagerank/bin/lac)"
						+ "\n\tParâmetro 8: diretorioSaidaLAC (Path completo para gravação do arquivo de sáida do LAC. Ex: /imagerank/lac_output)"
						+ "\n\tParâmetro 9: diretorioSaidaScore (Nome do subdiretório para gravação do arquivo de saída do score dentro do diretório da base de consulta. Ex: score_output)"
						+ "\n\tParâmetro 10: totalParesGerar (Total de pares para geração. Ex: 500)"
						+ "\n"
						;
				throw new ProcessorException("Parâmetros obrigatórios não informados." + parameters);
			}

			// parâmetros de entrada
			String diretorioBaseCompleta = args[0];
			String diretorioBaseConsulta = args[1];
			String diretorioImagens = args[2];
			String diretorioDescritores = args[3];
			String diretorioPares = args[4];
			String diretorioLACDataset = args[5];
			String diretorioExecucaoLAC = args[6];
			String diretorioSaidaLAC = args[7];
			String diretorioSaidaScore = args[8];
			int totalParesGerar = 500;
//			int topKImagesToReturn = 30; // TODO Wanderson - implementar

			try
			{
				totalParesGerar = new Integer(args[9]);
				if (totalParesGerar <= 0)
				{
					throw new IllegalArgumentException();
				}
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Parâmetros totalParesGerar inválido. Favor informar um valor válido. Valor informado: '%s'.", args[9]));
			}

				// TODO Wanderson - implementar
//				if (args.length >= 10)
//				{
//					try
//					{
//						topKImagesToReturn = new Integer(args[10]);
//					}
//					catch (Exception e)
//					{
//						throw new ProcessorException(String.format("Parâmetros topKImagesToReturn inválido. Favor informar um valor válido. Valor informado: '%s'.", args[10]));
//					}
//				}


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

			// arquivos de pares de consulta
			String arquivoParesConsulta = new StringBuilder(diretorioBaseConsulta).append(File.separator).append(diretorioPares).append(File.separator).append(ImageRankConstants.PAIR_OUTPUT_FILENAME).toString();
			String arquivoParesWekaConsulta = new StringBuilder(diretorioBaseConsulta).append(File.separator).append(diretorioPares).append(File.separator).append(ImageRankConstants.WEKA_PAIR_OUTPUT_FILENAME).toString();
			String arquivoParesDiscretizadosConsulta = new StringBuilder(diretorioBaseConsulta).append(File.separator).append(diretorioPares).append(File.separator).append(ImageRankConstants.DISCRETIZED_WEKA_PAIR_OUTPUT_FILENAME).toString();

			// 1. Extrair descritores da imagem de consulta
			LOG.info("[EXTRAÇÃO DESCRITORES BASE CONSULTA] - INICIO");
			extractQueryImageDescriptors(diretorioBaseConsulta);
			LOG.info("[EXTRAÇÃO DESCRITORES BASE CONSULTA] - FIM");

			// gera os pares aleatórios e normalizados, considerando a imagem de consulta
			LOG.info("[GERAÇÃO ARQUIVO DE PARES] - INICIO");
			ExtratorParesConsulta extratorParesConsulta = new ExtratorParesConsulta(diretorioBaseCompleta, diretorioBaseConsulta, diretorioImagens, diretorioDescritores, diretorioPares, totalParesGerar);
			extratorParesConsulta.processar();
			LOG.info("[GERAÇÃO ARQUIVO DE PARES] - FIM");

			try
			{
				LOG.info("[CONVERSÃO ARQUIVO FORMATO WEKA] - INICIO");

				// converte pares para o formato do weka
				convertToWekaFile(arquivoParesConsulta, arquivoParesWekaConsulta);

				LOG.info("[CONVERSÃO ARQUIVO FORMATO WEKA] - FIM");
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Erro ao converter o arquivo de pares '%s' no formato do weka.", arquivoParesConsulta), e);
			}

			try
			{
				LOG.info("[DISCRETIZAÇÃO ARQUIVO WEKA] - INICIO");

				// discretiza o arquivo convertido no formato do weka
				discretizeWekaFile(arquivoParesWekaConsulta, arquivoParesDiscretizadosConsulta);

				LOG.info("[DISCRETIZAÇÃO ARQUIVO WEKA] - FIM");
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Erro ao discretizar o arquivo '%s'.", arquivoParesWekaConsulta), e);
			}

			// gera arquivo de treino no formato do LAC
			LOG.info("[GERAÇÃO ARQUIVO TREINO FORMATO LAC] - INICIO");

			LACQueryFileConverter converter = new LACQueryFileConverter(diretorioBaseConsulta, diretorioLACDataset, arquivoParesDiscretizadosConsulta, arquivoParesConsulta);
			converter.convert(false, true);

			LOG.info("[GERAÇÃO ARQUIVO TREINO FORMATO LAC] - FIM");

			// Executa o algoritmo do LAC
			String arquivoSaidaProcessamentoLAC = new StringBuilder(diretorioSaidaLAC).append(File.separator).append(ImageRankConstants.LAC_OUTPUT_FILENAME).toString();
			runLAC(diretorioBaseCompleta, diretorioBaseConsulta, diretorioLACDataset, diretorioExecucaoLAC, diretorioSaidaLAC);

			// Gera saída com top-K imagens similares (nome da imagem, score)
			LOG.info("[GERAÇÃO ARQUIVO DE SAÍDA COM SCORE] - INICIO");

			LACScoreOutputFileConverter scoreConverter = new LACScoreOutputFileConverter(diretorioBaseConsulta, diretorioSaidaScore, arquivoSaidaProcessamentoLAC);
			scoreConverter.convert();

			LOG.info("[GERAÇÃO ARQUIVO DE SAÍDA COM SCORE] - FIM");
		}
		catch (ProcessorException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void extractQueryImageDescriptors(String diretorioBaseConsulta) throws ProcessorException
	{
		String comando = "sh main.sh";
		String[] parametros = null;
		String diretorioSaidaProcessamento = new StringBuilder(diretorioBaseConsulta).append(File.separator).append("log").toString();
		String arquivoSaidaProcessamento = "extracao_descritores.log";

		ShellCommandRunner shell = new ShellCommandRunner(diretorioBaseConsulta, comando, parametros, diretorioSaidaProcessamento, arquivoSaidaProcessamento);
		int status = shell.run();

		if (status != 0)
		{
			String msgErro = StringUtils.EMPTY;
			if (shell.getSaidaErro() != null && !shell.getSaidaErro().isEmpty())
			{
				msgErro = StringUtils.join(shell.getSaidaErro().toArray(), '\n').trim();
			}

			throw new ProcessorException(String.format("Ocorreu um erro inesperado na extração dos descritores da base de consulta.\nDETALHE: \"%s\".", msgErro));
		}
	}

	private static void runLAC(String diretorioBaseCompleta, String diretorioBaseConsulta, String diretorioLACDataset, String diretorioExecucaoLAC, String diretorioSaidaLAC) throws ProcessorException
	{
		String comando = "lazy";
		String dataSetTreino = new StringBuilder(diretorioBaseCompleta).append(File.separator).append(diretorioLACDataset).append(File.separator).append(ImageRankConstants.LAC_TRAINING_FILENAME).toString();
		String dataSetTeste = new StringBuilder(diretorioBaseConsulta).append(File.separator).append(diretorioLACDataset).append(File.separator).append(ImageRankConstants.LAC_TEST_FILENAME).toString();

		String[] parametros = { "-i" // dataset de treino
							  , dataSetTreino

							  // dataset de teste
							  , "-t"
							  , dataSetTeste

							  // suporte mínimo
							  , "-s" // parâmetro
							  , "1" // valor

							  // confiança mínima
							  , "-c" // parâmetro
							  , "0.01" // valor

							  // quantidade máxima de regras a serem geradas
							  , "-m" // parâmetro
							  , "2" // valor

							  // cache
							  , "-e" // parâmetro
							  , "10000000" // valor
							  };

		ShellCommandRunner shell = new ShellCommandRunner(diretorioExecucaoLAC, comando, parametros, diretorioSaidaLAC, ImageRankConstants.LAC_OUTPUT_FILENAME);
		int status = shell.run();

		if (status != 0)
		{
			String msgErro = StringUtils.EMPTY;
			if (shell.getSaidaErro() != null && !shell.getSaidaErro().isEmpty())
			{
				msgErro = StringUtils.join(shell.getSaidaErro().toArray(), '\n').trim();
			}

			throw new ProcessorException(String.format("Ocorreu um erro inesperado na execução do LAC.\nDETALHE: \"%s\".", msgErro));
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
