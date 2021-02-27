package asap.primitive.bits.bitStore;

import asap.primitive.exception.ExceptionTemplate;

@SuppressWarnings( "serial" )
public class BitStoreException extends ExceptionTemplate {

    public BitStoreException( Throwable cause ) {
        super( cause );
    }

    public BitStoreException( String messageFormat,
                              Object... messageArgs ) {
        super( messageFormat,
               messageArgs );
    }

    public BitStoreException( Throwable cause,
                              String messageFormat,
                              Object... messageArgs ) {
        super( cause,
               messageFormat,
               messageArgs );
    }
}
