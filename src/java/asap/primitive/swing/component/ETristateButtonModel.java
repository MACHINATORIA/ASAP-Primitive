package asap.primitive.swing.component;

import java.awt.event.ItemEvent;

import javax.swing.JToggleButton.ToggleButtonModel;

@SuppressWarnings( "serial" )
public class ETristateButtonModel extends ToggleButtonModel {

    private ETristateState state = ETristateState.DESELECTED;

    public ETristateButtonModel( ETristateState state ) {
        this.setState( state );
    }

    public ETristateButtonModel( ) {
        this( ETristateState.DESELECTED );
    }

    public void setIndeterminate( ) {
        this.setState( ETristateState.INDETERMINATE );
    }

    public boolean isIndeterminate( ) {
        return state == ETristateState.INDETERMINATE;
    }

    @Override
    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        this.displayState( );
    }

    @Override
    public void setSelected( boolean selected ) {
        this.setState( selected ? ETristateState.SELECTED
                                : ETristateState.DESELECTED );
    }

    @Override
    public void setArmed( boolean b ) {
    }

    @Override
    public void setPressed( boolean b ) {
    }

    protected void iterateState( ) {
        this.setState( state.next( ) );
    }

    protected void setState( ETristateState state ) {
        this.state = state;
        this.displayState( );
        if ( state == ETristateState.INDETERMINATE && isEnabled( ) ) {
            this.fireStateChanged( );
            int ItemEvent_INDETERMINATE = ( ItemEvent.DESELECTED + 1 );
            this.fireItemStateChanged( new ItemEvent( this,
                                                      ItemEvent.ITEM_STATE_CHANGED,
                                                      this,
                                                      ItemEvent_INDETERMINATE ) );
        }
    }

    protected void displayState( ) {
        super.setSelected( this.state != ETristateState.DESELECTED );
        super.setArmed( this.state == ETristateState.INDETERMINATE );
        super.setPressed( this.state == ETristateState.INDETERMINATE );
    }

    public ETristateState getState( ) {
        return this.state;
    }
}
