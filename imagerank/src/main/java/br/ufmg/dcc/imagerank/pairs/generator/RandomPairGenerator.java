package br.ufmg.dcc.imagerank.pairs.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.pairs.dto.ParDTO;
import br.ufmg.dcc.imagerank.pairs.extractor.ExtratorPares;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class RandomPairGenerator
{
	private static final Log LOG = LogFactory.getLog(RandomPairGenerator.class);

	private static int TOTAL_PARES_A_GERAR = 100000*1; // default 100.000

	private static Map<String, Boolean> controlePares;

	/**
	 * Gera pares aleatórios, calculando as distâncias dos descritores de cada par, com base no arquivo de pares fornecido
	 * @param args
	 * @throws ProcessorException
	 */
	public static void main(String[] args) throws ProcessorException
	{
		if (args == null || args.length < 5)
		{
			String parameters = "\n\tParâmetro 1: diretorioBase (Path raiz para o subdiretório de imagens. Ex: /opt/extrai_descritores)"
					+ "\n\tParâmetro 2: diretorioImagens (Nome do subdiretório de imagens. Ex: imagens)"
					+ "\n\tParâmetro 3: diretorioDescritores (Nome do subdiretório de descritores. Ex: descritores)"
					+ "\n\tParâmetro 4: diretorioSaida (Nome do subdiretório para gravação do arquivos de pares. Ex: pares)"
					+ "\n\tParâmetro 5: arquivoPares (Path completo para o arquivo de pares. Ex: /opt/pares/arquivo-pares.dat)"
					+ "\n\tParâmetro 6 <opcional>: totalParesGerar (Total de pares para geração. Ex: 500000 - Default: 100000 )"
					+ "\n"
					;
			throw new ProcessorException("Parâmetros obrigatórios não informados." + parameters);
		}

		String diretorioBase = args[0];
		String diretorioImagens = args[1];
		String diretorioDescritores = args[2];
		String diretorioSaida = args[3];
		String arquivoPares = args[4];

		if (args.length >= 6)
		{
			try
			{
				TOTAL_PARES_A_GERAR = new Integer(args[5]);
			}
			catch (Exception e)
			{
				throw new ProcessorException(String.format("Parâmetros totalParesGerar inválido. Favor informar um valor válido. Valor informado: '%s'.", args[5]));
			}
		}

		controlePares = new HashMap<String, Boolean>();

		LOG.info("Iniciando processamento...");
		try
		{
			LOG.info("Montando pares de imagens...");

			// obtém os pares básicos de imagens para processamento
			List<ParDTO> pares = getParesImagens(diretorioBase, diretorioImagens, arquivoPares);

			// processa os pares, ou seja, calcula as distâncias entre os descritores de cada par
			ExtratorPares extrator = new ExtratorPares(diretorioBase, diretorioImagens, diretorioDescritores, diretorioSaida);
			extrator.processar(pares);
		}
		catch (ProcessorException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
			throw new ProcessorException(e.getMessage(), e);
		}

		LOG.info("Fim...");
	}

	/**
	 * Obtém os pares básicos de imagens para processamento
	 * @param diretorioBase
	 * @param diretorioImagens
	 * @param arquivoPares
	 * @return
	 * @throws IOException
	 */
	private static List<ParDTO> getParesImagens(String diretorioBase, String diretorioImagens, String arquivoPares) throws IOException
	{
		List<ParDTO> pares = new ArrayList<ParDTO>();

		FileReader fr = null;
		BufferedReader br = null;
		LineNumberReader lnr = null;

		try
		{
			fr = new FileReader(arquivoPares);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);

			int totalLineNumbers = getTotalLineNumbers(arquivoPares);
			Set<Integer> randomLineNumbers = getRandomLineNumbers(totalLineNumbers);

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				int lineNumber = lnr.getLineNumber();
				if (randomLineNumbers.contains(lineNumber))
				{
					String[] dadosLinha = StringUtils.split(line, StringUtils.SPACE);

					String[] dadosImagem1 = dadosLinha[0].split("/");
					String imagem1 = dadosImagem1[dadosImagem1.length-1];

					String[] dadosImagem2 = dadosLinha[1].split("/");
					String imagem2 = dadosImagem2[dadosImagem2.length-1];

					// verifica se o par existe
					if (existsPair(imagem1, imagem2))
					{
						continue;
					}

					// adiciona o par no mapa para controle
					controlePares.put(getPairKey(imagem1, imagem2, false), true);
					controlePares.put(getPairKey(imagem1, imagem2, true), true);

					imagem1 = getPathImagem(diretorioBase, diretorioImagens, imagem1);
					imagem2 = getPathImagem(diretorioBase, diretorioImagens, imagem2);
					int classe = Integer.valueOf(dadosLinha[2]);

					pares.add(new ParDTO(new File(imagem1), new File(imagem2), classe));
				}
			}

			// libera memória
			controlePares = new HashMap<String, Boolean>();

			return pares;
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(fr);
		}
	}

	/**
	 * Gera a chave do par de imagem
	 * @param key1
	 * @param key2
	 * @param inverted
	 * @return
	 */
	private static String getPairKey(String key1, String key2, boolean inverted)
	{
		return new StringBuilder().append((!inverted ? key1 : key2)).append("_").append((!inverted ? key2 : key1)).toString();
	}

	/**
	 * Verifica se o par da imagem já existe
	 * @param key1
	 * @param key2
	 * @return
	 */
	private static boolean existsPair(String key1, String key2)
	{
		boolean exists = false;

		String key = getPairKey(key1, key2, false);
		exists = controlePares.containsKey(key);
		if (!exists)
		{
			String invertedKey = getPairKey(key1, key2, true);
			exists = controlePares.containsKey(invertedKey);
		}

		return exists;
	}

	/**
	 * Obtém o total de linhas do arquivo de pares original
	 * @param arquivoPares
	 * @return
	 * @throws IOException
	 */
	private static int getTotalLineNumbers(String arquivoPares) throws IOException
	{
		int total = 0;

		FileReader fr = null;
		BufferedReader br = null;
		LineNumberReader lnr = null;

		try
		{
			File file = new File(arquivoPares);
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			lnr.skip(file.length());

			total = lnr.getLineNumber();
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(fr);
		}

		return total;
	}

	/**
	 * Obtém as linhas aleatórias do arquivo de pares original, para geração dos pares com distâncias
	 * @param totalLineNumbers
	 * @return
	 */
	private static Set<Integer> getRandomLineNumbers(int totalLineNumbers)
	{
		Random random = new Random(System.currentTimeMillis());

		Set<Integer> lines = new HashSet<Integer>();
		do
		{
			lines.add(random.nextInt((totalLineNumbers-1)));
		}
		while (lines.size() < TOTAL_PARES_A_GERAR);

		return lines;
	}

	/**
	 * Obtém o path base para processamento
	 * @param dir
	 * @return
	 */
	private static String getPathImagem(String diretorioBase, String diretorioImagens, String imagem)
	{
		return new StringBuilder(diretorioBase).append(File.separator).append(diretorioImagens).append(File.separator).append(imagem).toString();
	}
}
