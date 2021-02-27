package asap.primitive.pattern;

import asap.primitive.pattern.StockPattern.BasicRootStock;
import asap.primitive.pattern.StockPattern.BasicStock;
import asap.primitive.pattern.StockPattern.RootStock;
import asap.primitive.pattern.StockPattern.Stock;
import asap.primitive.pattern.StockPattern.StockRegistry;

public class FactoryPattern {

    public static interface ObjectModel {
    }

    public static interface FactoryModel< E extends Throwable, O > {

        public boolean isStarted( );

        public void start( )
            throws E;

        public void stop( )
            throws E;

        public void poll( )
            throws E;

        public StockRegistry< O > getRegistry( );
    }

    public static abstract class ManagerModel< FE extends Throwable, FO extends ObjectModel, F extends FactoryModel< FE, FO > > {

        @SuppressWarnings( "unchecked" )
        protected ManagerModel( F... factories ) {
            this.factoryStock = new BasicStock< F >( );
            this.objectRootStock = new BasicRootStock< FO >( );
            for ( F tmpFactory : factories ) {
                this.addFactory( tmpFactory );
            }
        }

        public void start( )
            throws FE {
            for ( F tmpFactory : this.factoryStock.getItems( ) ) {
                tmpFactory.start( );
            }
        }

        public void stop( )
            throws FE {
            for ( F tmpFactory : this.factoryStock.getItems( ) ) {
                tmpFactory.stop( );
            }
        }

        @SuppressWarnings( "unchecked" )
        public void addFactory( F... factories ) {
            for ( F tmpFactory : factories ) {
                this.factoryStock.addItem( tmpFactory );
                this.objectRootStock.addChildStock( tmpFactory.getRegistry( ) );
            }
        }

        @SuppressWarnings( "unchecked" )
        public void removeFactory( F... factories ) {
            for ( F tmpFactory : factories ) {
                this.objectRootStock.removeChildStock( tmpFactory.getRegistry( ) );
                this.factoryStock.removeItem( tmpFactory );
            }
        }

        public void poll( )
            throws FE {
            synchronized ( this ) {
                for ( F tmpFactory : this.factoryStock.getItems( ) ) {
                    tmpFactory.poll( );
                }
            }
        }

        public StockRegistry< F > getFactoryRegistry( ) {
            return this.factoryStock.getRegistry( );
        }

        public StockRegistry< FO > getObjectRegistry( ) {
            return this.objectRootStock.getRegistry( );
        }

        protected Stock< F >      factoryStock;

        protected RootStock< FO > objectRootStock;
    }
}
