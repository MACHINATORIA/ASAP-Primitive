package asap.primitive.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import asap.primitive.console.ConsoleHelper;
import asap.primitive.log.LogService.LogManager;
import asap.primitive.log.LogService.Logger;

public class ProcessHelper {

    public static class JVMShutdownHook {

        private final static String SHELL_MODE_PROPERTY_NAME = "ShellMode";

        private final static String SHELL_MODE_ON_VALUE      = "Y";

        private final static String SHELL_MODE_OFF_VALUE     = "N";

        private final Logger        log;

        private Class< ? >          mainClass;

        private boolean             closeStdOut;

        private boolean             closeStdErr;

        private boolean             shutdownRequest          = false;

        private Thread              mainThread;

        protected void requestShutdown( ) {
            this.log.info( "Encerrando..." );
            this.shutdownRequest = true;
            try {
                this.mainThread.join( );
                this.log.info( "Encerrado com sucesso" );
            }
            catch ( InterruptedException e ) {
                this.log.warn( "Encerramento interrompido" );
            }
        }

        protected class HookThread extends Thread {

            public HookThread( JVMShutdownHook shutdownHook ) {
                this.shutdownHook = shutdownHook;
            }

            public void run( ) {
                this.shutdownHook.requestShutdown( );
            }

            protected JVMShutdownHook shutdownHook;
        }

        public JVMShutdownHook( Class< ? > mainClass,
                                boolean closeStdOut,
                                boolean closeStdErr ) {
            this.mainClass = mainClass;
            this.log = LogManager.getLogger( mainClass );
        }

        public JVMShutdownHook( Class< ? > mainClass,
                                boolean closeStdStreams ) {
            this( mainClass,
                  closeStdStreams,
                  closeStdStreams );
        }

        public JVMShutdownHook( Class< ? > mainClass ) {
            this( mainClass,
                  true,
                  true );
        }

        public void waitTermination( ) {
            String tmpShellMode = System.getProperty( String.format( "%s.%s",
                                                                     this.mainClass.getSimpleName( ),
                                                                     SHELL_MODE_PROPERTY_NAME ),
                                                      SHELL_MODE_OFF_VALUE );
            if ( tmpShellMode.compareToIgnoreCase( SHELL_MODE_ON_VALUE ) == 0 ) {
                ConsoleHelper.waitEnterKeyPress( "\n\n\nTecle <ENTER> ou <CTRL+C> para encerrar...\n\n\n" );
            }
            else {
                this.mainThread = Thread.currentThread( );
                Runtime.getRuntime( ).addShutdownHook( new HookThread( this ) );
                if ( this.closeStdOut && ( System.out != null ) ) {
                    System.out.close( );
                }
                if ( this.closeStdErr && ( System.err != null ) ) {
                    System.err.close( );
                }
                while ( !this.shutdownRequest ) {
                    try {
                        Thread.sleep( 5 );
                    }
                    catch ( InterruptedException e ) {
                        break;
                    }
                }
            }
        }
    }

    public static String briefExceptionMsg( String Prefix,
                                            Throwable e ) {
        String tmpString = e.getMessage( );
        if ( tmpString == null ) {
            tmpString = "";
        }
        return ( Prefix + ": " + tmpString.split( "nested exception is:" )[ 0 ] );
    }

    public static String stackTrace( Throwable e ) {
        ByteArrayOutputStream tmpByteStream = new ByteArrayOutputStream( );
        e.printStackTrace( new PrintStream( tmpByteStream ) );
        return new String( tmpByteStream.toByteArray( ) );
    }

    public static String simpleStackTrace( Throwable e ) {
        return simpleStackTrace( e,
                                 5 );
    }

    public static String simpleStackTrace( Throwable e,
                                           int exceptionDepth ) {
        return simpleStackTrace( e,
                                 exceptionDepth,
                                 3 );
    }

    public static String simpleStackTrace( Throwable e,
                                           int exceptionDepth,
                                           int causeDepth ) {
        return simpleStackTrace( e,
                                 "\n",
                                 "\n    ",
                                 exceptionDepth,
                                 causeDepth );
    }

    public static String simpleStackTrace( Throwable e,
                                           String exceptionSeparator,
                                           String stackSeparator,
                                           int exceptionDepth,
                                           int causeDepth ) {
        StringBuilder tmpResult = new StringBuilder( );
        Throwable tmpThrowable = e;
        boolean tmpFirstException = true;
        do {
            if ( tmpFirstException ) {
                tmpResult.append( "Excecao" );
            }
            else {
                tmpResult.append( exceptionSeparator );
                tmpResult.append( "Causada por" );
            }
            tmpResult.append( String.format( " %s: %s",
                                             tmpThrowable.getClass( ).getName( ),
                                             tmpThrowable.getLocalizedMessage( ).replaceAll( "((\\f|\\n|\\r)+( |\\t)*)+\\z",
                                                                                             "" ) ) );
            StackTraceElement[ ] tmpStackTrace = tmpThrowable.getStackTrace( );
            int tmpStackDepth = tmpFirstException ? exceptionDepth
                                                  : causeDepth;
            if ( tmpStackDepth != 0 ) {
                if ( ( tmpStackDepth < 0 ) || ( tmpStackDepth > tmpStackTrace.length ) ) {
                    tmpStackDepth = tmpStackTrace.length;
                }
                int tmpStackIndex = 0;
                for ( ; tmpStackIndex < tmpStackDepth; tmpStackIndex++ ) {
                    StackTraceElement tmpStackElement = tmpStackTrace[ tmpStackIndex ];
                    String tmpFileLine;
                    if ( tmpStackElement.isNativeMethod( ) ) {
                        tmpFileLine = " (nativo)";
                    }
                    else if ( tmpStackElement.getFileName( ) != null ) {
                        tmpFileLine = String.format( " (%s:%d)",
                                                     tmpStackElement.getFileName( ),
                                                     tmpStackElement.getLineNumber( ) );
                    }
                    else {
                        tmpFileLine = "";
                    }
                    tmpResult.append( String.format( "%sem %s.%s%s",
                                                     stackSeparator,
                                                     tmpStackElement.getClassName( ),
                                                     tmpStackElement.getMethodName( ),
                                                     tmpFileLine ) );
                }
                if ( tmpStackIndex < tmpStackTrace.length ) {
                    tmpResult.append( String.format( "%s... mais %d",
                                                     stackSeparator,
                                                     ( tmpStackTrace.length - tmpStackIndex ) ) );
                }
            }
            tmpThrowable = tmpThrowable.getCause( );
            tmpFirstException = false;
        }
        while ( tmpThrowable != null );
        return tmpResult.toString( );
    }

    public static Properties readVersionProperties( String artifactName ) {
        Properties tmpProperties = new Properties( );
        try {
            ClassLoader tmpClassLoader = Thread.currentThread( ).getContextClassLoader( );
            String tmpPropertiesPath = String.format( "%s-version.properties",
                                                      artifactName );
            InputStream tmpInputStream = tmpClassLoader.getResourceAsStream( tmpPropertiesPath );
            if ( tmpInputStream != null ) {
                try {
                    tmpProperties.load( tmpInputStream );
                }
                finally {
                    tmpInputStream.close( );
                }
            }
        }
        catch ( IOException e ) {
        }
        return tmpProperties;
    }

    public static String readArtifactVersion( String artifactName ) {
        Properties tmpProperties = readVersionProperties( artifactName );
        String tmpVersionFormat = String.format( "%s-version-%%s-number",
                                                 artifactName );
        return String.format( "%s.%s.%s.%s",
                              tmpProperties.getProperty( String.format( tmpVersionFormat,
                                                                        "major" ),
                                                         "1" ),
                              tmpProperties.getProperty( String.format( tmpVersionFormat,
                                                                        "minor" ),
                                                         "0" ),
                              tmpProperties.getProperty( String.format( tmpVersionFormat,
                                                                        "release" ),
                                                         "0" ),
                              tmpProperties.getProperty( String.format( tmpVersionFormat,
                                                                        "build" ),
                                                         "1" ) );
    }

    public static String readArtifactBuildTime( String artifactName ) {
        Properties tmpProperties = readVersionProperties( artifactName );
        return tmpProperties.getProperty( String.format( "%s-build-time",
                                                         artifactName ),
                                          "2000-01-01 12:00:00" );
    }

    public static String readArtifactBuildDate( String artifactName ) {
        String tmpBuildDate = readArtifactBuildTime( artifactName );
        if ( tmpBuildDate.length( ) >= 10 ) {
            tmpBuildDate = tmpBuildDate.substring( 0,
                                                   10 );
        }
        return tmpBuildDate;
    }
}
