package asap.primitive.lang;

public class JavaHelper {

    public static boolean equals( Object object1,
                                  Object object2 ) {
        return ( ( ( object1 == null ) && ( object2 == null ) )
                 || ( ( object1 != null ) && object1.equals( object2 ) ) );
    }
}
