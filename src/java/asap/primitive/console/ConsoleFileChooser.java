package asap.primitive.console;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings( "serial" )
public class ConsoleFileChooser extends JFileChooser {

    public ConsoleFileChooser( String title,
                               String directoryPath ) {
        super( directoryPath );
        this.setDialogTitle( title );
        this.setDialogType( JFileChooser.OPEN_DIALOG );
        this.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
    }

    public ConsoleFileChooser( boolean saveDialog,
                               String title,
                               String filePath,
                               String fileDescription,
                               String... fileExtension ) {
        this( saveDialog,
              false,
              title,
              filePath,
              fileDescription,
              fileExtension );
    }

    public ConsoleFileChooser( boolean saveDialog,
                               boolean multiple,
                               String title,
                               String filePath,
                               String fileDescription,
                               String... fileExtension ) {
        super( );
        this.setDialogTitle( title );
        this.setMultiSelectionEnabled( multiple );
        File tmpSelectedFile = new File( ( filePath != null ) ? filePath
                                                              : "." );
        if ( tmpSelectedFile.exists( ) && tmpSelectedFile.isDirectory( ) ) {
            this.setCurrentDirectory( tmpSelectedFile );
        }
        else {
            this.setSelectedFile( tmpSelectedFile );
        }
        if ( fileExtension.length > 0 ) {
            this.setFileFilter( new FileNameExtensionFilter( fileDescription,
                                                             fileExtension ) );
        }
        this.setDialogType( saveDialog ? JFileChooser.SAVE_DIALOG
                                       : JFileChooser.OPEN_DIALOG );
    }

    public boolean showDialog( ) {
        return ( showDialog( null,
                             getApproveButtonText( ) ) == JFileChooser.APPROVE_OPTION );
    }

    @Override
    protected JDialog createDialog( Component parent )
        throws HeadlessException {
        JDialog tmpDialog = super.createDialog( parent );
        tmpDialog.setModal( true );
        tmpDialog.requestFocusInWindow( );
        return tmpDialog;
    }
}
