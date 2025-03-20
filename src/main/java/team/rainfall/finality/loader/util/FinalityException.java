package team.rainfall.finality.loader.util;

/**
 * <p>A custom exception class for handling exceptions specific to the Finality loader.
 * <p>This class extends RuntimeException and provides a constructor to set the exception message.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * throw new FinalityException("An error occurred");
 * }
 * </pre>
 *
 * <p>Note: This exception is unchecked and does not need to be declared in a method's throws clause.
 *
 * @author RedreamR
 */
public class FinalityException extends RuntimeException {
    public FinalityException(String message) {
        super(message);
    }
}
