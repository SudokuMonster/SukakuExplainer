package diuf.sudoku.tools;

/**
 * A typed reference to an arbitrary object.
 */
public class StrongReference<T> {

    private T value;


    public StrongReference() {  
    }

    public StrongReference(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isValueSet() {
        return this.value != null;
    }

}
