package asap.primitive.swing.component;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

@SuppressWarnings( "serial" )
public final class ETristateCheckBox extends JCheckBox {

    // Listener on model changes to maintain correct focusability
    private final ChangeListener enableListener = new ChangeListener( ) {

        public void stateChanged( ChangeEvent e ) {
            ETristateCheckBox.this.setFocusable( getModel( ).isEnabled( ) );
        }
    };

    public ETristateCheckBox( ) {
        this( null,
              null,
              ETristateState.DESELECTED );
    }

    public ETristateCheckBox( String text ) {
        this( text,
              null,
              ETristateState.DESELECTED );
    }

    public ETristateCheckBox( String text,
                              Icon icon,
                              ETristateState initial ) {
        super( text,
               icon );
        //Set default single model
        setModel( new ETristateButtonModel( initial ) );
        // override action behaviour
        super.addMouseListener( new MouseAdapter( ) {

            public void mousePressed( MouseEvent e ) {
                ETristateCheckBox.this.iterateState( );
            }
        } );
        ActionMap actions = new ActionMapUIResource( );
        actions.put( "pressed",
                     new AbstractAction( ) {

                         public void actionPerformed( ActionEvent e ) {
                             ETristateCheckBox.this.iterateState( );
                         }
                     } );
        actions.put( "released",
                     null );
        SwingUtilities.replaceUIActionMap( this,
                                           actions );
    }

    // Next two methods implement new API by delegation to model
    public void setIndeterminate( ) {
        getTristateModel( ).setIndeterminate( );
    }

    public boolean isIndeterminate( ) {
        return getTristateModel( ).isIndeterminate( );
    }

    public ETristateState getState( ) {
        return getTristateModel( ).getState( );
    }

    public void setState( ETristateState state ) {
        switch ( state ) {
            case DESELECTED:
                this.setSelected( false );
                break;
            case INDETERMINATE:
                this.setIndeterminate( );
                break;
            case SELECTED:
                this.setSelected( true );
                break;
            default:
                break;
        }
    }

    //Overrides superclass method
    public void setModel( ButtonModel newModel ) {
        super.setModel( newModel );
        //Listen for enable changes
        if ( model instanceof ETristateButtonModel )
            model.addChangeListener( enableListener );
    }

    //Empty override of superclass method
    public void addMouseListener( MouseListener l ) {
    }

    // Mostly delegates to model
    private void iterateState( ) {
        //Maybe do nothing at all?
        if ( !getModel( ).isEnabled( ) )
            return;
        grabFocus( );
        // Iterate state
        getTristateModel( ).iterateState( );
        // Fire ActionEvent
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent( );
        if ( currentEvent instanceof InputEvent ) {
            modifiers = ( (InputEvent) currentEvent ).getModifiersEx( );
        }
        else if ( currentEvent instanceof ActionEvent ) {
            modifiers = ( (ActionEvent) currentEvent ).getModifiers( );
        }
        fireActionPerformed( new ActionEvent( this,
                                              ActionEvent.ACTION_PERFORMED,
                                              getText( ),
                                              System.currentTimeMillis( ),
                                              modifiers ) );
    }

    //Convenience cast
    public ETristateButtonModel getTristateModel( ) {
        return (ETristateButtonModel) super.getModel( );
    }
}
