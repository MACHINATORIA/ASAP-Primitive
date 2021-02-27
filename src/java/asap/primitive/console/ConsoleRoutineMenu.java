package asap.primitive.console;

import asap.primitive.string.IndentedStringBuilder;

public class ConsoleRoutineMenu {

    public static abstract class RoutineMenuItem {

        public final String menuText;

        protected RoutineMenuItem( String menuText ) {
            this.menuText = menuText;
        }

        public abstract void run( )
            throws Exception;
    }

    protected String                                caption;

    protected String                                exitOptionText;

    protected ConsoleRoutineMenu.RoutineMenuItem[ ] menuItens;

    public ConsoleRoutineMenu( String caption,
                               String exitOptionText,
                               ConsoleRoutineMenu.RoutineMenuItem[ ] menuItens ) {
        this.caption = caption;
        this.exitOptionText = exitOptionText;
        this.menuItens = menuItens;
    }

    public ConsoleRoutineMenu( String caption,
                               ConsoleRoutineMenu.RoutineMenuItem[ ] menuItens ) {
        this.caption = caption;
        this.exitOptionText = null;
        this.menuItens = menuItens;
    }

    public void run( ) {
        IndentedStringBuilder tmpMenu = new IndentedStringBuilder( );
        tmpMenu.increaseIndent( );
        tmpMenu.append( this.caption );
        tmpMenu.append( "" );
        tmpMenu.increaseIndent( );
        for ( int tmpItemNum = 0; tmpItemNum < menuItens.length; tmpItemNum++ ) {
            tmpMenu.append( "%d - %s",
                            ( tmpItemNum + 1 ),
                            menuItens[ tmpItemNum ].menuText );
        }
        tmpMenu.append( "%d - %s",
                        ( menuItens.length + 1 ),
                        ( this.exitOptionText != null ) ? this.exitOptionText
                                                        : "Sair do menu" );
        tmpMenu.decreaseIndent( );
        tmpMenu.append( "" );
        tmpMenu.append( "Digite o numero da opcao e tecle <ENTER>" );
        while ( true ) {
            ConsoleHelper.println( "\n%s",
                                   tmpMenu.getResult( ) );
            int tmpSelectedOption = ConsoleHelper.readNumber( );
            if ( ( tmpSelectedOption > 0 ) && ( tmpSelectedOption <= menuItens.length ) ) {
                try {
                    menuItens[ tmpSelectedOption - 1 ].run( );
                }
                catch ( Exception e ) {
                    ConsoleHelper.println( "%s: %s",
                                           e.getClass( ).getSimpleName( ),
                                           e.getLocalizedMessage( ) );
                }
            }
            else if ( tmpSelectedOption == ( menuItens.length + 1 ) ) {
                break;
            }
            else {
                ConsoleHelper.println( "Opcao invalida!" );
            }
        }
    }
}
