package asap.primitive.jni;

public class JNIPattern {

    public String getNativeName( ) {
        return this.nativeName;
    }

    public boolean isNativeAttached( ) {
        return ( this.nativePtr != 0L );
    }

    protected JNIPattern( String nativeName,
                          long nativePtr ) {
        this.nativeName = nativeName;
        this.nativePtr = nativePtr;
    }

    protected void nativeCleanUp( ) {
    }

    protected final String nativeName;

    protected final long   nativePtr;
}
