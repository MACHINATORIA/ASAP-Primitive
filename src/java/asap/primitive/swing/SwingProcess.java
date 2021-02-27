package asap.primitive.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import asap.primitive.lang.ArrayHelper;
import asap.primitive.swing.SwingHelper.MessageBox;

public abstract class SwingProcess {

    protected static SwingProcess singleton = null;

    protected static String[ ]    args;

    /*
     *  
     */
    protected JFrame              mainFrame;

    private WindowListener        frameListener;

    private MouseAdapter          inputDisableMouseAdapter;

    private KeyAdapter            inputDisableKeyAdapter;

    private int                   inputDisableCount;

    protected SwingProcess( ) {
        this.frameListener = new WindowAdapter( ) {

            public void windowClosed( WindowEvent e ) {
                SwingProcess.this.stopProcess( );
            }
        };
        this.inputDisableMouseAdapter = new MouseAdapter( ) {

            @Override
            public void mouseClicked( MouseEvent e ) {
                SwingProcess.this.warnUserToWait( );
            }
        };
        this.inputDisableKeyAdapter = new KeyAdapter( ) {

            @Override
            public void keyPressed( KeyEvent e ) {
                SwingProcess.this.warnUserToWait( );
            }
        };
    }

    protected final void setMainFrame( JFrame frame ) {
        if ( this.mainFrame != null ) {
            this.enableUserInput( true );
            this.mainFrame.setVisible( false );
            this.mainFrame.removeWindowListener( this.frameListener );
        }
        this.mainFrame = frame;
        if ( this.mainFrame != null ) {
            // SwingUtilities.updateComponentTreeUI( this.mainFrame );
            this.mainFrame.setLocationRelativeTo( null );
            this.mainFrame.addWindowListener( this.frameListener );
            this.mainFrame.setVisible( true );
        }
    }

    /*
     * Subclass model
     */
    protected void initializeProcess( ) {
        System.getProperties( ).put( "sun.java2d.noddraw",
                                     "true" );
        SwingHelper.setLookAndFeel( );
    }

    protected void engageProcess( ) {
    }

    protected void warmUpProcess( ) {
    }

    protected abstract void startProcess( );

    protected abstract void stopProcess( );

    protected void coolDownProcess( ) {
    }

    protected void disengageProcess( ) {
    }

    protected void closeProcess( ) {
    }

    /*
     * 
     */
    protected void warnUserToWait( ) {
        MessageBox.warningMessage( null,
                                   "Aguarde o término da\noperação em execução" );
    }

    /*
     * 
     */
    public String[ ] getArgs( ) {
        return ArrayHelper.copyOf( SwingProcess.args );
    }

    public final void enableUserInput( final boolean enable ) {
        synchronized ( this ) {
            if ( this.mainFrame != null ) {
                if ( enable ) {
                    if ( this.inputDisableCount == 0 ) {
                        return;
                    }
                    --this.inputDisableCount;
                    if ( this.inputDisableCount > 0 ) {
                        return;
                    }
                }
                else {
                    ++this.inputDisableCount;
                    if ( this.inputDisableCount > 1 ) {
                        return;
                    }
                }
                SwingHelper.runSwingTask( new Runnable( ) {

                    protected boolean   _enable    = ( SwingProcess.this.inputDisableCount == 0 );

                    protected Component _glassPane = SwingProcess.this.mainFrame.getRootPane( ).getGlassPane( );

                    @Override
                    public void run( ) {
                        if ( _enable ) {
                            this._glassPane.setCursor( Cursor.getDefaultCursor( ) );
                            this._glassPane.setVisible( false );
                            this._glassPane.removeMouseListener( SwingProcess.this.inputDisableMouseAdapter );
                            this._glassPane.removeKeyListener( SwingProcess.this.inputDisableKeyAdapter );
                        }
                        else {
                            this._glassPane.addMouseListener( SwingProcess.this.inputDisableMouseAdapter );
                            this._glassPane.addKeyListener( SwingProcess.this.inputDisableKeyAdapter );
                            this._glassPane.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
                            this._glassPane.setVisible( true );
                        }
                        SwingHelper.updateComponent( this._glassPane );
                    }
                } );
            }
        }
    }

    public void shutdown( ) {
        EventQueue.invokeLater( ( ) -> {
            try {
                this.stopProcess( );
                this.disengageProcess( );
                this.closeProcess( );
            }
            catch ( Throwable e ) {
                MessageBox.exceptionMessage( e );
            }
            if ( this.mainFrame != null ) {
                this.mainFrame.dispose( );
            }
            this.exit( );
        } );
        
    }

    public void exit( ) {
        System.exit( 0 );
    }

    /*
     * 
     */
    public static synchronized void run( final SwingProcess appObject,
                                         String... args ) {
        // Ensure singleton
        if ( SwingProcess.singleton != null ) {
            SwingProcess.singleton.shutdown( );
            SwingProcess.singleton = null;
        }
        SwingProcess.args = args;
        if ( appObject != null ) {
            try {
                //
                appObject.initializeProcess( );
                //
                appObject.engageProcess( );
                //
                appObject.warmUpProcess( );
                //
                SwingProcess.singleton = appObject;
                //
                EventQueue.invokeAndWait( new Runnable( ) {

                    public void run( ) {
                        try {
                            // 
                            SwingProcess.singleton.startProcess( );
                        }
                        catch ( Throwable e ) {
                            MessageBox.exceptionMessage( e );
                            SwingProcess.singleton.disengageProcess( );
                            SwingProcess.singleton.stopProcess( );
                            SwingProcess.singleton.closeProcess( );
                        }
                    }
                } );
            }
            catch ( Throwable e ) {
                MessageBox.exceptionMessage( e );
                appObject.shutdown( );
            }
        }
    }
}
