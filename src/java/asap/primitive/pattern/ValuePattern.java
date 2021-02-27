package asap.primitive.pattern;

public class ValuePattern {

    public static interface ReadOnlyValue< V > {

        public abstract V get( );
    }

    public static interface Value< V > {

        public abstract V get( );

        public abstract void set( V value );

        public abstract ReadOnlyValue< V > getReadOnly( );
    }

    /*
     * Implementação mínima básica
     */
    public static class BasicValue< V > implements Value< V > {

        protected V                  value;

        protected ReadOnlyValue< V > readOnly;

        public BasicValue( ) {
            this.value = null;
        }

        public BasicValue( V initialValue ) {
            this.value = initialValue;
            this.readOnly = null;
        }

        @Override
        public V get( ) {
            return this.value;
        }

        @Override
        public void set( V value ) {
            this.value = value;
        }

        public ReadOnlyValue< V > getReadOnly( ) {
            if ( this.readOnly == null ) {
                this.readOnly = new ReadOnlyValue< V >( ) {

                    @Override
                    public V get( ) {
                        return BasicValue.this.value;
                    }
                };
            }
            return this.readOnly;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public boolean equals( Object anotherObject ) {
            if ( ( anotherObject == null ) || ( !Value.class.isAssignableFrom( anotherObject.getClass( ) ) ) ) {
                return false;
            }
            Object tmpAnotherValue = ( (Value< V >) anotherObject ).get( );
            if ( ( ( this.value == null ) ^ ( tmpAnotherValue == null ) )
                 || ( ( this.value != null ) && !this.value.equals( tmpAnotherValue ) ) ) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode( ) {
            return ( Value.class.hashCode( ) //
                     + ( ( this.value == null ) ? 0
                                                : this.value.hashCode( ) ) );
        }
    }

    /*
     * Implementação com controle de concorrência
     */
    public static class ThreadValue< V > extends BasicValue< V > {

        public ThreadValue( V initialValue ) {
            super( initialValue );
        }

        public ThreadValue( ) {
            this( null );
        }

        @Override
        public V get( ) {
            synchronized ( this ) {
                return super.get( );
            }
        }

        @Override
        public void set( V value ) {
            synchronized ( this ) {
                super.set( value );
            }
        }

        @Override
        public ReadOnlyValue< V > getReadOnly( ) {
            synchronized ( this ) {
                if ( this.readOnly == null ) {
                    this.readOnly = new ReadOnlyValue< V >( ) {

                        @Override
                        public V get( ) {
                            synchronized ( ThreadValue.this ) {
                                return ThreadValue.super.get( );
                            }
                        }
                    };
                }
                return this.readOnly;
            }
        }

        @Override
        public boolean equals( Object anotherObject ) {
            synchronized ( this ) {
                return super.equals( anotherObject );
            }
        }

        @Override
        public int hashCode( ) {
            synchronized ( this ) {
                return super.hashCode( );
            }
        }
    }

    /*
     * Implementação mínima/básica com nome (não influencia 'equals/hashCode')
     */
    public static class NamedValue< V > extends BasicValue< V > {

        protected String name;

        public NamedValue( String name,
                           V value ) {
            super( value );
            this.name = name;
        }

        public NamedValue( String name ) {
            this( name,
                  null );
        }

        public String getName( ) {
            return this.name;
        }
    }
}
