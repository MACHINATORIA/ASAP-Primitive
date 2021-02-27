package asap.primitive.pattern;

public class PairPattern {

    public static class Pair< F, S > {

        public F first;

        public S second;

        public Pair( F first,
                     S second ) {
            this.first = first;
            this.second = second;
        }

        public Pair( ) {
            this( null,
                  null );
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public boolean equals( Object anotherObject ) {
            if ( ( anotherObject == null ) || !this.getClass( ).isAssignableFrom( anotherObject.getClass( ) ) ) {
                return false;
            }
            Pair< F, S > tmpAnotherPair = (Pair< F, S >) anotherObject;
            if ( ( ( this.first == null ) ^ ( tmpAnotherPair.first == null ) )
                 || ( ( this.second == null ) ^ ( tmpAnotherPair.second == null ) ) ) {
                return false;
            }
            if ( ( ( this.first != null ) && !this.first.equals( tmpAnotherPair.first ) )
                 || ( ( this.second != null ) && !this.second.equals( tmpAnotherPair.second ) ) ) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode( ) {
            return ( ( ( this.first == null ) ? 0
                                              : this.first.hashCode( ) )
                     + ( ( this.second == null ) ? 0
                                                 : this.second.hashCode( ) ) );
        }
    }
}
