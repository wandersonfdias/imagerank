package br.ufmg.dcc.imagerank.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImageProcessorException extends ProcessorException
{
	private static final long serialVersionUID = -2144323208561683124L;

	/**
	 *
	 */
	public ImageProcessorException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ImageProcessorException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ImageProcessorException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ImageProcessorException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
