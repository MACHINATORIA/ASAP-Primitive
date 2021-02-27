package asap.primitive.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import asap.primitive.string.IndentedStringBuilder;
import asap.primitive.string.StringHelper;

public class ConsoleHelper {

    protected static int         dummy_stream_count = 0;

    protected static InputStream originalSystemIn   = System.in;

    protected static PrintStream originalSystemOut  = System.out;

    protected static PrintStream originalSystemErr  = System.err;

    protected static InputStream dummySystemIn      = new PipedInputStream( );

    protected static PrintStream dummySystemOut     = new PrintStream( new PipedOutputStream( ) );

    protected static PrintStream dummySystemErr     = new PrintStream( new PipedOutputStream( ) );

    public static void setDummySystemStreams( ) {
        synchronized ( ConsoleHelper.class ) {
            if ( dummy_stream_count == 0 ) {
                System.setIn( ConsoleHelper.dummySystemIn );
                System.setOut( ConsoleHelper.dummySystemOut );
                System.setErr( ConsoleHelper.dummySystemErr );
            }
            dummy_stream_count += 1;
        }
    }

    public static void restoreOriginalSystemStreams( ) {
        synchronized ( ConsoleHelper.class ) {
            if ( dummy_stream_count > 0 ) {
                dummy_stream_count -= 1;
            }
            if ( dummy_stream_count == 0 ) {
                System.setIn( ConsoleHelper.originalSystemIn );
                System.setOut( ConsoleHelper.originalSystemOut );
                System.setErr( ConsoleHelper.originalSystemErr );
            }
        }
    }

    public static String println( String format,
                                  Object... args ) {
        String tmpResult = StringHelper.flawlessFormat( format,
                                                        args );
        System.out.println( tmpResult );
        System.out.flush( );
        return tmpResult;
    }

    public static String println( int indentation,
                                  String format,
                                  Object... args ) {
        String tmpResult = new IndentedStringBuilder( indentation ).append( format,
                                                                            args ).getResult( );
        System.out.println( tmpResult );
        System.out.flush( );
        return tmpResult;
    }

    public static boolean wasEnterKeyPressed( ) {
        boolean tmpResult = false;
        try {
            if ( System.in.available( ) != 0 ) {
                System.in.read( );
                tmpResult = true;
            }
            else {
                try {
                    Thread.sleep( 20 );
                }
                catch ( InterruptedException e ) {
                }
            }
        }
        catch ( IOException e ) {
        }
        return tmpResult;
    }

    public static void waitEnterKeyPress( String Message ) {
        if ( Message != null ) {
            System.out.println( Message );
            System.out.flush( );
        }
        try {
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
            while ( System.in.available( ) == 0 ) {
                try {
                    Thread.sleep( 20 );
                }
                catch ( InterruptedException e ) {
                }
            }
            System.in.read( );
        }
        catch ( IOException e ) {
        }
    }

    public static boolean readConfirmation( String message ) {
        boolean tmpResult = false;
        try {
            IndentedStringBuilder tmpMenu = new IndentedStringBuilder( );
            tmpMenu.append( "" );
            tmpMenu.increaseIndent( );
            tmpMenu.append( message );
            tmpMenu.append( "Digite 'S' ou 'N' e tecle <ENTER>" );
            while ( true ) {
                println( tmpMenu.getResult( ) );
                while ( System.in.available( ) > 0 ) {
                    System.in.read( );
                }
                while ( System.in.available( ) == 0 ) {
                    Thread.yield( );
                }
                int tmpKeyboardInput = System.in.read( );
                if ( ( tmpKeyboardInput == 'S' ) || ( tmpKeyboardInput == 's' ) ) {
                    tmpResult = true;
                    break;
                }
                else if ( ( tmpKeyboardInput == 'N' ) || ( tmpKeyboardInput == 'n' ) ) {
                    break;
                }
                println( "Resposta invalida!" );
                while ( System.in.available( ) > 0 ) {
                    System.in.read( );
                }
            }
        }
        catch ( IOException e ) {
        }
        return tmpResult;
    }

    public static int selectOptionList( String caption,
                                        String cancelOption,
                                        String[ ] optionsList ) {
        int tmpResult = ( -1 );
        IndentedStringBuilder tmpOptionsList = new IndentedStringBuilder( );
        tmpOptionsList.increaseIndent( );
        tmpOptionsList.append( caption );
        tmpOptionsList.append( "" );
        tmpOptionsList.increaseIndent( );
        for ( int tmpItemNum = 0; tmpItemNum < optionsList.length; tmpItemNum++ ) {
            tmpOptionsList.append( "%d - %s",
                                   ( tmpItemNum + 1 ),
                                   optionsList[ tmpItemNum ] );
        }
        tmpOptionsList.append( "%d - %s",
                               ( optionsList.length + 1 ),
                               cancelOption );
        tmpOptionsList.decreaseIndent( );
        tmpOptionsList.append( "" );
        tmpOptionsList.append( "Digite o numero da opcao e tecle <ENTER>" );
        while ( true ) {
            ConsoleHelper.println( "\n%s",
                                   tmpOptionsList.getResult( ) );
            int tmpSelectedOption = ConsoleHelper.readNumber( );
            if ( ( tmpSelectedOption > 0 ) && ( tmpSelectedOption <= optionsList.length ) ) {
                tmpResult = ( tmpSelectedOption - 1 );
                break;
            }
            else if ( tmpSelectedOption == ( optionsList.length + 1 ) ) {
                break;
            }
            else {
                ConsoleHelper.println( "Opcao invalida!" );
            }
        }
        return tmpResult;
    }

    public static String readText( ) {
        StringBuilder tmpResult = new StringBuilder( );
        try {
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
            while ( System.in.available( ) == 0 ) {
                try {
                    Thread.sleep( 1 );
                }
                catch ( InterruptedException e ) {
                }
            }
            while ( System.in.available( ) > 0 ) {
                int tmpKeyboardInput = System.in.read( );
                if ( ( tmpKeyboardInput == '\n' ) || ( tmpKeyboardInput == '\r' ) ) {
                    break;
                }
                tmpResult.append( (char) tmpKeyboardInput );
            }
        }
        catch ( IOException e ) {
        }
        return tmpResult.toString( );
    }

    public static int readNumber( ) {
        int tmpReadedNumber = ( -1 );
        try {
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
            while ( System.in.available( ) == 0 ) {
                try {
                    Thread.sleep( 1 );
                }
                catch ( InterruptedException e ) {
                }
            }
            String tmpTypedNumber = "";
            while ( System.in.available( ) > 0 ) {
                int tmpKeyboardInput = System.in.read( );
                if ( ( tmpKeyboardInput >= '0' ) && ( tmpKeyboardInput <= '9' ) ) {
                    tmpTypedNumber += String.valueOf( (char) tmpKeyboardInput );
                }
                else if ( ( tmpKeyboardInput == '\n' ) || ( tmpKeyboardInput == '\r' ) ) {
                    if ( tmpTypedNumber.length( ) > 0 ) {
                        try {
                            tmpReadedNumber = Integer.parseInt( tmpTypedNumber );
                        }
                        catch ( NumberFormatException e ) {
                        }
                    }
                    break;
                }
                else {
                    tmpTypedNumber = "";
                }
            }
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
        }
        catch ( IOException e ) {
        }
        return tmpReadedNumber;
    }

    public static int readPositiveNumberInRange( String caption,
                                                 int minimum,
                                                 int maximum ) {
        int tmpResult = ( -1 );
        println( caption );
        while ( true ) {
            println( "Digite um número entre %d e %d (inclusive)",
                     minimum,
                     maximum );
            int tmpNumber = readNumber( );
            if ( tmpNumber < 0 ) {
                break;
            }
            else if ( ( tmpNumber >= minimum ) && ( tmpNumber <= maximum ) ) {
                tmpResult = tmpNumber;
                break;
            }
            else {
                println( "Número inválido" );
            }
        }
        return tmpResult;
    }

    public static int[ ] readNumberArray( ) {
        int[ ] tmpResult = new int[ 0 ];
        try {
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
            while ( System.in.available( ) == 0 ) {
                Thread.yield( );
            }
            String tmpTypedNumberArray = "";
            while ( System.in.available( ) > 0 ) {
                int tmpKeyboardInput = System.in.read( );
                if ( ( ( tmpKeyboardInput >= '0' ) && ( tmpKeyboardInput <= '9' ) )
                     || ( tmpKeyboardInput == ',' ) //
                     || ( tmpKeyboardInput == ' ' ) ) {
                    tmpTypedNumberArray += String.valueOf( (char) tmpKeyboardInput );
                }
                else {
                    try {
                        String[ ] tmpNumberArray = tmpTypedNumberArray.split( "[, ]+" );
                        tmpResult = new int[ tmpNumberArray.length ];
                        for ( int tmpNumberIndex = 0; tmpNumberIndex < tmpResult.length; tmpNumberIndex++ ) {
                            tmpResult[ tmpNumberIndex ] = Integer.parseInt( tmpNumberArray[ tmpNumberIndex ] );
                        }
                    }
                    catch ( NumberFormatException e ) {
                        tmpResult = new int[ 0 ];
                    }
                    break;
                }
            }
            while ( System.in.available( ) > 0 ) {
                System.in.read( );
            }
        }
        catch ( IOException e ) {
        }
        return tmpResult;
    }

    public static abstract class ProgressMonitor {

        protected long count;

        protected long startingTime;

        protected long completed;

        protected long nextCompleted;

        protected int  defaultNotificationSteps;

        protected int  notificationSteps;

        protected ProgressMonitor( int notificationSteps ) {
            this.count = 0;
            this.startingTime = 0;
            this.completed = ( -1 );
            this.nextCompleted = ( -1 );
            this.defaultNotificationSteps = notificationSteps;
        }

        public void start( long count,
                           int notificationSteps ) {
            this.count = count;
            this.startingTime = System.currentTimeMillis( );
            this.completed = 0;
            this.nextCompleted = 0;
            this.notificationSteps = notificationSteps;
        }

        public void start( long count ) {
            this.start( count,
                        this.defaultNotificationSteps );
        }

        public void step( long steps ) {
            if ( this.completed < 0 ) {
                throw new IllegalStateException( "Invalid task progress phase" );
            }
            else {
                this.completed += steps;
                if ( ( this.completed >= this.nextCompleted ) && ( this.completed <= this.count ) ) {
                    long tmpPercent = ( ( this.completed * 100 ) / this.count );
                    long tmpNextPercent = ( tmpPercent + notificationSteps );
                    this.nextCompleted = ( ( ( this.count * ( tmpNextPercent <= 100 ? tmpNextPercent
                                                                                    : 100 ) )
                                             + 99 )
                                           / 100 );
                    long tmpElapsedTime = ( System.currentTimeMillis( ) - this.startingTime );
                    long tmpEstimatedTime = ( ( this.count * tmpElapsedTime ) / this.completed );
                    this.progress( this.count,
                                   this.completed,
                                   tmpPercent,
                                   tmpElapsedTime,
                                   tmpEstimatedTime );
                }
            }
        }

        protected abstract void progress( long count,
                                          long completed,
                                          long percent,
                                          long elapsedTime,
                                          long estimatedTime );
    }
}
