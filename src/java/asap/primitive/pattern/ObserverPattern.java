package asap.primitive.pattern;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import asap.primitive.pattern.ValuePattern.BasicValue;
import asap.primitive.pattern.ValuePattern.ReadOnlyValue;
import asap.primitive.pattern.ValuePattern.ThreadValue;
import asap.primitive.pattern.ValuePattern.Value;

public class ObserverPattern {

    public static interface ValueObserver< V > {

        public abstract void changed( V oldValue,
                                      V newValue );
    }

    public static interface ObservableReadOnlyValue< V > extends ReadOnlyValue< V > {

        public abstract void attachObserver( ValueObserver< V > observer );

        public abstract void detachObserver( ValueObserver< V > observer );
    }

    public static interface ObservableValue< V > extends Value< V > {

        public abstract void attachObserver( ValueObserver< V > observer );

        public abstract void detachObserver( ValueObserver< V > observer );

        public abstract ObservableReadOnlyValue< V > getReadOnly( );
    }

    public static class BasicObservableValue< V > extends BasicValue< V > implements ObservableValue< V > {

        protected List< ValueObserver< V > >   observers;

        protected ObservableReadOnlyValue< V > readOnly;

        public BasicObservableValue( V initialValue ) {
            super( initialValue );
            this.observers = new ArrayList< ValueObserver< V > >( );
            this.readOnly = null;
        }

        public BasicObservableValue( ) {
            this( null );
        }

        @Override
        public void attachObserver( ValueObserver< V > observer ) {
            if ( ( observer != null ) && !this.observers.contains( observer ) ) {
                this.observers.add( observer );
                observer.changed( null,
                                  this.value );
            }
        }

        @Override
        public void detachObserver( ValueObserver< V > observer ) {
            if ( ( observer != null ) && this.observers.contains( observer ) ) {
                this.observers.remove( observer );
                observer.changed( this.value,
                                  null );
            }
        }

        @Override
        public void set( V value ) {
            V tmpOldValue = this.get( );
            if ( ( tmpOldValue == null ) ? ( value != null )
                                         : !tmpOldValue.equals( value ) ) {
                super.set( value );
                for ( ValueObserver< V > tmpObserver : this.observers ) {
                    tmpObserver.changed( tmpOldValue,
                                         value );
                }
            }
        }

        @Override
        public ObservableReadOnlyValue< V > getReadOnly( ) {
            if ( this.readOnly == null ) {
                this.readOnly = new ObservableReadOnlyValue< V >( ) {

                    @Override
                    public V get( ) {
                        return BasicObservableValue.this.get( );
                    }

                    @Override
                    public void attachObserver( ValueObserver< V > observer ) {
                        BasicObservableValue.this.attachObserver( observer );
                    }

                    @Override
                    public void detachObserver( ValueObserver< V > observer ) {
                        BasicObservableValue.this.detachObserver( observer );
                    }
                };
            }
            return this.readOnly;
        }
    }

    public static interface ObservableThreadValue< V > extends ObservableValue< V > {

        public abstract void attachObserver( Executor executor,
                                             ValueObserver< V > observer );
    }

    public static class BasicObservableThreadValue< V > extends ThreadValue< V > implements ObservableThreadValue< V > {

        protected List< ValueObserver< V > >          observers;

        protected Map< ValueObserver< V >, Executor > executorMap;

        protected Executor                            defaultExecutor;

        protected ObservableReadOnlyValue< V >        readOnly;

        public BasicObservableThreadValue( V initialValue,
                                           Executor defaultExecutor ) {
            super( initialValue );
            this.observers = new ArrayList< ValueObserver< V > >( );
            this.executorMap = new HashMap< ValueObserver< V >, Executor >( );
            this.defaultExecutor = defaultExecutor;
        }

        public BasicObservableThreadValue( V initialValue ) {
            this( initialValue,
                  null );
        }

        public BasicObservableThreadValue( ) {
            this( null,
                  null );
        }

        protected void notifyObserver( Executor executor,
                                       final ValueObserver< V > observer,
                                       final V oldValue,
                                       final V newValue ) {
            Executor tmpExecutor = ( executor != null ) ? executor
                                                        : this.defaultExecutor;
            if ( tmpExecutor != null ) {
                tmpExecutor.execute( new Runnable( ) {

                    protected ValueObserver< V > _observer = observer;

                    protected V                  _oldValue = oldValue;

                    protected V                  _newValue = newValue;

                    @Override
                    public void run( ) {
                        try {
                            this._observer.changed( this._oldValue,
                                                    this._newValue );
                        }
                        catch ( Throwable e ) {
                        }
                    }
                } );
            }
            else {
                try {
                    observer.changed( oldValue,
                                      newValue );
                }
                catch ( Throwable e ) {
                }
            }
        }

        @Override
        public void attachObserver( ValueObserver< V > observer ) {
            this.attachObserver( null,
                                 observer );
        }

        @Override
        public void attachObserver( Executor executor,
                                    ValueObserver< V > observer ) {
            synchronized ( this ) {
                if ( ( observer != null ) && !this.observers.contains( observer ) ) {
                    this.observers.add( observer );
                    this.executorMap.put( observer,
                                          executor );
                    this.notifyObserver( executor,
                                         observer,
                                         null,
                                         this.value );
                }
            }
        }

        @Override
        public void detachObserver( ValueObserver< V > observer ) {
            synchronized ( this ) {
                if ( ( observer != null ) && this.observers.contains( observer ) ) {
                    this.observers.remove( observer );
                    Executor tmpExecutor = this.executorMap.remove( observer );
                    this.notifyObserver( tmpExecutor,
                                         observer,
                                         this.value,
                                         null );
                }
            }
        }

        @Override
        public void set( V value ) {
            synchronized ( this ) {
                V tmpOldValue = this.value;
                if ( ( tmpOldValue == null ) ? ( value != null )
                                             : !tmpOldValue.equals( value ) ) {
                    this.value = value;
                    for ( ValueObserver< V > tmpObserver : this.observers ) {
                        Executor tmpExecutor = this.executorMap.get( tmpObserver );
                        this.notifyObserver( tmpExecutor,
                                             tmpObserver,
                                             tmpOldValue,
                                             value );
                    }
                }
            }
        }

        @Override
        public ObservableReadOnlyValue< V > getReadOnly( ) {
            if ( this.readOnly == null ) {
                this.readOnly = new ObservableReadOnlyValue< V >( ) {

                    @Override
                    public V get( ) {
                        return BasicObservableThreadValue.this.get( );
                    }

                    @Override
                    public void attachObserver( ValueObserver< V > observer ) {
                        BasicObservableThreadValue.this.attachObserver( observer );
                    }

                    @Override
                    public void detachObserver( ValueObserver< V > observer ) {
                        BasicObservableThreadValue.this.detachObserver( observer );
                    }
                };
            }
            return this.readOnly;
        }
    }

    public static interface ListObserver< V > {

        public abstract void added( int index,
                                    V element );

        public abstract void changed( int index,
                                      V oldElement,
                                      V newElement );

        public abstract void removed( int index,
                                      V element );
    }

    public static interface ObservableList< V > extends List< V > {

        public abstract void attachObserver( ListObserver< V > observer );

        public abstract void detachObserver( ListObserver< V > observer );
    }

    public static class BasicObservableList< V > extends AbstractList< V > implements ObservableList< V > {

        protected List< V >                 valueList;

        protected List< ListObserver< V > > observers;

        public BasicObservableList( ) {
            this.valueList = new ArrayList< V >( );
            this.observers = new ArrayList< ListObserver< V > >( );
        }

        public BasicObservableList( int initialCapacity ) {
            this.valueList = new ArrayList< V >( initialCapacity );
            this.observers = new ArrayList< ListObserver< V > >( );
        }

        public BasicObservableList( Collection< ? extends V > collection ) {
            this.valueList = new ArrayList< V >( collection );
            this.observers = new ArrayList< ListObserver< V > >( );
        }

        public void attachObserver( ListObserver< V > observer ) {
            if ( ( observer != null ) && !this.observers.contains( observer ) ) {
                this.observers.add( observer );
                for ( int tmpIndex = 0; tmpIndex < this.valueList.size( ); tmpIndex++ ) {
                    try {
                        observer.added( tmpIndex,
                                        this.valueList.get( tmpIndex ) );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        public void detachObserver( ListObserver< V > observer ) {
            if ( ( observer != null ) && this.observers.contains( observer ) ) {
                this.observers.remove( observer );
                for ( int tmpIndex = 0; tmpIndex < this.valueList.size( ); tmpIndex++ ) {
                    try {
                        observer.removed( tmpIndex,
                                          this.valueList.get( tmpIndex ) );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        @Override
        public V get( int index ) {
            return this.valueList.get( index );
        }

        @Override
        public void add( int index,
                         V element ) {
            this.valueList.add( index,
                                element );
            for ( ListObserver< V > tmpObserver : this.observers ) {
                try {
                    tmpObserver.added( index,
                                       element );
                }
                catch ( Throwable e ) {
                }
            }
        }

        @Override
        public V remove( int index ) {
            V tmpOldElement = this.valueList.remove( index );
            for ( ListObserver< V > tmpObserver : this.observers ) {
                try {
                    tmpObserver.removed( index,
                                         tmpOldElement );
                }
                catch ( Throwable e ) {
                }
            }
            return tmpOldElement;
        }

        @Override
        public V set( int index,
                      V element ) {
            V tmpOldElement = this.valueList.set( index,
                                                  element );
            if ( ( tmpOldElement == null ) ? ( element != null )
                                           : !tmpOldElement.equals( element ) ) {
                for ( ListObserver< V > tmpObserver : this.observers ) {
                    try {
                        tmpObserver.changed( index,
                                             tmpOldElement,
                                             element );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
            return tmpOldElement;
        }

        @Override
        public int size( ) {
            return this.valueList.size( );
        }
    }

    public static interface SetObserver< V > {

        public abstract void added( V element );

        public abstract void removed( V element );
    }

    public static interface ObservableSet< V > extends Set< V > {

        public abstract void attachObserver( SetObserver< V > observer );

        public abstract void detachObserver( SetObserver< V > observer );
    }

    public static class BasicObservableSet< V > extends AbstractSet< V > implements ObservableSet< V > {

        protected Set< V >                 valueSet;

        protected List< SetObserver< V > > observers;

        protected static class ObservableSetIterator< V > implements Iterator< V > {

            protected BasicObservableSet< V > observableSet;

            protected Iterator< V >           iterator;

            protected V                       lastValue;

            public ObservableSetIterator( BasicObservableSet< V > observableSet ) {
                this.observableSet = observableSet;
                this.iterator = observableSet.valueSet.iterator( );
                this.lastValue = null;
            }

            @Override
            public boolean hasNext( ) {
                return this.iterator.hasNext( );
            }

            @Override
            public V next( ) {
                this.lastValue = this.iterator.next( );
                return this.lastValue;
            }

            @Override
            public void remove( ) {
                this.iterator.remove( );
                for ( SetObserver< V > tmpObserver : this.observableSet.observers ) {
                    try {
                        tmpObserver.removed( this.lastValue );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        public BasicObservableSet( ) {
            this.valueSet = new HashSet< V >( );
            this.observers = new ArrayList< SetObserver< V > >( );
        }

        public BasicObservableSet( int initialCapacity ) {
            this.valueSet = new HashSet< V >( initialCapacity );
            this.observers = new ArrayList< SetObserver< V > >( );
        }

        public BasicObservableSet( Collection< ? extends V > collection ) {
            this.valueSet = new HashSet< V >( collection );
            this.observers = new ArrayList< SetObserver< V > >( );
        }

        public void attachObserver( SetObserver< V > observer ) {
            if ( ( observer != null ) && !this.observers.contains( observer ) ) {
                this.observers.add( observer );
                for ( V tmpValue : this.valueSet ) {
                    try {
                        observer.added( tmpValue );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        public void detachObserver( SetObserver< V > observer ) {
            if ( ( observer != null ) && this.observers.contains( observer ) ) {
                this.observers.remove( observer );
                for ( V tmpValue : this.valueSet ) {
                    try {
                        observer.removed( tmpValue );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        @Override
        public boolean add( V value ) {
            boolean tmpResult = this.valueSet.add( value );
            if ( tmpResult ) {
                for ( SetObserver< V > tmpObserver : this.observers ) {
                    try {
                        tmpObserver.added( value );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
            return tmpResult;
        }

        @Override
        public Iterator< V > iterator( ) {
            return new ObservableSetIterator< V >( this );
        }

        @Override
        public int size( ) {
            return this.valueSet.size( );
        }
    }

    public static interface MapObserver< K, V > {

        public abstract void added( K key,
                                    V value );

        public abstract void changed( K key,
                                      V oldValue,
                                      V newValue );

        public abstract void removed( K key,
                                      V value );
    }

    public static interface ObservableMap< K, V > extends Map< K, V > {

        public abstract void attachObserver( MapObserver< K, V > observer );

        public abstract void detachObserver( MapObserver< K, V > observer );
    }

    public static class BasicObservableMap< K, V > extends AbstractMap< K, V > implements ObservableMap< K, V > {

        protected BasicObservableSet< Map.Entry< K, V > > entrySet;

        protected List< MapObserver< K, V > >             observers;

        public BasicObservableMap( ) {
            this.entrySet = new BasicObservableSet< Map.Entry< K, V > >( );
            this.construct( );
        }

        public BasicObservableMap( int initialCapacity ) {
            this.entrySet = new BasicObservableSet< Map.Entry< K, V > >( initialCapacity );
            this.construct( );
        }

        public BasicObservableMap( Map< K, V > anotherMap ) {
            this.entrySet = new BasicObservableSet< Map.Entry< K, V > >( anotherMap.entrySet( ) );
            this.construct( );
        }

        protected void construct( ) {
            this.observers = new ArrayList< MapObserver< K, V > >( );
            this.entrySet.attachObserver( new SetObserver< Entry< K, V > >( ) {

                @Override
                public void added( java.util.Map.Entry< K, V > element ) {
                    for ( MapObserver< K, V > tmpObserver : BasicObservableMap.this.observers ) {
                        try {
                            tmpObserver.added( element.getKey( ),
                                               element.getValue( ) );
                        }
                        catch ( Throwable e ) {
                        }
                    }
                }

                @Override
                public void removed( java.util.Map.Entry< K, V > element ) {
                    for ( MapObserver< K, V > tmpObserver : BasicObservableMap.this.observers ) {
                        try {
                            tmpObserver.removed( element.getKey( ),
                                                 element.getValue( ) );
                        }
                        catch ( Throwable e ) {
                        }
                    }
                }
            } );
        }

        public void attachObserver( MapObserver< K, V > observer ) {
            if ( ( observer != null ) && !this.observers.contains( observer ) ) {
                this.observers.add( observer );
                for ( Map.Entry< K, V > tmpEntry : this.entrySet ) {
                    try {
                        observer.added( tmpEntry.getKey( ),
                                        tmpEntry.getValue( ) );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        public void detachObserver( MapObserver< K, V > observer ) {
            if ( ( observer != null ) && this.observers.contains( observer ) ) {
                this.observers.remove( observer );
                for ( Map.Entry< K, V > tmpEntry : this.entrySet ) {
                    try {
                        observer.removed( tmpEntry.getKey( ),
                                          tmpEntry.getValue( ) );
                    }
                    catch ( Throwable e ) {
                    }
                }
            }
        }

        @Override
        public Set< Map.Entry< K, V > > entrySet( ) {
            return this.entrySet;
        }

        @Override
        public V put( K key,
                      V value ) {
            K tmpKey = null;
            V tmpValue = null;
            for ( Map.Entry< K, V > tmpEntry : this.entrySet ) {
                K tmpEntryKey = tmpEntry.getKey( );
                if ( ( tmpEntryKey == null ) ? ( key == null )
                                             : tmpEntryKey.equals( key ) ) {
                    tmpKey = tmpEntryKey;
                    tmpValue = tmpEntry.getValue( );
                    if ( ( tmpValue == null ) ? ( value != null )
                                              : !tmpValue.equals( value ) ) {
                        tmpEntry.setValue( value );
                        for ( MapObserver< K, V > tmpObserver : this.observers ) {
                            try {
                                tmpObserver.changed( tmpKey,
                                                     tmpValue,
                                                     value );
                            }
                            catch ( Throwable e ) {
                            }
                        }
                    }
                    break;
                }
            }
            if ( tmpKey == null ) {
                this.entrySet.add( new AbstractMap.SimpleEntry< K, V >( key,
                                                                        value ) );
            }
            return tmpValue;
        }
    }
}
