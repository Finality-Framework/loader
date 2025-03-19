package team.rainfall.finality;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * The AlternativeOutputStream class extends PrintStream to provide additional functionality.
 * It allows for bypassing the custom logging behavior and provides access to the parent OutputStream.
 * When not bypassing, it logs the output with a custom prefix.
 *
 * @see PrintStream
 * @see OutputStream
 * @author RedreamR
 */
public class AlternativeOutputStream extends PrintStream {

    public boolean bypassing = false;
    private final OutputStream parentOS;

    /**
     * Constructs an AlternativeOutputStream with the specified OutputStream.
     *
     * @param out the parent OutputStream
     * @author RedreamR
     */
    public AlternativeOutputStream(OutputStream out) {
        super(out);
        parentOS = out;
    }

    /**
     * Prints a string and logs it with a custom prefix if not bypassing.
     *
     * @param s the string to be printed
     * @author RedreamR
     */
    public void print(String s){
        super.print(s);
        if(!bypassing) {
            FinalityLogger.output("[Game] " + s);
        }
    }

    /**
     * Returns the parent OutputStream.
     *
     * @return the parent OutputStream
     * @author RedreamR
     */
    public OutputStream getParentOS() {
        return parentOS;
    }

}
