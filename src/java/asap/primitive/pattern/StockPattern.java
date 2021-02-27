package asap.primitive.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockPattern {

    /*
     * -----------------------------------------------------------------------
     */
    public static interface StockProvider< T > {

        public List< T > getItems( );

        public int getItemCount( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface StockListener< T > {

        public void itemAdded( T item );

        public void itemRemoved( T item );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface StockRegistry< T > extends StockProvider< T > {

        public boolean addListener( StockListener< ? super T > listener );

        public boolean removeListener( StockListener< ? super T > listener );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface Stock< T > extends StockProvider< T > {

        public StockRegistry< T > getRegistry( );

        public boolean addItem( T item );

        public < E extends T > int addItems( E[ ] items );

        public int addItems( List< ? extends T > items );

        public boolean containsItem( T item );

        public boolean removeItem( T item );

        public < E extends T > int removeItems( E[ ] items );

        public int removeItems( List< ? extends T > items );

        public void clear( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface SuperStock< S, T extends S > extends Stock< T > {

        public StockRegistry< S > getSuperRegistry( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicStock< T > implements Stock< T > {

        protected List< T >                          items;

        protected List< StockListener< ? super T > > listeners;

        protected StockRegistry< T >                 registry;

        protected abstract class AbstractStockRegistry< C > implements StockRegistry< C > {

            protected abstract List< C > _getItems( );

            protected abstract void _addListener( StockListener< ? super C > listener );

            protected abstract void _removeListener( StockListener< ? super C > listener );

            public List< C > getItems( ) {
                synchronized ( BasicStock.this ) {
                    return this._getItems( );
                }
            }

            public int getItemCount( ) {
                synchronized ( BasicStock.this ) {
                    return BasicStock.this.items.size( );
                }
            }

            @Override
            public boolean addListener( StockListener< ? super C > listener ) {
                synchronized ( BasicStock.this ) {
                    if ( ( listener == null ) || BasicStock.this.listeners.contains( listener ) ) {
                        return false;
                    }
                    this._addListener( listener );
                    for ( C tmpItem : this._getItems( ) ) {
                        try {
                            listener.itemAdded( tmpItem );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                    }
                    return true;
                }
            }

            @Override
            public boolean removeListener( StockListener< ? super C > listener ) {
                synchronized ( BasicStock.this ) {
                    if ( !BasicStock.this.listeners.contains( listener ) ) {
                        return false;
                    }
                    this._removeListener( listener );
                    for ( C tmpItem : this._getItems( ) ) {
                        try {
                            listener.itemRemoved( tmpItem );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                    }
                    return true;
                }
            }
        }

        public BasicStock( ) {
            this( new ArrayList< T >( ) );
        }

        @SuppressWarnings( "unchecked" )
        public BasicStock( T... itemArray ) {
            this( Arrays.asList( itemArray ) );
        }

        public BasicStock( List< T > itemList ) {
            this.items = new ArrayList< T >( itemList );
            this.listeners = new ArrayList< StockListener< ? super T > >( );
            this.registry = new AbstractStockRegistry< T >( ) {

                @Override
                protected List< T > _getItems( ) {
                    return new ArrayList< T >( BasicStock.this.items );
                }

                @Override
                protected void _addListener( StockListener< ? super T > listener ) {
                    BasicStock.this.listeners.add( listener );
                }

                @Override
                protected void _removeListener( StockListener< ? super T > listener ) {
                    BasicStock.this.listeners.remove( listener );
                }
            };
        }

        @Override
        public StockRegistry< T > getRegistry( ) {
            return this.registry;
        }

        @Override
        public List< T > getItems( ) {
            synchronized ( this ) {
                return new ArrayList< T >( this.items );
            }
        }

        @Override
        public int getItemCount( ) {
            synchronized ( this ) {
                return this.items.size( );
            }
        }

        @Override
        public boolean addItem( T item ) {
            synchronized ( this ) {
                if ( ( item == null ) || this.items.contains( item ) ) {
                    return false;
                }
                this.items.add( item );
                for ( StockListener< ? super T > tmpListener : this.listeners ) {
                    tmpListener.itemAdded( item );
                }
                return true;
            }
        }

        @Override
        public < E extends T > int addItems( E[ ] items ) {
            return this.addItems( Arrays.asList( items ) );
        }

        @Override
        public int addItems( List< ? extends T > items ) {
            synchronized ( this ) {
                int tmpAddedCount = 0;
                for ( T tmpItem : items ) {
                    if ( this.addItem( tmpItem ) ) {
                        ++tmpAddedCount;
                    }
                }
                return tmpAddedCount;
            }
        }

        @Override
        public boolean containsItem( T item ) {
            synchronized ( this ) {
                return this.items.contains( item );
            }
        }

        @Override
        public boolean removeItem( T item ) {
            synchronized ( this ) {
                if ( !this.items.remove( item ) ) {
                    return false;
                }
                for ( StockListener< ? super T > tmpListener : this.listeners ) {
                    tmpListener.itemRemoved( item );
                }
                return true;
            }
        }

        @Override
        public < E extends T > int removeItems( E[ ] items ) {
            return this.removeItems( Arrays.asList( items ) );
        }

        @Override
        public int removeItems( List< ? extends T > items ) {
            synchronized ( this ) {
                int tmpRemovedCount = 0;
                for ( T tmpItem : items ) {
                    if ( this.removeItem( tmpItem ) ) {
                        ++tmpRemovedCount;
                    }
                }
                return tmpRemovedCount;
            }
        }

        @Override
        public void clear( ) {
            synchronized ( this ) {
                List< T > tmpItems = new ArrayList< T >( this.items );
                for ( T tmpItem : tmpItems ) {
                    this.items.remove( tmpItem );
                    for ( StockListener< ? super T > tmpListener : this.listeners ) {
                        tmpListener.itemRemoved( tmpItem );
                    }
                }
            }
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicSuperStock< S, T extends S > extends BasicStock< T > implements SuperStock< S, T > {

        protected StockRegistry< S > superRegistry;

        public BasicSuperStock( ) {
            this( new ArrayList< T >( ) );
        }

        @SuppressWarnings( "unchecked" )
        public BasicSuperStock( T... itemArray ) {
            this( Arrays.asList( itemArray ) );
        }

        public BasicSuperStock( List< T > itemList ) {
            this.superRegistry = new AbstractStockRegistry< S >( ) {

                @Override
                protected List< S > _getItems( ) {
                    return new ArrayList< S >( BasicSuperStock.this.items );
                }

                @Override
                protected void _addListener( StockListener< ? super S > listener ) {
                    BasicSuperStock.this.registry.addListener( listener );
                }

                @Override
                protected void _removeListener( StockListener< ? super S > listener ) {
                    BasicSuperStock.this.registry.removeListener( listener );
                }
            };
        }

        @Override
        public StockRegistry< S > getSuperRegistry( ) {
            return this.superRegistry;
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface RootStock< T > extends StockProvider< T > {
    
        public StockRegistry< T > getRegistry( );
    
        public boolean addChildStock( Stock< ? extends T > childStock );
    
        public boolean addChildStock( RootStock< ? extends T > childRootStock );
    
        public boolean addChildStock( StockRegistry< ? extends T > childStockRegistry );
    
        public boolean removeChildStock( Stock< ? extends T > childStock );
    
        public boolean removeChildStock( RootStock< ? extends T > childRootStock );
    
        public boolean removeChildStock( StockRegistry< ? extends T > childStockRegistry );
    
        public boolean containsChildStock( Stock< ? extends T > childStock );
    
        public boolean containsChildStock( RootStock< ? extends T > childRootStock );
    
        public boolean containsChildStock( StockRegistry< ? extends T > childStockRegistry );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface SuperRootStock< S, T extends S > extends RootStock< T > {
    
        public StockRegistry< S > getSuperRegistry( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicRootStock< T > implements RootStock< T > {

        protected List< StockRegistry< ? extends T > > childStocks;

        protected StockListener< T >                   childStocksListener;

        protected List< StockListener< ? super T > >   rootListeners;

        protected StockRegistry< T >                   rootRegistry;

        protected abstract class AbstractRootStockRegistry< C > implements StockRegistry< C > {

            protected abstract List< StockRegistry< ? extends C > > _getChildStocks( );

            protected abstract void _addListener( StockListener< ? super C > listener );

            protected abstract void _removeListener( StockListener< ? super C > listener );

            protected List< C > _getItems( ) {
                List< C > tmpResult = new ArrayList< C >( );
                for ( StockRegistry< ? extends C > tmpChild : this._getChildStocks( ) ) {
                    tmpResult.addAll( tmpChild.getItems( ) );
                }
                return tmpResult;
            }

            @Override
            public List< C > getItems( ) {
                synchronized ( BasicRootStock.this ) {
                    return this._getItems( );
                }
            }

            @Override
            public int getItemCount( ) {
                synchronized ( this ) {
                    int tmpResult = 0;
                    for ( StockRegistry< ? extends C > tmpChildStock : this._getChildStocks( ) ) {
                        tmpResult += tmpChildStock.getItemCount( );
                    }
                    return tmpResult;
                }
            }

            @Override
            public boolean addListener( StockListener< ? super C > listener ) {
                synchronized ( BasicRootStock.this ) {
                    if ( ( listener == null ) || BasicRootStock.this.rootListeners.contains( listener ) ) {
                        return false;
                    }
                    this._addListener( listener );
                    for ( C tmpItem : this._getItems( ) ) {
                        try {
                            listener.itemAdded( tmpItem );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                    }
                    return true;
                }
            }

            @Override
            public boolean removeListener( StockListener< ? super C > listener ) {
                synchronized ( BasicRootStock.this ) {
                    if ( ( listener == null ) || !BasicRootStock.this.rootListeners.contains( listener ) ) {
                        return false;
                    }
                    this._removeListener( listener );
                    for ( C tmpItem : this._getItems( ) ) {
                        try {
                            listener.itemRemoved( tmpItem );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                    }
                    return true;
                }
            }
        }

        @SuppressWarnings( "unchecked" )
        public BasicRootStock( Stock< T >... childStocks ) {
            this( );
            for ( Stock< T > tmpChildStok : childStocks ) {
                StockRegistry< ? extends T > tmpRegistry = tmpChildStok.getRegistry( );
                this.childStocks.add( tmpRegistry );
                tmpRegistry.addListener( this.childStocksListener );
            }
        }

        @SuppressWarnings( "unchecked" )
        public BasicRootStock( StockRegistry< T >... childStocks ) {
            this( Arrays.asList( childStocks ) );
        }

        public BasicRootStock( List< StockRegistry< T > > childRegistries ) {
            this( );
            for ( StockRegistry< T > tmpChildRegistry : childRegistries ) {
                this.childStocks.add( tmpChildRegistry );
                tmpChildRegistry.addListener( this.childStocksListener );
            }
        }

        public BasicRootStock( ) {
            this.childStocks = new ArrayList< StockRegistry< ? extends T > >( );
            this.childStocksListener = new StockListener< T >( ) {

                @Override
                public void itemAdded( T item ) {
                    synchronized ( BasicRootStock.this ) {
                        for ( StockListener< ? super T > tmpListener : BasicRootStock.this.rootListeners ) {
                            tmpListener.itemAdded( item );
                        }
                    }
                }

                @Override
                public void itemRemoved( T item ) {
                    synchronized ( BasicRootStock.this ) {
                        for ( StockListener< ? super T > tmpListener : BasicRootStock.this.rootListeners ) {
                            tmpListener.itemRemoved( item );
                        }
                    }
                }
            };
            this.rootListeners = new ArrayList< StockListener< ? super T > >( );
            this.rootRegistry = new AbstractRootStockRegistry< T >( ) {

                @Override
                protected List< StockRegistry< ? extends T > > _getChildStocks( ) {
                    return BasicRootStock.this.childStocks;
                }

                @Override
                protected void _addListener( StockListener< ? super T > listener ) {
                    BasicRootStock.this.rootListeners.add( listener );
                }

                @Override
                protected void _removeListener( StockListener< ? super T > listener ) {
                    BasicRootStock.this.rootListeners.remove( listener );
                }
            };
        }

        @Override
        public List< T > getItems( ) {
            synchronized ( this ) {
                List< T > tmpResult = new ArrayList< T >( );
                for ( StockRegistry< ? extends T > tmpChildStock : this.childStocks ) {
                    tmpResult.addAll( tmpChildStock.getItems( ) );
                }
                return tmpResult;
            }
        }

        @Override
        public int getItemCount( ) {
            synchronized ( this ) {
                int tmpResult = 0;
                for ( StockRegistry< ? extends T > tmpChildStock : this.childStocks ) {
                    tmpResult += tmpChildStock.getItemCount( );
                }
                return tmpResult;
            }
        }

        @Override
        public StockRegistry< T > getRegistry( ) {
            return this.rootRegistry;
        }

        @Override
        public boolean addChildStock( Stock< ? extends T > childStock ) {
            return this.addChildStock( childStock.getRegistry( ) );
        }

        @Override
        public boolean addChildStock( RootStock< ? extends T > childRootStock ) {
            return this.addChildStock( childRootStock.getRegistry( ) );
        }

        @Override
        public boolean addChildStock( StockRegistry< ? extends T > childStockRegistry ) {
            synchronized ( this ) {
                if ( ( childStockRegistry == null ) || this.childStocks.contains( childStockRegistry ) ) {
                    return false;
                }
                this.childStocks.add( childStockRegistry );
                childStockRegistry.addListener( this.childStocksListener );
                return true;
            }
        }

        @Override
        public boolean removeChildStock( Stock< ? extends T > childStock ) {
            return this.removeChildStock( childStock.getRegistry( ) );
        }

        @Override
        public boolean removeChildStock( RootStock< ? extends T > childRootStock ) {
            return this.removeChildStock( childRootStock.getRegistry( ) );
        }

        @Override
        public boolean removeChildStock( StockRegistry< ? extends T > childStockRegistry ) {
            synchronized ( this ) {
                if ( ( childStockRegistry == null ) || !this.childStocks.contains( childStockRegistry ) ) {
                    return false;
                }
                this.childStocks.remove( childStockRegistry );
                childStockRegistry.removeListener( this.childStocksListener );
                return true;
            }
        }

        @Override
        public boolean containsChildStock( Stock< ? extends T > childStock ) {
            return this.containsChildStock( childStock.getRegistry( ) );
        }

        @Override
        public boolean containsChildStock( RootStock< ? extends T > childRootStock ) {
            return this.containsChildStock( childRootStock.getRegistry( ) );
        }

        @Override
        public boolean containsChildStock( StockRegistry< ? extends T > childStockRegistry ) {
            synchronized ( this ) {
                return this.childStocks.contains( childStockRegistry );
            }
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicSuperRootStock< S, T extends S > extends BasicRootStock< T >
                    implements SuperRootStock< S, T > {

        protected Map< StockListener< ? super S >, StockListener< T > > superRootListeners;

        protected StockRegistry< S >                                    superRootRegistry;

        public BasicSuperRootStock( ) {
            this( new ArrayList< StockRegistry< T > >( ) );
        }

        @SuppressWarnings( "unchecked" )
        public BasicSuperRootStock( StockRegistry< T >... childStocks ) {
            this( Arrays.asList( childStocks ) );
        }

        public BasicSuperRootStock( List< StockRegistry< T > > childRegistries ) {
            super( childRegistries );
            this.superRootListeners = new HashMap< StockListener< ? super S >, StockListener< T > >( );
            this.superRootRegistry = new AbstractRootStockRegistry< S >( ) {

                @Override
                protected List< StockRegistry< ? extends S > > _getChildStocks( ) {
                    return new ArrayList< StockRegistry< ? extends S > >( BasicSuperRootStock.this.childStocks );
                }

                @Override
                protected void _addListener( StockListener< ? super S > listener ) {
                    StockListener< T > tmpSubListener = BasicSuperRootStock.this.superRootListeners.get( listener );
                    if ( tmpSubListener == null ) {
                        tmpSubListener = new StockListener< T >( ) {

                            protected StockListener< ? super S > _listener = listener;

                            @Override
                            public void itemAdded( T item ) {
                                this._listener.itemAdded( item );
                            }

                            @Override
                            public void itemRemoved( T item ) {
                                this._listener.itemRemoved( item );
                            }
                        };
                        BasicSuperRootStock.this.rootRegistry.addListener( tmpSubListener );
                        BasicSuperRootStock.this.superRootListeners.put( listener,
                                                                         tmpSubListener );
                    }
                }

                @Override
                protected void _removeListener( StockListener< ? super S > listener ) {
                    StockListener< ? super T > tmpSubListener = BasicSuperRootStock.this.superRootListeners.remove( listener );
                    if ( tmpSubListener != null ) {
                        BasicSuperRootStock.this.rootRegistry.removeListener( tmpSubListener );
                    }
                }
            };
        }

        @Override
        public StockRegistry< S > getSuperRegistry( ) {
            return this.superRootRegistry;
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface PoolStockRegistry< T > extends StockRegistry< T > {
    
        public void acquire( StockListener< ? super T > listener,
                             T item );
    
        public void release( StockListener< ? super T > listener,
                             T item );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface PoolSuperStock< S, T extends S > extends PoolStock< T > {
    
        public PoolStockRegistry< S > getSuperRegistry( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface PoolStock< T > extends StockProvider< T > {
    
        public PoolStockRegistry< T > getRegistry( );
    
        public boolean addStock( Stock< ? extends T > childStock );
    
        public boolean addStock( RootStock< ? extends T > childRootStock );
    
        public boolean addStock( StockRegistry< ? extends T > childStockRegistry );
    
        public boolean removeStock( Stock< ? extends T > childStock );
    
        public boolean removeStock( RootStock< ? extends T > childRootStock );
    
        public boolean removeStock( StockRegistry< ? extends T > childStockRegistry );
    
        public boolean containsStock( Stock< ? extends T > childStock );
    
        public boolean containsStock( RootStock< ? extends T > childRootStock );
    
        public boolean containsStock( StockRegistry< ? extends T > childStockRegistry );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicPoolStock< T > implements PoolStock< T > {

        protected List< StockRegistry< ? extends T > > childStocks;

        protected StockListener< T >                   childStocksListener;

        protected List< StockListener< ? super T > >   rootListeners;

        protected StockRegistry< T >                   rootRegistry;

        @Override
        public List< T > getItems( ) {
            return null;
        }

        @Override
        public int getItemCount( ) {
            return 0;
        }

        @Override
        public PoolStockRegistry< T > getRegistry( ) {
            return null;
        }

        @Override
        public boolean addStock( Stock< ? extends T > childStock ) {
            return false;
        }

        @Override
        public boolean addStock( RootStock< ? extends T > childRootStock ) {
            return false;
        }

        @Override
        public boolean addStock( StockRegistry< ? extends T > childStockRegistry ) {
            return false;
        }

        @Override
        public boolean removeStock( Stock< ? extends T > childStock ) {
            return false;
        }

        @Override
        public boolean removeStock( RootStock< ? extends T > childRootStock ) {
            return false;
        }

        @Override
        public boolean removeStock( StockRegistry< ? extends T > childStockRegistry ) {
            return false;
        }

        @Override
        public boolean containsStock( Stock< ? extends T > childStock ) {
            return false;
        }

        @Override
        public boolean containsStock( RootStock< ? extends T > childRootStock ) {
            return false;
        }

        @Override
        public boolean containsStock( StockRegistry< ? extends T > childStockRegistry ) {
            return false;
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface ConfigurationStockItem {
    
        public String getVersionName( );
    
        public int getVersionMajor( );
    
        public int getVersionMinor( );
    
        public int getVersionRelease( );
    
        public String getVersionString( );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface ConfigurationStockProvider< T extends ConfigurationStockItem > extends StockProvider< T > {
    
        public List< T > getVersions( String name );
    
        public T getNewestVersion( String name );
    
        public T getExactVersion( String name,
                                  int versionMajor,
                                  int versionMinor,
                                  int versionRelease );
    
        public T getExactVersion( String name,
                                  String version );
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface ConfigurationStock< T extends ConfigurationStockItem >
                    extends SuperStock< ConfigurationStockItem, T >, ConfigurationStockProvider< T > {
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static interface ConfigurationRootStock< T extends ConfigurationStockItem >
                    extends SuperRootStock< ConfigurationStockItem, T >, ConfigurationStockProvider< T > {
    }

    /*
     * -----------------------------------------------------------------------
     */
    protected static class AbstractConfigurationStockProvider< T extends ConfigurationStockItem >
                    implements ConfigurationStockProvider< T > {

        protected StockProvider< T > stockProvider;

        protected AbstractConfigurationStockProvider( StockProvider< T > stockProvider ) {
            this.stockProvider = stockProvider;
        }

        @Override
        public List< T > getItems( ) {
            return this.stockProvider.getItems( );
        }

        @Override
        public int getItemCount( ) {
            synchronized ( this ) {
                return this.stockProvider.getItemCount( );
            }
        }

        public List< T > getVersions( String name ) {
            synchronized ( this.stockProvider ) {
                List< T > tmpResult = new ArrayList< T >( );
                for ( T tmpItem : this.getItems( ) ) {
                    if ( tmpItem.getVersionName( ).compareTo( name ) == 0 ) {
                        tmpResult.add( tmpItem );
                    }
                }
                return tmpResult;
            }
        }

        public T getNewestVersion( String name ) {
            synchronized ( this.stockProvider ) {
                T tmpResult = null;
                for ( T tmpItem : this.getItems( ) ) {
                    if ( ( tmpItem.getVersionName( ).compareTo( name ) == 0 )
                         && ( ( tmpResult == null )
                              || ( tmpResult.getVersionMajor( ) < tmpItem.getVersionMajor( ) )
                              || ( tmpResult.getVersionMinor( ) < tmpItem.getVersionMinor( ) )
                              || ( tmpResult.getVersionRelease( ) < tmpItem.getVersionRelease( ) ) ) ) {
                        tmpResult = tmpItem;
                    }
                }
                return tmpResult;
            }
        }

        public T getExactVersion( String name,
                                  int versionMajor,
                                  int versionMinor,
                                  int versionRelease ) {
            synchronized ( this.stockProvider ) {
                T tmpResult = null;
                for ( T tmpItem : this.getItems( ) ) {
                    if ( ( tmpItem.getVersionName( ).compareTo( name ) == 0 )
                         && ( tmpItem.getVersionMajor( ) == versionMajor )
                         && ( tmpItem.getVersionMinor( ) == versionMinor )
                         && ( tmpItem.getVersionRelease( ) == versionRelease ) ) {
                        tmpResult = tmpItem;
                        break;
                    }
                }
                return tmpResult;
            }
        }

        public T getExactVersion( String name,
                                  String version ) {
            synchronized ( this.stockProvider ) {
                T tmpResult = null;
                for ( T tmpItem : this.getItems( ) ) {
                    if ( ( tmpItem.getVersionName( ).compareTo( name ) == 0 )
                         && ( tmpItem.getVersionString( ).compareTo( version ) == 0 ) ) {
                        tmpResult = tmpItem;
                        break;
                    }
                }
                return tmpResult;
            }
        }
    }

    /*
     * -----------------------------------------------------------------------
     */
    public static class BasicConfigurationStock< T extends ConfigurationStockItem >
                    extends BasicSuperStock< ConfigurationStockItem, T > implements ConfigurationStock< T > {

        protected AbstractConfigurationStockProvider< T > stockProvider;

        public BasicConfigurationStock( ) {
            this.stockProvider = new AbstractConfigurationStockProvider< T >( this );
        }

        @Override
        public List< T > getVersions( String name ) {
            return this.stockProvider.getVersions( name );
        }

        @Override
        public T getNewestVersion( String name ) {
            return this.stockProvider.getNewestVersion( name );
        }

        @Override
        public T getExactVersion( String name,
                                  int versionMajor,
                                  int versionMinor,
                                  int versionRelease ) {
            return this.stockProvider.getExactVersion( name,
                                                       versionMajor,
                                                       versionMinor,
                                                       versionRelease );
        }

        @Override
        public T getExactVersion( String name,
                                  String version ) {
            return this.stockProvider.getExactVersion( name,
                                                       version );
        }
    }

    public static class BasicConfigurationRootStock< T extends ConfigurationStockItem >
                    extends BasicSuperRootStock< ConfigurationStockItem, T > implements ConfigurationRootStock< T > {

        protected AbstractConfigurationStockProvider< T > stockProvider;

        public BasicConfigurationRootStock( ) {
            this.stockProvider = new AbstractConfigurationStockProvider< T >( this );
        }

        @Override
        public List< T > getVersions( String name ) {
            return this.stockProvider.getVersions( name );
        }

        @Override
        public T getNewestVersion( String name ) {
            return this.stockProvider.getNewestVersion( name );
        }

        @Override
        public T getExactVersion( String name,
                                  int versionMajor,
                                  int versionMinor,
                                  int versionRelease ) {
            return this.stockProvider.getExactVersion( name,
                                                       versionMajor,
                                                       versionMinor,
                                                       versionRelease );
        }

        @Override
        public T getExactVersion( String name,
                                  String version ) {
            return this.stockProvider.getExactVersion( name,
                                                       version );
        }
    }
}
