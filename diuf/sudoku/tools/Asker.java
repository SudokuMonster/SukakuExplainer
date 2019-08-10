package diuf.sudoku.tools;

/**
 * Interface for a gui component that can ask a yes/no question
 * to the user (e.g. in the form of a modal dialog).
 */
public interface Asker {

    /**
     * Ask a question to the user and wait for the answer.
     * @param question the question to display
     * @return whether the "yes" option was selected by the user
     */
    public boolean ask(String question);

}
