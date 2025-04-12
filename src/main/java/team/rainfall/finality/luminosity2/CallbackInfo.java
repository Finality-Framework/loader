package team.rainfall.finality.luminosity2;

@SuppressWarnings("unused")
public class CallbackInfo {
    public Object returnValue;
    public boolean isCancelled = false;
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
