package br.ufmg.dcc.imagerank.pairs.extractor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ImageProcessorException;
import br.ufmg.dcc.imagerank.pairs.dto.ParDTO;
import br.ufmg.dcc.imagerank.util.ImageRankUtils;

/**
 * Processador de par de imagens.
 * <br>Calcula as distâncias entre os descritores das imagens.
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessadorParImagem
{
	private static final Log LOG = LogFactory.getLog(ProcessadorParImagem.class);

	private ParDTO par;
	private List<File> descriptors;
	private String diretorioBase;
	private static final MathContext MATH_CONTEXT = new MathContext(ImageRankConstants.DESCRIPTOR_PRECISION_VALUE); // define a precisão

	public ProcessadorParImagem(ParDTO par, List<File> descriptors, String diretorioBase)
	{
		super();
		this.par = par;
		this.descriptors = descriptors;
		this.diretorioBase = diretorioBase;
	}

	/**
	 * Realiza o processamento de uma imagem
	 * @return
	 * @throws ImageProcessorException
	 */
	public void processar() throws ImageProcessorException
	{
		if (this.par == null || this.descriptors == null || this.descriptors.isEmpty())
		{
			throw new ImageProcessorException("Informações obrigatórias para processamento não informadas.");
		}

		if (LOG.isDebugEnabled())
		{
			LOG.info(String.format("imagem origem: %s - imagem destino: %s", this.par.getImagem1().getPath(), this.par.getImagem2().getPath()));
		}

		for (File descriptorFile : this.descriptors)
		{
			// obtém os descritores das imagens dos pares
			String[] descriptorImage1 = this.getDescriptorValuesForImage(descriptorFile, this.par.getImagem1());
			String[] descriptorImage2 = this.getDescriptorValuesForImage(descriptorFile, this.par.getImagem2());

			// calcula a distância entre os descritores
			BigDecimal distance = this.calculateDescriptorDistance(descriptorImage1, descriptorImage2);

			// armazena a distância do descritor
			this.par.getDistancias().put(descriptorFile.getName(), distance);

			if (LOG.isDebugEnabled())
			{
				LOG.info(String.format("descritor: %s - distância: %s", descriptorFile.getName(), distance));
			}
		}

		if (LOG.isDebugEnabled())
		{
			LOG.info(StringUtils.repeat("-", 20));
		}
	}

	/**
	 * Calcula a distância entre um tipo de descritor de duas imagens
	 * @param descriptorForCurrentImage
	 * @param descriptorForImage
	 * @return
	 */
	private BigDecimal calculateDescriptorDistance(String[] descriptorForCurrentImage, String[] descriptorForImage)
	{
		BigDecimal distance = new BigDecimal(0, MATH_CONTEXT);

		BigDecimal currentImageValue = null;
		BigDecimal imageValue = null;

		for (int i=0; i<descriptorForCurrentImage.length; i++)
		{
			currentImageValue = new BigDecimal(((descriptorForCurrentImage[i] != null && descriptorForCurrentImage[i].trim().length() > 0) ? descriptorForCurrentImage[i] : "0"), MATH_CONTEXT);
			imageValue = new BigDecimal(((descriptorForImage[i] != null && descriptorForImage[i].trim().length() > 0) ? descriptorForImage[i] : "0"), MATH_CONTEXT);

			distance = distance.add(currentImageValue.subtract(imageValue).abs());
		}

		currentImageValue = null;
		imageValue = null;

		return distance;
	}

	/**
	 * Obtém os valores de um descritor de uma imagem
	 * @param descriptorFile
	 * @param imageFile
	 * @return
	 * @throws ImageProcessorException
	 */
	private String[] getDescriptorValuesForImage(File descriptorFile, File imageFile) throws ImageProcessorException
	{
		String descriptorFileName = null;
		try
		{
			String[] descriptors = null;

			descriptorFileName = getDescriptorName(descriptorFile, imageFile);
			String descriptorContent = FileUtils.readFileToString(new File(descriptorFileName));

			if (StringUtils.isBlank(descriptorContent))
			{
				throw new ImageProcessorException(String.format("Não foi possível obter o conteúdo do descritor '%s'.", descriptorFileName));
			}
			else
			{
				String[] data = StringUtils.split(descriptorContent.trim(), "\n");
				if (data.length < 2)
				{
					throw new ImageProcessorException(String.format("Conteúdo do descritor '%s' está fora do padrão. Padrão: <total de atributos>\\n<valores dos atributos>'.", descriptorFileName));
				}
				else
				{
					int attributes = Integer.valueOf(data[0]);
					String content = data[1];

					String[] contentData = StringUtils.split(content, StringUtils.SPACE);
					if (contentData.length == attributes || contentData.length > 1)
					{
						descriptors = contentData;
					}
					else
					{
						descriptors = content.split(StringUtils.EMPTY);
					}
				}
			}

			return descriptors;
		}
		catch (IOException e)
		{
			throw new ImageProcessorException("Ocorreu um erro ao obter o conteúdo do descritor '" + descriptorFileName + "'.",e );
		}
	}

	/**
	 * Obtém o o nome completo de um descritor
	 * @param descriptorFile
	 * @param imageFile
	 * @return
	 */
	private String getDescriptorName(File descriptorFile, File imageFile)
	{
		StringBuilder descriptorName = new StringBuilder(descriptorFile.getPath());
		descriptorName.append(File.separator);
		descriptorName.append(ImageRankUtils.getDirWithoutBaseDir(imageFile, this.diretorioBase));
		descriptorName.append(File.separator);
		descriptorName.append(ImageRankUtils.getNameWithoutExtension(imageFile.getName()));
		descriptorName.append(ImageRankConstants.FILENAME_SEPARATOR);
		descriptorName.append(descriptorFile.getName());

		return descriptorName.toString();
	}
}
