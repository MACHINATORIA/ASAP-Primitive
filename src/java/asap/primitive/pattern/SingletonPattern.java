package asap.primitive.pattern;

public class SingletonPattern {

    public static interface Singleton< T > {

        public T get( );
    }

    public static final class BasicSingleton< T > implements Singleton< T > {

        protected T instance;

        public BasicSingleton( T instance ) {
            if ( instance == null ) {
                throw new Error( "Singleton instance is null" );
            }
            this.instance = instance;
        }

        @Override
        public T get( ) {
            return this.instance;
        }
    }

    public static final class WriteOnceSingleton< T > implements Singleton< T > {

        protected T instance;

        public WriteOnceSingleton( ) {
            this.instance = null;
        }

        @Override
        public T get( ) {
            if ( instance == null ) {
                throw new Error( "Singleton was not loaded" );
            }
            return instance;
        }

        public static < T > void set( Singleton< T > singleton,
                                      T instance ) {
            if ( ( singleton == null ) || !( singleton instanceof WriteOnceSingleton ) ) {
                throw new Error( "Singleton object is null or invalid" );
            }
            if ( ( (WriteOnceSingleton< T >) singleton ).instance != null ) {
                throw new Error( "Singleton instance is already set" );
            }
            if ( instance == null ) {
                throw new Error( "Singleton instance is null" );
            }
            ( (WriteOnceSingleton< T >) singleton ).instance = instance;
        }
    }
}
