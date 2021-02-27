package asap.primitive.exception;

@SuppressWarnings( "serial" )
public class ExceptionTemplate extends Exception {

    public ExceptionTemplate( Throwable cause ) {
        super( null,
               cause );
    }

    public ExceptionTemplate( String messageFormat,
                              Object... messageArgs ) {
        super( formatMessage( messageFormat,
                              messageArgs ),
               null );
    }

    public ExceptionTemplate( Throwable cause,
                              String messageFormat,
                              Object... messageArgs ) {
        super( formatMessage( messageFormat,
                              messageArgs ),
               cause );
    }

    private static String formatMessage( String messageFormat,
                                         Object... messageArgs ) {
        try {
            return String.format( messageFormat,
                                  messageArgs );
        }
        catch ( Throwable e ) {
            return String.format( "Message format error: %s",
                                  e.getLocalizedMessage( ) );
        }
    }

    @Override
    public String getLocalizedMessage( ) {
        String tmpResult = super.getLocalizedMessage( );
        if ( tmpResult == null ) {
            Throwable tmpCause = super.getCause( );
            if ( tmpCause == null ) {
                tmpResult = "<null>";
            }
            else {
                tmpResult = tmpCause.getLocalizedMessage( );
            }
        }
        return tmpResult;
    }

    @Override
    public String getMessage( ) {
        String tmpResult = super.getMessage( );
        if ( tmpResult == null ) {
            Throwable tmpCause = super.getCause( );
            if ( tmpCause == null ) {
                tmpResult = "<null>";
            }
            else {
                tmpResult = tmpCause.getMessage( );
            }
        }
        return tmpResult;
    }
}
