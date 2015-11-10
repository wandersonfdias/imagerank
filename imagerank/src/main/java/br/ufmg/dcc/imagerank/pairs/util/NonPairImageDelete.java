package br.ufmg.dcc.imagerank.pairs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;


/**
 * Remove imagens que não fazem parte do arquivo de pares fornecido
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class NonPairImageDelete
{
	private static final Log LOG = LogFactory.getLog(NonPairImageDelete.class);

	/**
	 * Remove imagens que não fazem parte do arquivo de pares fornecido
	 * @param args
	 * <ul>
	 * 	<li>Parâmetro 1: diretorioBase (<i>Path raiz para o subdiretório de imagens. Ex: /opt/extrai_descritores</i>)</li>
	 *  <li>Parâmetro 2: diretorioImagens (<i>Nome do subdiretório de imagens. Ex: imagens</i>)</li>
	 *  <li>Parâmetro 3: arquivoPares (<i>Path completo para o arquivo de pares. Ex: /opt/pares/arquivo-pares.dat</i>)</li>
	 * </ul>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
//		if (args == null || args.length < 3)
//		{
//			throw new Exception("Parâmetros obrigatórios não informados.");
//		}
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = "imagens";
		String arquivoPares = diretorioBase + "/diego/tag-classes-reduced.dat";

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new FileReader(arquivoPares);
			bufferedReader = new BufferedReader(fileReader);
			Set<String> imagens = new HashSet<String>();

			String linha = null;
			while ((linha = bufferedReader.readLine()) != null)
			{
				if (StringUtils.isNotBlank(linha))
				{
					String[] dadosLinha = StringUtils.split(linha, StringUtils.SPACE);

					String[] dadosImagem1 = dadosLinha[0].split("/");
					String imagem1 = dadosImagem1[dadosImagem1.length-1];

					String[] dadosImagem2 = dadosLinha[1].split("/");
					String imagem2 = dadosImagem2[dadosImagem2.length-1];

					imagens.add(imagem1);
					imagens.add(imagem2);
				}
				else
				{
					break;
				}
			}

			List<File> images = getImages(getFullPath(diretorioBase, diretorioImagens));
			for (File image : images)
			{
				if (!imagens.contains(image.getName()))
				{
					// remove a imagem
					image.delete();
				}
			}
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(fileReader);
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	/**
	 * Obtém as imagens
	 * @param diretorioImagens
	 * @return
	 */
	private static List<File> getImages(String diretorioImagens)
	{
		Collection<File> files = FileUtils.listFiles(new File(diretorioImagens), new String[]{ImageRankConstants.JPEG_IMAGE_EXTENSION}, true);
		return ((files != null && !files.isEmpty()) ? new LinkedList<File>(files) : null); // mantém a ordem da lista
	}

	/**
	 * Obtém o path base para processamento
	 * @param diretorioBase
	 * @param dir
	 * @return
	 */
	private static String getFullPath(String diretorioBase, String dir)
	{
		return new StringBuilder(diretorioBase).append(File.separator).append(dir).append(File.separator).toString();
	}
}
