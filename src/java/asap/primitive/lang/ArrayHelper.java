package asap.primitive.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayHelper {

    @SafeVarargs
    public static < T > T[ ] build( T... items ) {
        return items;
    }

    public static < T > T[ ] arrayCopy( T[ ] sourceArray,
                                        T[ ] targetArray ) {
        if ( ( sourceArray != null ) && ( targetArray != null ) ) {
            System.arraycopy( sourceArray,
                              0,
                              targetArray,
                              0,
                              ( targetArray.length < sourceArray.length ) ? targetArray.length
                                                                          : sourceArray.length );
        }
        return targetArray;
    }

    public static < T > T[ ] copyOf( T[ ] sourceArray ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOf( sourceArray,
                                                        sourceArray.length );
    }

    public static < T > T[ ] copyOf( T[ ] sourceArray,
                                     int newLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOf( sourceArray,
                                                        newLength );
    }

    public static < T > T[ ] copyOfRange( T[ ] sourceArray,
                                          int rangeOffset,
                                          int rangeLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             rangeOffset,
                                                             ( rangeOffset + rangeLength ) );
    }

    public static < T > T[ ] subArray( T[ ] sourceArray,
                                       int subOffset,
                                       int subLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             subOffset,
                                                             ( subOffset + subLength ) );
    }

    public static < T > T[ ] leftSubArray( T[ ] sourceArray,
                                           int leftLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             0,
                                                             leftLength );
    }

    public static < T > T[ ] rightSubArray( T[ ] sourceArray,
                                            int rightLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             ( sourceArray.length - rightLength ),
                                                             sourceArray.length );
    }

    @SuppressWarnings( "unchecked" )
    public static < T > List< T > asList( T[ ]... arrays ) {
        List< T > tmpResult = new ArrayList< T >( );
        for ( T[ ] tmpArray : arrays ) {
            if ( tmpArray != null ) {
                for ( T tmpValue : tmpArray ) {
                    tmpResult.add( tmpValue );
                }
            }
        }
        return tmpResult;
    }
}
