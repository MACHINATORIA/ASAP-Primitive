package asap.primitive.swing.component;

public enum ETristateState {
    SELECTED {

        public ETristateState next( ) {
            return DESELECTED;
        }
    },
    INDETERMINATE {

        public ETristateState next( ) {
            return DESELECTED;
        }
    },
    DESELECTED {

        public ETristateState next( ) {
            return SELECTED;
        }
    };

    public abstract ETristateState next( );
}
