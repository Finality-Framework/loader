package team.rainfall.finality;

import java.io.OutputStream;
import java.io.PrintStream;

public class AlternativeOutputStream extends PrintStream {
    public boolean bypassing = false;
    private OutputStream parentOS;
    public AlternativeOutputStream(OutputStream out) {
        super(out);
        parentOS = out;
    }
    public void print(String s){
        super.print(s);
        if(!bypassing) {
            FinalityLogger.output("[Game] " + s);
        }
    }
}
