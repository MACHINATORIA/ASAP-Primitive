package asap.primitive.pattern;

import java.util.List;

public class Lambdas {

    public interface PermissionRequest {

        public boolean isPermitted( );
    }

    public interface VoidTask {

        public void execute( );
    }

    public interface ResultTask< R > {

        public R execute( );
    }

    public interface BooleanTask extends ResultTask< Boolean > {
    }

    public interface IntegerTask extends ResultTask< Integer > {
    }

    public interface StringTask extends ResultTask< String > {
    }

    public interface ValueComputer< I, O > {

        public O compute( I input );
    }

    public interface BooleanComputer< I > extends ValueComputer< I, Boolean > {
    }

    public interface IntegerComputer< I > extends ValueComputer< I, Integer > {
    }

    public interface StringComputer< I > extends ValueComputer< I, String > {
    }

    public interface ValueMapper< K, V > {

        public V map( K key );
    }

    public interface ValueConverter< I, O > {

        public O convert( I input );
    }

    public interface ValueFormatter< I > {

        public String format( I input );
    }

    public interface BooleanFormatter extends ValueFormatter< Boolean > {
    }

    public interface IntegerFormatter extends ValueFormatter< Integer > {
    }

    public interface StringFormatter extends ValueFormatter< String > {
    }

    public interface ValueBuilder< A, O > {

        public O build( A argument );
    }

    public interface StringBuilder< A > extends ValueBuilder< A, String > {
    }

    public interface StringListBuilder< A > extends ValueBuilder< A, List< String > > {
    }

    public interface EventListener {

        public void happened( );
    }

    public interface ExceptionListener< T extends Throwable > {

        public void thrown( T exception );
    }

    public interface ErrorListener< E extends Enum< E > > {

        public void occurred( E error );
    }

    public interface ChangeListener< T > {

        public void changed( T previous,
                             T current );
    }

    public interface RangeListener {

        public void range( int offset,
                           int length );
    }
}
