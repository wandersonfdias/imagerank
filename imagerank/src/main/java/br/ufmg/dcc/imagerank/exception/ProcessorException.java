package br.ufmg.dcc.imagerank.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessorException extends Exception
{
	private static final long serialVersionUID = -6224864304063710016L;

	/**
	 *
	 */
	public ProcessorException()
	{
	}

	/**
	 * @param message
	 */
	public ProcessorException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ProcessorException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessorException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
