package br.ufmg.dcc.imagerank;

import br.ufmg.dcc.imagerank.eval.converter.EvalFileConverter;
import br.ufmg.dcc.imagerank.exception.ProcessorException;

/**
 * Classe para teste do conversor dos arquivos de entrada para o EvalScore
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class EvalConverterTest
{
	public static void main(String[] args)
	{
		try
		{
			String[] dados = {"m2"}; //, "m3", "m4", "m5"};

			for (String prefix : dados)
			{
				System.out.println("Gerando dados para: "+ prefix);
				String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
				String diretorioSaida = "eval_dataset/"+prefix;
				String arquivoSaidaLAC = diretorioBase + "/lac/saida/" + prefix + ".log";
				String arquivoEntradaTesteLAC = diretorioBase + "/lac_dataset/teste";

				EvalFileConverter converter = new EvalFileConverter(diretorioBase, diretorioSaida, arquivoSaidaLAC, arquivoEntradaTesteLAC);
				converter.convert();

			}
		}
		catch (ProcessorException e)
		{
			e.printStackTrace();
		}
	}
}
