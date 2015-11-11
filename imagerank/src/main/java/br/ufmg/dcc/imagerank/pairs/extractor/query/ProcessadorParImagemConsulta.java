package br.ufmg.dcc.imagerank.pairs.extractor.query;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.ImageProcessorException;
import br.ufmg.dcc.imagerank.pairs.dto.ParDTO;
import br.ufmg.dcc.imagerank.util.ImageRankUtils;

/**
 * Processador de par de imagens para consulta.
 * <br>Calcula as distâncias entre os descritores das imagens de consulta/comparação.
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessadorParImagemConsulta
{
	private static final Log LOG = LogFactory.getLog(ProcessadorParImagemConsulta.class);

	private ParDTO par;
	private List<File> descritoresBaseConsulta;
	private List<File> descritoresBaseCompleta;

	/**
	 * Diretório da base de consulta para processamento das imagens
	 */
	private String diretorioBaseConsulta;

	/**
	 * Diretório da base de completa para processamento das imagens
	 */
	private String diretorioBaseCompleta;

	private static final MathContext MATH_CONTEXT = new MathContext(ImageRankConstants.DESCRIPTOR_PRECISION_VALUE); // define a precisão

	private static Map<String, String[]> cacheDescritoresImagemConsulta = new HashMap<String, String[]>();

	/**
	 * Construtor
	 * @param par
	 * @param descritoresBaseConsulta
	 * @param descritoresBaseCompleta
	 * @param diretorioBaseConsulta
	 * @param diretorioBaseCompleta
	 */
	public ProcessadorParImagemConsulta(ParDTO par, List<File> descritoresBaseConsulta, List<File> descritoresBaseCompleta, String diretorioBaseConsulta, String diretorioBaseCompleta)
	{
		super();
		this.par = par;
		this.descritoresBaseConsulta = descritoresBaseConsulta;
		this.descritoresBaseCompleta = descritoresBaseCompleta;
		this.diretorioBaseConsulta = diretorioBaseConsulta;
		this.diretorioBaseCompleta = diretorioBaseCompleta;
	}

	/**
	 * Limpa cache de processamento
	 */
	public static void clearCache()
	{
		cacheDescritoresImagemConsulta = new HashMap<String, String[]>();
	}

	/**
	 * Realiza o processamento de uma imagem
	 * @return
	 * @throws ImageProcessorException
	 */
	public void processar() throws ImageProcessorException
	{
		if (this.par == null || this.diretorioBaseConsulta == null || this.diretorioBaseCompleta == null
			|| this.descritoresBaseConsulta == null || this.descritoresBaseCompleta.isEmpty()
			|| this.descritoresBaseCompleta == null || this.descritoresBaseCompleta.isEmpty())
		{
			throw new ImageProcessorException("Informações obrigatórias para processamento não informadas.");
		}

		if (LOG.isDebugEnabled())
		{
			System.out.printf("imagem origem: %s - imagem destino: %s", this.par.getImagem1().getPath(), this.par.getImagem2().getPath());
			System.out.println();
		}

		// gera cache dos descritores da imagem de consulta (imagem1), para performance
		if (cacheDescritoresImagemConsulta.isEmpty())
		{
			for (File descritorBaseConsulta : this.descritoresBaseConsulta)
			{
				String cacheKey = descritorBaseConsulta.getName();
				String[] valoresDescritorImagemConsulta = this.getDescriptorValuesForImage(this.diretorioBaseConsulta, descritorBaseConsulta, this.par.getImagem1());
				cacheDescritoresImagemConsulta.put(cacheKey, valoresDescritorImagemConsulta);
			}
		}

		for (File descritorBaseConsulta : this.descritoresBaseConsulta)
		{
			// obtém descritores da imagem de consulta (usa o cache para performance)
			String cacheKey = descritorBaseConsulta.getName();
			String[] valoresDescritorImagemConsulta = cacheDescritoresImagemConsulta.get(cacheKey);

			// obtém descritores da imagem de comparação
			File descritorBaseCompleta = this.getDescritorBaseCompleta(descritorBaseConsulta.getName());
			String[] valoresDescritorImagemComparacao = this.getDescriptorValuesForImage(this.diretorioBaseCompleta, descritorBaseCompleta, this.par.getImagem2());

			// calcula a distância entre os descritores
			BigDecimal distance = this.calculateDescriptorDistance(valoresDescritorImagemConsulta, valoresDescritorImagemComparacao);

			// armazena a distância do descritor
			this.par.getDistancias().put(descritorBaseConsulta.getName(), distance);

			if (LOG.isDebugEnabled())
			{
				System.out.printf("descritor: %s - distância: %s", descritorBaseConsulta.getName(), distance);
				System.out.println();
			}
		}

		if (LOG.isDebugEnabled())
		{
			System.out.println();
			System.out.println(StringUtils.repeat("-", 20));
		}
	}

	/**
	 * Obtém o descritor da base completa
	 * @param descriptorName Nome do descritor para busca
	 * @return
	 */
	private File getDescritorBaseCompleta(String descriptorName)
	{
		File descriptor = null;

		for (File descriptorFile : this.descritoresBaseCompleta)
		{
			if (descriptorFile.getName().equalsIgnoreCase(descriptorName))
			{
				descriptor = descriptorFile;
				break;
			}
		}

		return descriptor;
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
	 * @param diretorioBase
	 * @param descriptorFile
	 * @param imageFile
	 * @return
	 * @throws ImageProcessorException
	 */
	private String[] getDescriptorValuesForImage(String diretorioBase, File descriptorFile, File imageFile) throws ImageProcessorException
	{
		String descriptorFileName = null;
		try
		{
			String[] descriptors = null;

			descriptorFileName = getDescriptorName(diretorioBase, descriptorFile, imageFile);
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
	private String getDescriptorName(String diretorioBase, File descriptorFile, File imageFile)
	{
		StringBuilder descriptorName = new StringBuilder(descriptorFile.getPath());
		descriptorName.append(File.separator);
		descriptorName.append(ImageRankUtils.getDirWithoutBaseDir(imageFile, diretorioBase));
		descriptorName.append(File.separator);
		descriptorName.append(ImageRankUtils.getNameWithoutExtension(imageFile.getName()));
		descriptorName.append(ImageRankConstants.FILENAME_SEPARATOR);
		descriptorName.append(descriptorFile.getName());

		return descriptorName.toString();
	}
}
