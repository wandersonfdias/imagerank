package br.ufmg.dcc.imagerank.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.eval.converter.EvalFileConverter;
import br.ufmg.dcc.imagerank.exception.ProcessorException;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImageRankEvalConverter
{
	private static final Log LOG = LogFactory.getLog(ImageRankEvalConverter.class);

	/**
	 * Converte arquivos de entrada e saída do LAC em arquivos para teste com EvalScript
	 * @throws ProcessorException
	 */
	public static void main(String[] args) throws ProcessorException
	{
		try
		{
			if (args == null || args.length < 4)
			{
				String parameters = "\n\tParâmetro 1: diretorioBaseConsulta (Path completo para o diretório da base de imagens de consulta. Ex: /imagerank/base_consulta)"
						+ "\n\tParâmetro 2: diretorioSaida (Nome do subdiretório para gravação dos arquivos do Eval. Ex: eval)"
						+ "\n\tParâmetro 3: arquivoSaidaLAC (Path completo e nome do arquivo de saída do LAC. Ex: /imagerank/base_consulta/lac_output/lac_result.txt)"
						+ "\n\tParâmetro 4: arquivoEntradaTesteLAC (Nome do arquivo de entrada para teste do LAC. Ex: /imagerank/base_consulta/lac_dataset/teste.txt)"
						+ "\n"
						;
				throw new ProcessorException("Parâmetros obrigatórios não informados." + parameters);
			}

			// parâmetros de entrada
			String diretorioBaseConsulta = args[0];
			String diretorioSaida = args[1];
			String arquivoSaidaLAC = args[2];
			String arquivoEntradaTesteLAC = args[3];


			// Gera saída com top-K imagens similares (nome da imagem, score)
			LOG.info("[GERAÇÃO ARQUIVOS PARA TESTE EVAL] - INICIO");

			EvalFileConverter evalFileConverter = new EvalFileConverter(diretorioBaseConsulta, diretorioSaida, arquivoSaidaLAC, arquivoEntradaTesteLAC);
			evalFileConverter.convert();

			LOG.info("[GERAÇÃO ARQUIVOS PARA TESTE EVAL] - FIM");
		}
		catch (ProcessorException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}
}
