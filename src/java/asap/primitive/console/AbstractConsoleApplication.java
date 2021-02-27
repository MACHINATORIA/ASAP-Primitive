package asap.primitive.console;

import asap.primitive.log.LogService.LogDetail;
import asap.primitive.log.LogService.LogLevel;
import asap.primitive.log.LogService.LogManager;
import asap.primitive.log.LogService.Logger;

public abstract class AbstractConsoleApplication {

    protected static Logger log = LogManager.getLogger( AbstractConsoleApplication.class );

    private static String   currentHeaderText;

    private static String   chainHeaderJuntion;
    //
    static {
        AbstractConsoleApplication.currentHeaderText = "";
        AbstractConsoleApplication.chainHeaderJuntion = "  <--  ";
    }

    protected abstract void _entry_point( String[ ] args )
        throws Throwable;

    protected static void _headerReset( String chainText ) {
        AbstractConsoleApplication.currentHeaderText = ( ( chainText == null ) ? ""
                                                                               : chainText );
    }

    protected static String _headerAppend( String text ) {
        StringBuilder tmpString = new StringBuilder( );
        tmpString.append( text );
        tmpString.append( AbstractConsoleApplication.chainHeaderJuntion );
        tmpString.append( AbstractConsoleApplication.currentHeaderText );
        AbstractConsoleApplication.currentHeaderText = tmpString.toString( );
        return ConsoleHelper.println( AbstractConsoleApplication.currentHeaderText );
    }

    protected static String _println( String format,
                                      Object... args ) {
        return ConsoleHelper.println( format,
                                      args );
    }

    protected static String _println( int indentation,
                                      String format,
                                      Object... args ) {
        return ConsoleHelper.println( indentation,
                                      format,
                                      args );
    }

    protected void exit( ) {
        System.exit( 0 );
    }

    public static < T extends AbstractConsoleApplication > void execute( boolean exit,
                                                                         boolean cleanlyLog,
                                                                         Class< T > mainClass,
                                                                         String[ ] args ) {
        try {
            //System.setErr( null );
            System.getProperties( ).put( "sun.java2d.noddraw",
                                         "true" );
            //
            LogManager.setMaxLevel( LogLevel.Info );
            if ( cleanlyLog ) {
                LogManager.setDetails( );
            }
            else {
                LogManager.setDetails( LogDetail.TimeStamp,
                                       LogDetail.LevelTag,
                                       LogDetail.SimpleClassName,
                                       LogDetail.MethodName,
                                       LogDetail.LineNumber );
            }
            log = LogManager.getLogger( mainClass );
            mainClass.getDeclaredConstructor( ).newInstance( )._entry_point( args );
        }
        catch ( Throwable e ) {
            log.exception( e );
        }
        if ( exit ) {
            System.exit( 0 );
        }
    }

    public static < T extends AbstractConsoleApplication > void execute( boolean cleanlyLog,
                                                                         Class< T > mainClass,
                                                                         String[ ] args ) {
        AbstractConsoleApplication.execute( true,
                                            cleanlyLog,
                                            mainClass,
                                            args );
    }

    public static < T extends AbstractConsoleApplication > void execute( Class< T > mainClass,
                                                                         String[ ] args ) {
        AbstractConsoleApplication.execute( true,
                                            true,
                                            mainClass,
                                            args );
    }
}
