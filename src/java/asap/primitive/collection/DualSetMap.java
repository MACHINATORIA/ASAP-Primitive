package asap.primitive.collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DualSetMap< T1, T2 > {

    protected Map< T1, Set< T2 > > firstMap;

    protected Map< T2, Set< T1 > > secondMap;

    public DualSetMap( ) {
        this.firstMap = new HashMap< T1, Set< T2 > >( );
        this.secondMap = new HashMap< T2, Set< T1 > >( );
    }

    @SuppressWarnings( "serial" )
    protected abstract class ItemSet< SK, SV > extends HashSet< SV > {

        protected SK key;

        protected ItemSet( SK key ) {
            this.key = key;
        }

        protected class ItemIterator implements Iterator< SV > {

            protected Iterator< SV > iterator;

            protected SV             lastNext;

            protected ItemIterator( Iterator< SV > iterator ) {
                this.iterator = iterator;
                this.lastNext = null;
            }

            @Override
            public void forEachRemaining( Consumer< ? super SV > action ) {
                this.iterator.forEachRemaining( action );
            }

            @Override
            public boolean hasNext( ) {
                return this.iterator.hasNext( );
            }

            @Override
            public SV next( ) {
                return ( this.lastNext = this.iterator.next( ) );
            }

            @Override
            public void remove( ) {
                this.iterator.remove( );
                ItemSet.this._itemRemove( this.lastNext );
            }
        }

        @Override
        public boolean add( SV value ) {
            boolean tmpResult = super.add( value );
            if ( tmpResult ) {
                this._itemAdd( value );
            }
            return tmpResult;
        }

        @Override
        public Iterator< SV > iterator( ) {
            return new ItemIterator( super.iterator( ) );
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public boolean remove( Object object ) {
            boolean tmpResult = this.remove( object );
            if ( tmpResult ) {
                this._itemRemove( (SV) object );
            }
            return tmpResult;
        }

        @Override
        public String toString( ) {
            return String.format( "%s={%s}",
                                  this.key.toString( ),
                                  super.toString( ) );
        }

        protected abstract void _itemAdd( SV itemValue );

        protected abstract void _itemRemove( SV itemValue );
    }

    @SuppressWarnings( "serial" )
    protected void putFirst( T1 first,
                             T2 second ) {
        Set< T2 > tmpSecondSet = this.firstMap.get( first );
        if ( tmpSecondSet == null ) {
            tmpSecondSet = new ItemSet< T1, T2 >( first ) {

                @Override
                protected void _itemAdd( T2 secondValue ) {
                    DualSetMap.this.putSecond( secondValue,
                                               this.key );
                }

                @Override
                protected void _itemRemove( T2 secondValue ) {
                    DualSetMap.this.removeSecond( secondValue,
                                                  this.key );
                }
            };
            this.firstMap.put( first,
                               tmpSecondSet );
        }
        tmpSecondSet.add( second );
    }

    protected void removeFirst( T1 first,
                                T2 second ) {
        Set< T2 > tmpSecondSet = this.firstMap.get( first );
        if ( tmpSecondSet != null ) {
            tmpSecondSet.remove( second );
            if ( tmpSecondSet.size( ) == 0 ) {
                this.firstMap.remove( first );
            }
        }
    }

    @SuppressWarnings( "serial" )
    protected void putSecond( T2 second,
                              T1 first ) {
        Set< T1 > tmpFirstSet = this.secondMap.get( second );
        if ( tmpFirstSet == null ) {
            tmpFirstSet = new ItemSet< T2, T1 >( second ) {

                @Override
                protected void _itemAdd( T1 firstValue ) {
                    DualSetMap.this.putFirst( firstValue,
                                              this.key );
                }

                @Override
                protected void _itemRemove( T1 firstValue ) {
                    DualSetMap.this.removeFirst( firstValue,
                                                 this.key );
                }
            };
            this.secondMap.put( second,
                                tmpFirstSet );
        }
        tmpFirstSet.add( first );
    }

    protected void removeSecond( T2 second,
                                 T1 first ) {
        Set< T1 > tmpFirstSet = this.secondMap.get( second );
        if ( tmpFirstSet != null ) {
            tmpFirstSet.remove( first );
            if ( tmpFirstSet.size( ) == 0 ) {
                this.secondMap.remove( second );
            }
        }
    }

    public void put( T1 first,
                     T2 second ) {
        this.putFirst( first,
                       second );
        this.putSecond( second,
                        first );
    }

    public void remove( T1 first,
                        T2 second ) {
        this.removeFirst( first,
                          second );
        this.removeSecond( second,
                           first );
    }

    public Set< T2 > getFirst( T1 key ) {
        return this.firstMap.get( key );
    }

    public Set< T1 > getSecond( T2 value ) {
        return this.secondMap.get( value );
    }
}
