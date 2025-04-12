package team.rainfall.luminosity;
@Deprecated
public class CallbackInfo<T> {
    public T self;
    public Object retValue;
    public boolean isRetVoid = true;
    public boolean isCancelled = false;

    /** Let the targetMethod return.<br>
     *  Only works when the targetMethod return void.
     *  @author RedreamR
     */
    public void cancel(){
        if(isRetVoid) {
            this.isCancelled = true;
        }else {
            throw new RuntimeException("Cannot cancel callback without a return value");
        }
    }
    /** Let the targetMethod return with a value.<br>
     *  Only works when the targetMethod return non-void.
     * @param ret The return value. Must be the same type as the targetMethod return type.
     *  @author RedreamR
     */
    public void cancelWithRetValue(Object ret){
        if(!isRetVoid) {
            this.retValue = ret;
            this.isCancelled = true;
        }else {
            throw new RuntimeException("Cannot cancel callback with return value when desc retValue is Void");
        }
    }
    public CallbackInfo(){
        this.self = null;
    }
    public CallbackInfo(T self){
        this.self = self;
    }
}
