package asap.primitive.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.RepaintManager;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import asap.primitive.console.ConsoleHelper;
import asap.primitive.exception.ExceptionTemplate;
import asap.primitive.pattern.ValuePattern.BasicValue;
import asap.primitive.pattern.ValuePattern.ThreadValue;
import asap.primitive.pattern.ValuePattern.Value;
import asap.primitive.process.ProcessHelper;
import asap.primitive.string.HtmlHelper;
import asap.primitive.string.HtmlHelper.HtmlColor;
import asap.primitive.string.HtmlHelper.HtmlHorizontalAlignment;
import asap.primitive.string.HtmlHelper.HtmlVerticalAlignment;
import asap.primitive.string.StringHelper;
import asap.primitive.thread.ThreadHelper;

public final class SwingHelper {

    protected static boolean         ENABLE_CONCURRENT_AWT_QUEUE = true;

    protected static boolean         SET_METAL_LOOK_AND_FEEL     = true;

    protected static String          CHANGE_FONT_FAMILY          = null;                                             //"Tahoma";

    protected static Integer         CHANGE_FONT_STYLE           = null;                                             //Font.PLAIN;

    protected static Integer         CHANGE_FONT_SIZE            = null;                                             //1;

    protected static boolean         CHANGE_BUTTON_LOOK          = false;

    protected static ExecutorService executorService;
    /*
     * 
     */
    static {
        SwingHelper.executorService = Executors.newSingleThreadExecutor( );
    }

    public static void runSwingTask( Runnable task ) {
        if ( !ENABLE_CONCURRENT_AWT_QUEUE || EventQueue.isDispatchThread( ) ) {
            task.run( );
        }
        else {
            try {
                EventQueue.invokeAndWait( task );
            }
            catch ( Throwable e ) {
                ConsoleHelper.println( ProcessHelper.stackTrace( e ) );
            }
        }
    }

    public static < T > T runSwingTask( final Callable< T > task )
        throws Exception {
        final Value< T > tmpResult = new BasicValue< T >( );
        final Value< Throwable > tmpException = new BasicValue< Throwable >( );
        SwingHelper.runSwingTask( new Runnable( ) {

            @Override
            public void run( ) {
                try {
                    tmpResult.set( task.call( ) );
                }
                catch ( Throwable e ) {
                    tmpException.set( e );
                }
            }
        } );
        if ( tmpException.get( ) != null ) {
            throw new Exception( tmpException.get( ) );
        }
        return tmpResult.get( );
    }

    public static void setLookAndFeel( ) {
        if ( SET_METAL_LOOK_AND_FEEL ) {
            try {
                UIManager.setLookAndFeel( "javax.swing.plaf.metal.MetalLookAndFeel" );
                if ( ( CHANGE_FONT_FAMILY != null ) || ( CHANGE_FONT_STYLE != null ) || ( CHANGE_FONT_SIZE != null ) ) {
                    Enumeration< Object > tmpUiKeys = UIManager.getDefaults( ).keys( );
                    while ( tmpUiKeys.hasMoreElements( ) ) {
                        Object tmpUiKey = tmpUiKeys.nextElement( );
                        Object tmpUiValue = UIManager.get( tmpUiKey );
                        if ( ( tmpUiValue != null ) && ( tmpUiValue instanceof javax.swing.plaf.FontUIResource ) ) {
                            FontUIResource tmpUiFont = (FontUIResource) tmpUiValue;
                            FontUIResource tmpTweakFont = new FontUIResource( ( CHANGE_FONT_FAMILY != null ) ? CHANGE_FONT_FAMILY
                                                                                                             : tmpUiFont.getFamily( ),
                                                                              ( CHANGE_FONT_STYLE != null ) ? CHANGE_FONT_STYLE
                                                                                                            : tmpUiFont.getStyle( ),
                                                                              ( CHANGE_FONT_SIZE != null ) ? ( tmpUiFont.getSize( )
                                                                                                               + CHANGE_FONT_SIZE )
                                                                                                           : tmpUiFont.getSize( ) );
                            UIManager.put( tmpUiKey,
                                           tmpTweakFont );
                        }
                    }
                }
                if ( CHANGE_BUTTON_LOOK ) {
                    UIManager.put( "Button.background",
                                   Color.decode( "#eeeeee" ) );
                    UIManager.put( "ToggleButton.background",
                                   Color.decode( "#eeeeee" ) );
                    UIManager.put( "Button.border",
                                   new CompoundBorder( new LineBorder( new Color( 200,
                                                                                  200,
                                                                                  200 ) ),
                                                       new EmptyBorder( 2,
                                                                        2,
                                                                        2,
                                                                        2 ) ) );
                    UIManager.put( "ToggleButton.border",
                                   new CompoundBorder( new LineBorder( new Color( 200,
                                                                                  200,
                                                                                  200 ) ),
                                                       new EmptyBorder( 2,
                                                                        2,
                                                                        2,
                                                                        2 ) ) );
                }
            }
            catch ( Throwable e ) {
            }
        }
    }

    public static void resizeAllFonts( Container container,
                                       int fontOffset ) {
        resizeAllFonts_recursion( container,
                                  fontOffset );
    }

    private static void resizeAllFonts_recursion( Container container,
                                                  int fontOffset ) {
        for ( Component tmpComponent : container.getComponents( ) ) {
            Font tmpOldFont = tmpComponent.getFont( );
            if ( tmpOldFont != null ) {
                Font tmpNewFont = tmpOldFont.deriveFont( tmpOldFont.getSize( ) + fontOffset );
                tmpComponent.setFont( tmpNewFont );
            }
            if ( tmpComponent instanceof Container ) {
                resizeAllFonts_recursion( (Container) tmpComponent,
                                          fontOffset );
            }
        }
    }

    public static void setComponentFontSize( Component component,
                                             int size ) {
        component.setFont( component.getFont( ).deriveFont( size ) );
    }

    public static void scheduleSwingTask( Runnable task ) {
        try {
            EventQueue.invokeLater( task );
        }
        catch ( Throwable e ) {
            ConsoleHelper.println( ProcessHelper.stackTrace( e ) );
        }
    }

    public static void scheduleNonSwingTask( Runnable task ) {
        SwingHelper.executorService.execute( task );
    }

    public static void updateComponent( Component component ) {
        RepaintManager.currentManager( component ).paintDirtyRegions( );
    }

    public static void maximizeFrame( JFrame frame ) {
        frame.setExtendedState( frame.getExtendedState( ) | JFrame.MAXIMIZED_BOTH );
    }

    public static void showTextDialog( final boolean maximized,
                                       final String title,
                                       final String caption,
                                       final String text ) {
        final Value< TextOutputFrame > tmpTextFrame = new ThreadValue< TextOutputFrame >( );
        SwingProcess.run( new SwingProcess( ) {

            @Override
            protected void startProcess( ) {
                TextOutputFrame tmpFrame = new TextOutputFrame( );
                tmpFrame.setTitle( title );
                tmpFrame.getCaptionLabel( ).setText( caption );
                tmpFrame.getOutputTextPane( ).setText( text );
                tmpFrame.getOutputTextPane( ).setCaretPosition( 0 );
                if ( maximized ) {
                    SwingHelper.maximizeFrame( tmpFrame );
                }
                tmpFrame.getButtonOk( ).addActionListener( new ActionListener( ) {

                    @Override
                    public void actionPerformed( ActionEvent arg0 ) {
                        shutdown( );
                    }
                } );
                this.setMainFrame( tmpFrame );
                tmpTextFrame.set( tmpFrame );
            }

            @Override
            protected void stopProcess( ) {
            }
        } );
        while ( ( tmpTextFrame.get( ) == null ) || !tmpTextFrame.get( ).isActive( ) ) {
            ThreadHelper.sleep( 200 );
        }
        while ( tmpTextFrame.get( ).isActive( ) ) {
            ThreadHelper.sleep( 200 );
        }
    }

    public static class FileChooser {

        public FileChooser( ) {
            this( null );
        }

        public FileChooser( String startingFolder ) {
            this.lastStartingFolder = startingFolder;
        }

        private String lastStartingFolder;

        @SuppressWarnings( "serial" )
        private class CustomFileChooser extends JFileChooser {

            protected boolean saveDialog;

            private CustomFileChooser( boolean folderSelection,
                                       boolean saveDialog,
                                       boolean allowMultiple,
                                       String title,
                                       String startingFolder,
                                       String startingFile,
                                       String fileDescription,
                                       String... fileExtension ) {
                super( );
                this.setFileSelectionMode( folderSelection ? JFileChooser.DIRECTORIES_ONLY
                                                           : JFileChooser.FILES_ONLY );
                this.saveDialog = saveDialog;
                this.setMultiSelectionEnabled( allowMultiple );
                this.setDialogTitle( title );
                String tmpStartingFolder = ( startingFolder != null ) ? startingFolder
                                                                      : ( ( FileChooser.this.lastStartingFolder != null ) ? FileChooser.this.lastStartingFolder
                                                                                                                          : System.getProperty( "user.dir" ) );
                this.setCurrentDirectory( new File( tmpStartingFolder ) );
                this.setSelectedFile( startingFile != null ? new File( startingFile )
                                                           : null );
                if ( folderSelection ) {
                    this.setAcceptAllFileFilterUsed( false );
                }
                else {
                    if ( ( fileDescription != null ) && ( fileExtension.length > 0 ) ) {
                        this.setAcceptAllFileFilterUsed( false );
                        this.setFileFilter( new FileNameExtensionFilter( fileDescription,
                                                                         fileExtension ) );
                    }
                    else {
                        this.setAcceptAllFileFilterUsed( true );
                    }
                }
            }

            protected boolean showDialog( ) {
                Component tmpParentComponent = ( SwingProcess.singleton == null ) ? null
                                                                                  : SwingProcess.singleton.mainFrame;
                if ( JFileChooser.APPROVE_OPTION == ( this.saveDialog ? this.showSaveDialog( tmpParentComponent )
                                                                      : this.showOpenDialog( tmpParentComponent ) ) ) {
                    FileChooser.this.lastStartingFolder = ( this.isDirectorySelectionEnabled( ) ? this.getSelectedFile( )
                                                                                                : this.getCurrentDirectory( ) ).getPath( );
                    return true;
                }
                return false;
            }

            @Override
            @SuppressWarnings( "unused" )
            protected JDialog createDialog( Component parent )
                throws HeadlessException {
                JDialog tmpDialog = super.createDialog( parent );
                if ( true ) {
                    tmpDialog.setLocationByPlatform( true );
                    tmpDialog.setAlwaysOnTop( true );
                    tmpDialog.setLocationRelativeTo( null );
                }
                else {
                    tmpDialog.setModal( true );
                    tmpDialog.requestFocusInWindow( );
                }
                return tmpDialog;
            }
        }

        public File selectFolder( String title,
                                  String startingFolder ) {
            return this.selectFolder( title,
                                      startingFolder,
                                      null );
        }

        public File selectFolder( String title,
                                  String startingFolder,
                                  String fileDescription,
                                  String... fileExtension ) {
            File tmpResult = null;
            CustomFileChooser tmpChooser = new CustomFileChooser( true,
                                                                  false,
                                                                  false,
                                                                  title,
                                                                  startingFolder,
                                                                  null,
                                                                  fileDescription,
                                                                  fileExtension );
            if ( tmpChooser.showDialog( ) ) {
                tmpResult = tmpChooser.getSelectedFile( );
            }
            return tmpResult;
        }

        public File[ ] selectFolders( String title,
                                      String startingFolder ) {
            File[ ] tmpResult = null;
            CustomFileChooser tmpChooser = new CustomFileChooser( true,
                                                                  false,
                                                                  true,
                                                                  title,
                                                                  startingFolder,
                                                                  null,
                                                                  null );
            if ( tmpChooser.showDialog( ) ) {
                tmpResult = tmpChooser.getSelectedFiles( );
            }
            return tmpResult;
        }

        public File selectFileToLoad( String title,
                                      String startingFolder,
                                      String startingFile,
                                      String fileDescription,
                                      String... fileExtension ) {
            File tmpResult = null;
            CustomFileChooser tmpChooser = new CustomFileChooser( false,
                                                                  false,
                                                                  false,
                                                                  title,
                                                                  startingFolder,
                                                                  startingFile,
                                                                  fileDescription,
                                                                  fileExtension );
            if ( tmpChooser.showDialog( ) ) {
                tmpResult = tmpChooser.getSelectedFile( );
            }
            return tmpResult;
        }

        public File[ ] selectFilesToLoad( String title,
                                          String startingFolder,
                                          String startingFile,
                                          String fileDescription,
                                          String... fileExtension ) {
            File[ ] tmpResult = null;
            CustomFileChooser tmpChooser = new CustomFileChooser( false,
                                                                  false,
                                                                  true,
                                                                  title,
                                                                  startingFolder,
                                                                  startingFile,
                                                                  fileDescription,
                                                                  fileExtension );
            if ( tmpChooser.showDialog( ) ) {
                tmpResult = tmpChooser.getSelectedFiles( );
            }
            return tmpResult;
        }

        public File selectFileToSave( String title,
                                      String startingFolder,
                                      String startingFile,
                                      String fileDescription,
                                      String... fileExtension ) {
            File tmpResult = null;
            CustomFileChooser tmpChooser = new CustomFileChooser( false,
                                                                  true,
                                                                  false,
                                                                  title,
                                                                  startingFolder,
                                                                  startingFile,
                                                                  fileDescription,
                                                                  fileExtension );
            if ( tmpChooser.showDialog( ) ) {
                tmpResult = tmpChooser.getSelectedFile( );
            }
            return tmpResult;
        }
    }

    public static enum MessageOption {

        Invalid,
        Closed,
        Cancel,
        No,
        Yes,
        Ok;

        protected static MessageOption fromMessage( int jOption ) {
            switch ( jOption ) {
                case JOptionPane.CLOSED_OPTION:
                    return MessageOption.Closed;
                case JOptionPane.CANCEL_OPTION:
                    return MessageOption.Cancel;
                case JOptionPane.OK_OPTION:
                    return MessageOption.Ok;
                default:
                    return MessageOption.Invalid;
            }
        }

        protected static MessageOption fromConfirmation( int jOption ) {
            switch ( jOption ) {
                case JOptionPane.CLOSED_OPTION:
                    return MessageOption.Closed;
                case JOptionPane.CANCEL_OPTION:
                    return MessageOption.Cancel;
                case JOptionPane.NO_OPTION:
                    return MessageOption.No;
                case JOptionPane.YES_OPTION:
                    return MessageOption.Yes;
                default:
                    return MessageOption.Invalid;
            }
        }
    }

    public static class MessageBox {

        protected static void messageDialog( final boolean modal,
                                             final String title,
                                             final int messageType,
                                             final String messageFormat,
                                             final Object... messageArgs ) {
            SwingHelper.runSwingTask( new Runnable( ) {

                @Override
                public void run( ) {
                    Frame[ ] tmpFrames = JFrame.getFrames( );
                    String tmpMessage = ( messageFormat == null ) ? "null"
                                                                  : StringHelper.wrap( StringHelper.flawlessFormat( messageFormat,
                                                                                                                    messageArgs ),
                                                                                       120 );
                    JOptionPane tmpOptionPane = new JOptionPane( tmpMessage,
                                                                 messageType );
                    JDialog tmpDialog = tmpOptionPane.createDialog( ( ( tmpFrames != null )
                                                                      && ( tmpFrames.length > 0 ) ) ? tmpFrames[ 0 ]
                                                                                                    : null,
                                                                    title );
                    tmpDialog.setModal( modal );
                    tmpDialog.setVisible( true );
                }
            } );
        }

        protected static String[ ] YES_NO_CANCEL_STRINGS = new String[ ] { "Sim",
                                                                           "Não",
                                                                           "Cancelar" };

        protected static MessageOption confirmationDialog( final boolean cancelable,
                                                           final String title,
                                                           final int messageType,
                                                           final String messageFormat,
                                                           final Object... messageArgs ) {
            int tmpResult = -1;
            try {
                tmpResult = SwingHelper.runSwingTask( new Callable< Integer >( ) {

                    @Override
                    public Integer call( ) {
                        Frame[ ] tmpFrames = JFrame.getFrames( );
                        String tmpMessage = ( messageFormat == null ) ? "null"
                                                                      : StringHelper.wrap( StringHelper.flawlessFormat( messageFormat,
                                                                                                                        messageArgs ),
                                                                                           120 );
                        return JOptionPane.showConfirmDialog( ( ( tmpFrames != null )
                                                                && ( tmpFrames.length > 0 ) ) ? tmpFrames[ 0 ]
                                                                                              : null,
                                                              tmpMessage,
                                                              ( title != null ) ? title
                                                                                : "Confirmação",
                                                              cancelable ? JOptionPane.YES_NO_CANCEL_OPTION
                                                                         : JOptionPane.YES_NO_OPTION,
                                                              messageType );
                    }
                } );
            }
            catch ( Throwable e ) {
                ConsoleHelper.println( ProcessHelper.stackTrace( e ) );
            }
            return MessageOption.fromConfirmation( tmpResult );
        }

        public static MessageOption questionConfirmation( boolean cancelable,
                                                          String title,
                                                          String messageFormat,
                                                          Object... messageArgs ) {
            return MessageBox.confirmationDialog( cancelable,
                                                  title,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  messageFormat,
                                                  messageArgs );
        }

        public static boolean questionConfirmation( String title,
                                                    String messageFormat,
                                                    Object... messageArgs ) {
            return ( questionConfirmation( false,
                                           title,
                                           messageFormat,
                                           messageArgs ) == MessageOption.Yes );
        }

        public static MessageOption warningConfirmation( boolean cancelable,
                                                         String title,
                                                         String messageFormat,
                                                         Object... messageArgs ) {
            return MessageBox.confirmationDialog( cancelable,
                                                  title,
                                                  JOptionPane.WARNING_MESSAGE,
                                                  messageFormat,
                                                  messageArgs );
        }

        public static boolean warningConfirmation( String title,
                                                   String messageFormat,
                                                   Object... messageArgs ) {
            return ( warningConfirmation( false,
                                          title,
                                          messageFormat,
                                          messageArgs ) == MessageOption.Yes );
        }

        public static void informationMessage( boolean modal,
                                               String title,
                                               String msgFormat,
                                               Object... messageArgs ) {
            messageDialog( modal,
                           ( title != null ) ? title
                                             : "Informação",
                           JOptionPane.INFORMATION_MESSAGE,
                           msgFormat,
                           messageArgs );
        }

        public static void informationMessage( String title,
                                               String msgFormat,
                                               Object... messageArgs ) {
            informationMessage( true,
                                title,
                                msgFormat,
                                messageArgs );
        }

        public static void warningMessage( boolean modal,
                                           String title,
                                           String msgFormat,
                                           Object... messageArgs ) {
            messageDialog( modal,
                           ( title != null ) ? title
                                             : "Alerta",
                           JOptionPane.WARNING_MESSAGE,
                           msgFormat,
                           messageArgs );
        }

        public static void warningMessage( String title,
                                           String msgFormat,
                                           Object... messageArgs ) {
            warningMessage( true,
                            title,
                            msgFormat,
                            messageArgs );
        }

        public static void errorMessage( boolean modal,
                                         String title,
                                         String msgFormat,
                                         Object... messageArgs ) {
            messageDialog( modal,
                           ( title != null ) ? title
                                             : "Erro",
                           JOptionPane.ERROR_MESSAGE,
                           msgFormat,
                           messageArgs );
        }

        public static void errorMessage( String title,
                                         String msgFormat,
                                         Object... messageArgs ) {
            errorMessage( true,
                          title,
                          msgFormat,
                          messageArgs );
        }

        public static void exceptionMessage( boolean modal,
                                             Throwable e ) {
            errorMessage( modal,
                          null,
                          StringHelper.wrapTrace( ProcessHelper.stackTrace( e ),
                                                  120 ) );
        }

        public static void exceptionMessage( Throwable e ) {
            exceptionMessage( true,
                              e );
        }
    }

    public static class HtmlConsole {

        @SuppressWarnings( "serial" )
        public static class HtmlConsoleException extends ExceptionTemplate {

            public HtmlConsoleException( Throwable cause ) {
                super( cause );
            }

            public HtmlConsoleException( String messageFormat,
                                         Object... messageArgs ) {
                super( messageFormat,
                       messageArgs );
            }

            public HtmlConsoleException( Throwable cause,
                                         String messageFormat,
                                         Object... messageArgs ) {
                super( cause,
                       messageFormat,
                       messageArgs );
            }
        }

        protected JTextPane           textPane;

        protected HTMLDocument        document;

        protected Element             rootElement;

        protected boolean             backgroundToggle;

        protected static final String CLEAN_CONTENT = "<table id=\"tbConsole\" width=100%></table>";

        public HtmlConsole( JTextPane textPane ) {
            this.textPane = textPane;
            this.textPane.setContentType( "text/html" );
            this.document = (HTMLDocument) this.textPane.getDocument( );
            this.clear( );
        }

        public void clear( ) {
            this.textPane.setText( CLEAN_CONTENT );
            this.rootElement = this.document.getElement( "tbConsole" );
        }

        protected void appendText( Boolean fixedWidth,
                                   Boolean header,
                                   Integer fontSize,
                                   HtmlColor backColor,
                                   HtmlColor foreColor,
                                   String messageFormat,
                                   Object... messageArgs )
            throws HtmlConsoleException {
            try {
                this.document.insertBeforeEnd( this.rootElement,
                                               HtmlHelper.formatLine( HtmlVerticalAlignment.Top,
                                                                      HtmlColor.White,
                                                                      HtmlHelper.formatCell( header,
                                                                                             100,
                                                                                             false,
                                                                                             backColor,
                                                                                             HtmlHorizontalAlignment.Left,
                                                                                             null,
                                                                                             null,
                                                                                             HtmlHelper.formatText( fixedWidth,
                                                                                                                    fontSize,
                                                                                                                    foreColor,
                                                                                                                    false,
                                                                                                                    StringHelper.flawlessFormat( messageFormat,
                                                                                                                                                 messageArgs ) ) ) ) );
            }
            catch ( IllegalArgumentException | BadLocationException | IOException e ) {
                throw new HtmlConsoleException( e,
                                                "Falha ao adicionar conteúdo" );
            }
        }

        public void appendTitle( String messageFormat,
                                 Object... messageArgs )
            throws HtmlConsoleException {
            this.appendText( false,
                             false,
                             7,
                             null,
                             null,
                             messageFormat,
                             messageArgs );
        }

        public void appendHeader( String messageFormat,
                                  Object... messageArgs )
            throws HtmlConsoleException {
            this.appendText( false,
                             true,
                             6,
                             null,
                             null,
                             messageFormat,
                             messageArgs );
        }

        public void appendInfo( String messageFormat,
                                Object... messageArgs )
            throws HtmlConsoleException {
            this.appendText( true,
                             false,
                             4,
                             null,
                             null,
                             messageFormat,
                             messageArgs );
        }

        public void appendWarn( String messageFormat,
                                Object... messageArgs )
            throws HtmlConsoleException {
            this.appendText( true,
                             false,
                             4,
                             null,
                             HtmlColor.Orange,
                             messageFormat,
                             messageArgs );
        }

        public void appendError( String messageFormat,
                                 Object... messageArgs )
            throws HtmlConsoleException {
            this.appendText( true,
                             false,
                             4,
                             null,
                             HtmlColor.Red,
                             messageFormat,
                             messageArgs );
        }

        public void appendTable( String[ ] columnNames,
                                 String[ ]... lineValues )
            throws HtmlConsoleException {
            StringBuilder tmpLines = new StringBuilder( );
            StringBuilder tmpColumns = new StringBuilder( );
            for ( String tmpColumnName : columnNames ) {
                tmpColumns.append( String.format( "<td align=\"center\">%s</td>",
                                                  HtmlHelper.formatText( true,
                                                                         null,
                                                                         null,
                                                                         false,
                                                                         tmpColumnName ) ) );
            }
            tmpLines.append( String.format( "<tr>%s</tr>",
                                            tmpColumns.toString( ) ) );
            for ( String[ ] tmpLineValue : lineValues ) {
                tmpColumns.setLength( 0 );
                for ( String tmpColumnValue : tmpLineValue ) {
                    tmpColumns.append( String.format( "<td align=\"left\">%s</td>",
                                                      HtmlHelper.formatText( true,
                                                                             null,
                                                                             null,
                                                                             false,
                                                                             tmpColumnValue ) ) );
                }
                tmpLines.append( String.format( "<tr>%s</tr>",
                                                tmpColumns.toString( ) ) );
            }
            try {
                this.document.insertBeforeEnd( this.rootElement,
                                               String.format( "<tr><td><table>%s</table></td></tr>",
                                                              tmpLines.toString( ) ) );
            }
            catch ( IllegalArgumentException | BadLocationException | IOException e ) {
                throw new HtmlConsoleException( e,
                                                "Falha ao formatar conteúdo" );
            }
        }
    }
}
