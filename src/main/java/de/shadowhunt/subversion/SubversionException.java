package de.shadowhunt.subversion;

/**
 * {@link SubversionException} is the superclass of those exceptions that can be thrown in the subversion module
 */
public class SubversionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new SubversionException exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param   message   the detail message. The detail message is saved for
	 *          later retrieval by the {@link #getMessage()} method.
	 */
	public SubversionException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new SubversionException with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this runtime exception's detail message.
	 *
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public SubversionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}