package br.ufmg.dcc.imagerank.pairs.extractor.query;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.dcc.imagerank.constants.ImageRankConstants;
import br.ufmg.dcc.imagerank.exception.DescriptorsNotFoundException;
import br.ufmg.dcc.imagerank.exception.ImageProcessorException;
import br.ufmg.dcc.imagerank.exception.ImagesNotFoundException;
import br.ufmg.dcc.imagerank.exception.ProcessorException;
import br.ufmg.dcc.imagerank.pairs.dto.ParDTO;
import br.ufmg.dcc.imagerank.util.ImageRankUtils;

/**
 * Extrai pares de imagens para uma consulta específica
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ExtratorParesConsulta
{
	private static final MathContext MATH_CONTEXT = new MathContext(ImageRankConstants.DESCRIPTOR_PRECISION_VALUE);
	private static final Log LOG = LogFactory.getLog(ExtratorParesConsulta.class);

	/**
	 * Diretório da base de completa para processamento das imagens
	 */
	private String diretorioBaseCompleta;

	/**
	 * Diretório da base de consulta para processamento das imagens
	 */
	private String diretorioBaseConsulta;

	/**
	 * Diretório base das imagens
	 */
	private String diretorioImagens;

	/**
	 * Diretório base dos descritores
	 */
	private String diretorioDescritores;

	/**
	 * Diretório dos arquivos de saída
	 */
	private String diretorioSaida;

	/**
	 * Total de pares a serem gerados
	 */
	private int totalParesGerar;

	private Map<Integer, Boolean> pares;
	private Random randomGenerator;
	private static int contador = 0;

	/**
	 * @param diretorioBaseCompleta
	 * @param diretorioBaseConsulta
	 * @param diretorioImagens
	 * @param diretorioDescritores
	 * @param diretorioSaida
	 * @param totalParesGerar
	 */
	public ExtratorParesConsulta(String diretorioBaseCompleta, String diretorioBaseConsulta, String diretorioImagens, String diretorioDescritores, String diretorioSaida, int totalParesGerar)
	{
		super();
		this.diretorioBaseCompleta = StringUtils.trimToNull(diretorioBaseCompleta);
		this.diretorioBaseConsulta = StringUtils.trimToNull(diretorioBaseConsulta);
		this.diretorioImagens = StringUtils.trimToNull(diretorioImagens);
		this.diretorioDescritores = StringUtils.trimToNull(diretorioDescritores);
		this.diretorioSaida = StringUtils.trimToNull(diretorioSaida);
		this.totalParesGerar = totalParesGerar;
	}

	/**
	 * Valida os parâmetros de entrada
	 * @return
	 */
	private boolean isValidParameters()
	{
		return (StringUtils.isNotBlank(this.diretorioBaseCompleta) && StringUtils.isNotBlank(this.diretorioBaseConsulta)
				&& StringUtils.isNotBlank(this.diretorioImagens) && StringUtils.isNotBlank(this.diretorioDescritores)
				&& StringUtils.isNotBlank(this.diretorioSaida) && this.totalParesGerar > 0);
	}

	/**
	 * Inicializa variáveis para processamento
	 */
	private void inicializaVariavies()
	{
		this.pares = new HashMap<Integer, Boolean>();
		this.randomGenerator = new Random();
		this.randomGenerator.setSeed(System.currentTimeMillis());
		contador = 0;
	}

	/**
	 * Inicia o processamento das imagens e gera o arquivo de saída
	 * @throws ProcessorException
	 */
	public void processar() throws ProcessorException
	{
		this.inicializaVariavies();

		if (!this.isValidParameters())
		{
			throw new ProcessorException(String.format("Parâmetros de entrada informados inválidos. Diretório base completa: '%s' - Diretório base consulta: '%s' -  Diretório imagens: '%s' -  Diretório descritores: '%s' - Diretório saída: '%s' - Total pares gerar: '%d'."
													  , this.diretorioBaseCompleta
													  , this.diretorioBaseConsulta
													  , this.diretorioImagens
													  , this.diretorioDescritores
													  , this.diretorioSaida
													  , this.totalParesGerar
													  )
										);
		}
		else
		{
			// obtém  a imagem para consulta
			File imagemConsulta = this.getImagemBaseConsulta();
			if (imagemConsulta == null)
			{
				throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para consulta.");
			}

			// obtém os descritores da base de consulta
			List<File> descritoresBaseConsulta = this.getDescriptors(this.getFullPathBaseConsulta(this.diretorioDescritores));
			if (descritoresBaseConsulta == null || descritoresBaseConsulta.isEmpty())
			{
				throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens na base de consulta.");
			}

			// obtém a lista de imagens da base completa
			List<File> imagensBaseCompleta = this.getImagensBaseCompleta();
			if (imagensBaseCompleta == null || imagensBaseCompleta.isEmpty())
			{
				throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento na base completa.");
			}
			else
			{
				// obtém a lista de descritores da base completa
				List<File> descritoresBaseCompleta = this.getDescriptors(this.getFullPathBaseCompleta(this.diretorioDescritores));
				if (descritoresBaseCompleta == null || descritoresBaseCompleta.isEmpty())
				{
					throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens na base completa.");
				}
				else
				{
					// monta a lista de pares únicos de imagens
					List<ParDTO> pares = this.getParesImagens(imagemConsulta, imagensBaseCompleta);

					// processa os pares
					this.processar(pares, descritoresBaseConsulta, descritoresBaseCompleta);
				}
			}
		}
	}

	/**
	 * Gera pares de imagens, considerando a imagem de consulta
	 * @param imagemConsulta
	 * @param imagensBaseCompleta
	 * @return
	 */
	private List<ParDTO> getParesImagens(File imagemConsulta, List<File> imagensBaseCompleta)
	{
		// monta a lista de pares únicos de imagens
		List<ParDTO> pares = new LinkedList<ParDTO>(); // mantém a ordem da lista
		do
		{
			Integer randomNextUniqueImage = this.getRandomNextUniqueImage(imagensBaseCompleta.size());

			File imagemComparacao = imagensBaseCompleta.get(randomNextUniqueImage);
			pares.add(new ParDTO(imagemConsulta, imagemComparacao, 0)); // gera sempre com classe 0
		}
		while (pares.size() < this.totalParesGerar);

		return pares;
	}

	/**
	 * Processa os pares de imagens
	 * @param pares
	 * @param descritoresBaseConsulta
	 * @param descritoresBaseCompleta
	 * @throws ProcessorException
	 */
	private void processar(final List<ParDTO> pares, final List<File> descritoresBaseConsulta, final List<File> descritoresBaseCompleta) throws ProcessorException
	{
		LOG.info("Processando pares...");

		final ExecutorService poolThread = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		final List<ImageProcessorException> errosProcessamento = new ArrayList<ImageProcessorException>();

		for (final ParDTO par : pares)
		{
			if (LOG.isDebugEnabled())
			{
				System.out.printf("Processando par: %s - %s", par.getImagem1().getPath(), par.getImagem2().getPath());
				System.out.println();
			}

			poolThread.submit(new Runnable()
			{
				public void run()
				{
					try
					{
						// processa o par de imagens
						ProcessadorParImagemConsulta processadorParImagemConsulta = new ProcessadorParImagemConsulta(par, descritoresBaseConsulta, descritoresBaseCompleta, diretorioBaseConsulta, diretorioBaseCompleta);
						processadorParImagemConsulta.processar();
						processadorParImagemConsulta = null;
						contador++;
					}
					catch (ImageProcessorException e)
					{
						errosProcessamento.add(e);
					}
				}
			});
		}

		// Força a finalização do pool, sem cancelar as threads já iniciadas.
		poolThread.shutdown();
		int tempo = 1;
		do
		{
			try
			{
				if (tempo%5==0)
				{
					System.out.println("Pares processados: " + contador + " - " + tempo + "s");
				}
				Thread.sleep(1000);
				tempo++;
			}
			catch (InterruptedException e)
			{
				// ignora
			}
		}
		while(!poolThread.isTerminated());
		System.out.println("Pares processados: " + contador + " - " + tempo + "s");

		if (errosProcessamento.size() > 0)
		{
			LOG.error("Processamento Cancelado!!! Vide erros abaixo:");
			for (ImageProcessorException e : errosProcessamento)
			{
				LOG.error(e.getMessage(), e);
			}
			System.exit(1); // força sinal de erro
		}
		else
		{
			// normaliza os valores dos descritores dos pares
			LOG.info("Normalizando descritores dos pares...");
			this.normalizar(pares, descritoresBaseConsulta);

			// grava o arquivo de saída
			this.writeOutputFile(pares, descritoresBaseConsulta);
		}

		LOG.info("Fim do processamento");
	}

	/**
	 * Grava o arquivo de saída contendo os pares
	 * @param pares
	 * @param descriptors
	 * @throws ProcessorException
	 */
	private void writeOutputFile(List<ParDTO> pares, List<File> descriptors) throws ProcessorException
	{
		File arquivoSaida = this.createOutputFile();

		LOG.info(String.format("Gerando arquivo de saída: '%s'.", arquivoSaida.getPath()));

		// cria o cabeçalho do arquivo
		this.createHeaderFile(arquivoSaida, descriptors);

		FileOutputStream out = null;
		try
		{
			out = FileUtils.openOutputStream(arquivoSaida, true);
			final BufferedOutputStream buffer = new BufferedOutputStream(out);

			for (ParDTO par: pares)
			{
				try
				{
					StringBuilder line = new StringBuilder();
					line.append(this.getImagePairId(par));
					line.append(ImageRankConstants.FIELD_SEPARATOR);
					line.append(this.getDescriptorsLine(par, descriptors));
					line.append(ImageRankConstants.FIELD_SEPARATOR);
					line.append(par.getClasse());

					List<String> data = new ArrayList<String>();
					data.add(line.toString());
//					FileUtils.writeLines(arquivoSaida, data, ImageRankConstants.LINE_SEPARATOR, true);
					IOUtils.writeLines(data, ImageRankConstants.LINE_SEPARATOR, buffer, Charsets.UTF_8);
					buffer.flush();
				}
				catch (IOException e)
				{
					throw new ProcessorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s'.",  arquivoSaida.getAbsolutePath()), e);
				}
			}

			buffer.flush();
			out.close();
		}
		catch (IOException e)
		{
			throw new ProcessorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s'.",  arquivoSaida.getAbsolutePath()), e);
		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Monta a linha de descritores
	 * @param par
	 * @param descriptors
	 * @return
	 */
	private String getDescriptorsLine(ParDTO par, List<File> descriptors)
	{
		StringBuilder line = new StringBuilder();

		for (File descriptorFile : descriptors)
		{
			String descriptorName = descriptorFile.getName();

			if (line.length() > 0)
			{
				line.append(ImageRankConstants.FIELD_SEPARATOR);
			}
			line.append(par.getDistancias().get(descriptorName));
		}

		return line.toString();
	}

	/**
	 * Normaliza os valores dos descritores
	 * @param pares
	 * @param descriptors
	 */
	private void normalizar(List<ParDTO> pares, List<File> descriptors)
	{
		for (File descriptorFile : descriptors)
		{
			String descriptorName = descriptorFile.getName();
			List<BigDecimal> valores = new ArrayList<BigDecimal>();
			for (ParDTO par: pares)
			{
				valores.add(par.getDistancias().get(descriptorName));
			}

			BigDecimal minValue = Collections.min(valores);
			BigDecimal maxValue = Collections.max(valores);

			for (ParDTO par: pares)
			{
				BigDecimal currentValue = par.getDistancias().get(descriptorName);
				BigDecimal normalizedValue = this.normalize(currentValue, minValue, maxValue);
				par.getDistancias().put(descriptorName, normalizedValue);
			}
		}
	}

	/**
	 * Normaliza um valor considerando o máximo/mínimo existente
	 * @param currentValue
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	private BigDecimal normalize(BigDecimal currentValue, BigDecimal minValue, BigDecimal maxValue)
	{
		/*
		 * FÓRMULA PARA NORMALIZAÇÃO: (currentValue - minValue) / (maxValue - minValue)
		 */

		// (maxValue - minValue)
		BigDecimal divisor = new BigDecimal(maxValue.doubleValue(), MATH_CONTEXT);
		divisor = divisor.subtract(minValue);

		BigDecimal newValue = new BigDecimal(currentValue.doubleValue(), MATH_CONTEXT);
		newValue = newValue.subtract(minValue); // (currentValue - minValue)
		newValue = newValue.divide(divisor, MATH_CONTEXT); // (currentValue - minValue) / (maxValue - minValue)

		return newValue;
	}

	/**
	 * Obtém o id para o par de imagens
	 * @param par
	 * @return
	 */
	private String getImagePairId(ParDTO par)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(par.getImagem1().getParentFile().getName());
		sb.append(".");
		sb.append(ImageRankUtils.getNameWithoutExtension(par.getImagem1().getName()));
		sb.append(ImageRankConstants.PAIR_ID_SEPARATOR);
		sb.append(par.getImagem2().getParentFile().getName());
		sb.append(".");
		sb.append(ImageRankUtils.getNameWithoutExtension(par.getImagem2().getName()));

		return sb.toString();
	}

	/**
	 * Gera uma próxima imagem única e aleatória
	 * @param totalImagens
	 * @return
	 */
	private Integer getRandomNextUniqueImage(int totalImagens)
	{
		int imageKey = 0;
		do
		{
			imageKey = this.randomGenerator.nextInt(totalImagens-1);
		}
		while (existsImageKey(imageKey));

		// adiciona a chave no mapa para controle
		this.pares.put(imageKey, true);

		return imageKey;
	}

	/**
	 * Verifica se chave da imagem já existe
	 * @param key
	 * @return
	 */
	private boolean existsImageKey(Integer key)
	{
		return this.pares.containsKey(key);
	}

	/**
	 * Obtém as imagens
	 * @return
	 * @throws ImagesNotFoundException
	 */
	private List<File> getImagensBaseCompleta() throws ImagesNotFoundException
	{
		try
		{
			String fullPathImagem = this.getFullPathBaseCompleta(this.diretorioBaseCompleta);
			Collection<File> files = FileUtils.listFiles(new File(fullPathImagem), new String[]{ImageRankConstants.JPEG_IMAGE_EXTENSION}, true);
			return ((files != null && !files.isEmpty()) ? new LinkedList<File>(files) : null); // mantém a ordem da lista
		}
		catch (IllegalArgumentException e)
		{
			throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento.", e);
		}
	}

	/**
	 * Obtém a imagem para consulta
	 * @return
	 * @throws ImagesNotFoundException
	 */
	private File getImagemBaseConsulta() throws ImagesNotFoundException
	{
		try
		{
			String fullPathImagem = this.getFullPathBaseConsulta(this.diretorioBaseConsulta);
			Collection<File> files = FileUtils.listFiles(new File(fullPathImagem), new String[]{ImageRankConstants.JPEG_IMAGE_EXTENSION}, true);

			return ((files != null && !files.isEmpty()) ? new ArrayList<File>(files).get(0) : null);
		}
		catch (IllegalArgumentException e)
		{
			throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento.", e);
		}
	}

	/**
	 * Obtém os descritores
	 * @return
	 * @throws DescriptorsNotFoundException
	 */
	private List<File> getDescriptors(String fullPathDescriptors) throws DescriptorsNotFoundException
	{
		try
		{
			List<File> lista = new LinkedList<File>(); // mantém a ordem da lista

			File directory = new File(fullPathDescriptors);
			FileFilter fileFilter = new FileFilter()
			{
				public boolean accept(File pathname)
				{
					// só aceita diretórios
					return pathname.isDirectory();
				}
			};

			File[] diretorios = directory.listFiles((FileFilter) fileFilter);

			if (diretorios != null && diretorios.length > 0)
			{
				lista.addAll(Arrays.asList(diretorios));
			}

			return (lista.isEmpty() ? null : lista);
		}
		catch (IllegalArgumentException e)
		{
			throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.", e);
		}
	}

	/**
	 * Cria o arquivo de saída (pares) da base de consulta
	 * @return
	 * @throws ProcessorException
	 */
	private File createOutputFile() throws ProcessorException
	{
		String path = this.getFullPathBaseConsulta(this.diretorioSaida);
		File dir = new File(path);

		if (dir.exists())
		{
			try
			{
				// remove o diretório recursivamente
				FileUtils.deleteDirectory(dir);
			}
			catch (IOException e)
			{
				throw new ProcessorException(String.format("Ocorreu um erro ao excluir o diretório de saída '%s'.", path), e);
			}
		}

		// cria o diretório
		boolean dirCreated = dir.mkdir();
		if (!dirCreated)
		{
			throw new ProcessorException(String.format("Não foi possível criar o diretório de saída '%s'.", path));
		}

		String arquivo = new StringBuilder(path).append(ImageRankConstants.PAIR_OUTPUT_FILENAME).toString();
		File file = new File(arquivo);
		file.setWritable(true);
		file.setReadable(true);

		try
		{
			boolean created = file.createNewFile();
			if (!created)
			{
				throw new ProcessorException(String.format("Não foi possível criar o arquivo de saída '%s'.", arquivo));
			}
		}
		catch (IOException e)
		{
			throw new ProcessorException(String.format("Não foi possível criar o arquivo de saída '%s'.", arquivo), e);
		}

		return file;
	}

	/**
	 * Cria o cabeçalho do arquivo
	 * @param arquivoSaida
	 * @param imagens
	 * @param descriptors
	 * @throws ProcessorException
	 */
	private void createHeaderFile(File arquivoSaida, List<File> descriptors) throws ProcessorException
	{
		StringBuilder line = new StringBuilder();
		line.append("pair_id");

		for (File descriptorFile : descriptors)
		{
			line.append(ImageRankConstants.FIELD_SEPARATOR);
			line.append(descriptorFile.getName());
		}

		line.append(ImageRankConstants.FIELD_SEPARATOR);
		line.append("class");

		try
		{
			List<String> data = new ArrayList<String>();
			data.add(line.toString());
			FileUtils.writeLines(arquivoSaida, data, ImageRankConstants.LINE_SEPARATOR, true);
		}
		catch (IOException e)
		{
			throw new ProcessorException(String.format("Ocorreu um erro ao gravar o cabeçalho no arquivo de saída '%s'.", arquivoSaida.getAbsolutePath()), e);
		}
	}

	/**
	 * Obtém o path base para processamento na base completa
	 * @param dir
	 * @return
	 */
	private String getFullPathBaseCompleta(String dir)
	{
		return new StringBuilder(this.diretorioBaseCompleta).append(File.separator).append(dir).append(File.separator).toString();
	}

	/**
	 * Obtém o path base para processamento na base de consulta
	 * @param dir
	 * @return
	 */
	private String getFullPathBaseConsulta(String dir)
	{
		return new StringBuilder(this.diretorioBaseConsulta).append(File.separator).append(dir).append(File.separator).toString();
	}
}
