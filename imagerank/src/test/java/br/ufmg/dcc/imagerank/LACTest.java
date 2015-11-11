package br.ufmg.dcc.imagerank;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.shell.util.ShellCommandRunner;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACTest
{
	/**
	 * @param args
	 * @throws ProcessorException
	 */
	public static void main(String[] args) throws ProcessorException
	{
		String diretorioBaseCompleta = System.getenv("HOME") + "/extrai_descritores";
		String diretorioBaseConsulta = System.getenv("HOME") + "/extrai_descritores";
		String diretorioLACDataset = "lac_dataset";
		String diretorioExecucaoLAC = System.getenv("HOME") + "/extrai_descritores/lac";
		String diretorioSaidaLAC = System.getenv("HOME") +"/extrai_descritores/lac_output";

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
}
