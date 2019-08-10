package diuf.sudoku.io;

/**
 * A generic error message.
 * Consists of a message with "{0}", "{1}", ... patterns
 * and zero or more arguments. The "{0}", "{1}", ...
 * patterns are replaced by the arguments when the
 * message is converted to a string by {@link #toString()}.
 */
public class ErrorMessage {

    private final String message;
    private final Object[] args;
    private boolean fatal = false;

    public ErrorMessage(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

    public ErrorMessage(String message, boolean isFatal, Object... args) {
        this.message = message;
        this.args = args;
        this.fatal = isFatal;
    }

    public String getMessage() {
        return this.message;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public void setFatal(boolean isFatal) {
        this.fatal = isFatal;
    }

    public boolean isFatal() {
        return this.fatal;
    }

    @Override
    public String toString() {
        String result = message;
        for (int i = 0; i < args.length; i++) {
            String pattern = "{" + i + "}";
            String value = (args[i] == null ? "" : args[i].toString());
            result = result.replace(pattern, value);
        }
        return result;
    }

}
