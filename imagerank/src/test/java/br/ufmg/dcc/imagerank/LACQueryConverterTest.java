package br.ufmg.dcc.imagerank;

import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.lac.converter.LACQueryFileConverter;


/**
 * Classe para teste de conversão dos pares discretizados em arquivos de treino e/ou teste, considerando consulta, para entrada do LAC
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACQueryConverterTest
{
	public static void main(String[] args)
	{
		try
		{
			String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
			String diretorioSaida = "lac_dataset";
			String arquivoWeka = diretorioBase + "/pares_discretizados.arff";
			String arquivoOriginal = diretorioBase + "/pares/saida.txt";

			LACQueryFileConverter converter = new LACQueryFileConverter(diretorioBase, diretorioSaida, arquivoWeka, arquivoOriginal);
			converter.convert(true, true);
		}
		catch (ProcessorException e)
		{
			e.printStackTrace();
		}
	}
}
