package asap.primitive.jni;

import asap.primitive.console.ConsoleHelper;
import asap.primitive.log.LogService.Logger;

public class JNILoader {

    public final boolean loaded;

    public JNILoader( String libraryName ) {
        this( null,
              libraryName );
    }

    public JNILoader( Logger log,
                      String libraryName ) {
        //
        try {
            if ( ( log != null ) && log.isDebugEnabled( ) ) {
                log.debug( "Procurando biblioteca \"%s.dll\"...",
                           libraryName );
            }
            ConsoleHelper.setDummySystemStreams( );
            try {
                System.loadLibrary( libraryName );
            }
            finally {
                ConsoleHelper.restoreOriginalSystemStreams( );
            }
        }
        catch ( Throwable e1 ) {
            String tmpPlatformName = String.format( "%s-%s",
                                                    libraryName,
                                                    ( ( System.getProperty( "os.arch" ).indexOf( "64" ) < 0 ) ? "32"
                                                                                                              : "64" ) );
            try {
                if ( ( log != null ) && log.isDebugEnabled( ) ) {
                    log.debug( "Procurando biblioteca \"%s.dll\"...",
                               tmpPlatformName );
                }
                ConsoleHelper.setDummySystemStreams( );
                try {
                    System.loadLibrary( tmpPlatformName );
                }
                finally {
                    ConsoleHelper.restoreOriginalSystemStreams( );
                }
            }
            catch ( Throwable e2 ) {
                this.loaded = false;
                String tmpErrorMessage = String.format( "Falha ao carregar biblioteca nativa \"%s\" ou suas dependencias\n%s",
                                                        libraryName,
                                                        e2.getLocalizedMessage( ) );
                if ( log != null ) {
                    log.error( tmpErrorMessage );
                    if ( log.isDebugEnabled( ) ) {
                        log.debug( "java.library.path = %s",
                                   System.getProperty( "java.library.path" ) );
                    }
                }
                else {
                    System.out.println( tmpErrorMessage );
                    System.out.println( String.format( "java.library.path = %s",
                                                       System.getProperty( "java.library.path" ) ) );
                    System.out.flush( );
                }
                throw new Error( tmpErrorMessage );
            }
        }
        this.loaded = true;
    }
}
