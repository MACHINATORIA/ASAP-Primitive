package asap.primitive.bytes.codec;

import asap.primitive.exception.ExceptionTemplate;

@SuppressWarnings( "serial" )
public class ByteCodecException extends ExceptionTemplate {

    public ByteCodecException( Throwable cause ) {
        super( cause );
    }

    public ByteCodecException( String messageFormat,
                               Object... messageArgs ) {
        super( messageFormat,
               messageArgs );
    }

    public ByteCodecException( Throwable cause,
                               String messageFormat,
                               Object... messageArgs ) {
        super( cause,
               messageFormat,
               messageArgs );
    }
}
