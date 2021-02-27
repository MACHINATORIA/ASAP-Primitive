package asap.primitive.database;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import asap.primitive.log.LogService.LogManager;
import asap.primitive.log.LogService.Logger;
import asap.primitive.string.ColumnsStringBuffer;
import asap.primitive.string.StringHelper;

public class DatabaseHelper {

    private static Logger log = LogManager.getLogger( DatabaseHelper.class );
    //
    static {
        try {
            Class.forName( "oracle.jdbc.driver.OracleDriver" ).getDeclaredConstructor( ).newInstance( );
        }
        catch ( Throwable e ) {
            log.exception( e );
            throw new Error( e );
        }
    }

    public final String  baseName;

    protected Connection dbConnection;

    protected Statement  sqlStatement;

    public DatabaseHelper( String jdbcUrl,
                           String jdbcUser,
                           String jdbcPassword )
        throws SQLException {
        this( StringHelper.NULL,
              jdbcUrl,
              jdbcUser,
              jdbcPassword );
    }

    public DatabaseHelper( String baseName,
                           String jdbcUrl,
                           String jdbcUser,
                           String jdbcPassword )
        throws SQLException {
        this.baseName = baseName;
        this.dbConnection = DriverManager.getConnection( jdbcUrl,
                                                         jdbcUser,
                                                         jdbcPassword );
        try {
            this.dbConnection.setAutoCommit( false );
            ResultSet tmpResultSet = this.execute( "SELECT sysdate AS datahoraatual FROM dual " );
            if ( tmpResultSet.next( ) ) {
                Date tmpDate = tmpResultSet.getTimestamp( "datahoraatual" );
                log.debug( "Sysdate em %s: %s",
                           this.baseName,
                           new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ).format( tmpDate ) );
                tmpResultSet.close( );
            }
        }
        catch ( SQLException e ) {
            this.close( );
            throw new SQLException( e );
        }
    }

    public Statement createStatement( )
        throws SQLException {
        return this.dbConnection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                  ResultSet.CONCUR_READ_ONLY /*, ResultSet.CLOSE_CURSORS_AT_COMMIT*/ );
    }

    public PreparedStatement createPreparedStatement( String query )
        throws SQLException {
        return this.dbConnection.prepareStatement( query,
                                                   ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                   ResultSet.CONCUR_READ_ONLY /*, ResultSet.CLOSE_CURSORS_AT_COMMIT*/ );
    }

    public ResultSet execute( String query )
        throws SQLException {
        if ( this.sqlStatement == null ) {
            this.sqlStatement = this.createStatement( );
        }
        return this.sqlStatement.executeQuery( query );
    }

    public void commit( )
        throws SQLException {
        this.dbConnection.commit( );
    }

    public static abstract class AbstractResultSetDumper {

        protected abstract void startDump( int columnCount );

        protected abstract void addHeaderLine( List< String > headerColumns );

        protected abstract void addBodyLine( List< String > bodyColumns );

        protected abstract String getResult( );

        public String dump( ResultSet resultSet,
                            boolean includeHeader,
                            boolean formatNumber )
            throws SQLException {
            ResultSetMetaData tmpMetadata = resultSet.getMetaData( );
            int tmpColumnCount = tmpMetadata.getColumnCount( );
            //
            this.startDump( tmpColumnCount );
            //
            List< String > tmpStringList = new ArrayList< String >( );
            if ( includeHeader ) {
                for ( int tmpColumnIndex = 1; tmpColumnIndex <= tmpColumnCount; tmpColumnIndex++ ) {
                    tmpStringList.add( resultSet.getMetaData( ).getColumnName( tmpColumnIndex ) );
                }
                this.addHeaderLine( tmpStringList );
            }
            resultSet.beforeFirst( );
            SimpleDateFormat tmpDateFormat = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
            while ( resultSet.next( ) ) {
                tmpStringList.clear( );
                for ( int tmpColumnIndex = 1; tmpColumnIndex <= tmpColumnCount; tmpColumnIndex++ ) {
                    try {
                        String tmpColumnValue;
                        switch ( tmpMetadata.getColumnType( tmpColumnIndex ) ) {
                            case Types.DATE:
                                Timestamp tmpTimestamp = resultSet.getTimestamp( tmpColumnIndex );
                                tmpColumnValue = ( tmpTimestamp != null ) ? tmpDateFormat.format( new Date( tmpTimestamp.getTime( ) ) )
                                                                          : null;
                                break;
                            case Types.NUMERIC:
                                BigDecimal tmpBigDecimalValue = resultSet.getBigDecimal( tmpColumnIndex );
                                if ( resultSet.wasNull( ) ) {
                                    tmpColumnValue = "";
                                }
                                else {
                                    int tmpScale = tmpBigDecimalValue.scale( );
                                    String tmpFormat = ( tmpScale <= 0 ) ? formatNumber ? "#,###,###,##0"
                                                                                        : "#########0"
                                                                         : String.format( formatNumber ? "#,###,###,##0.%s"
                                                                                                       : "#########0.%s",
                                                                                          StringHelper.repeatedChar( tmpScale,
                                                                                                                     '0' ) );
                                    double tmpNumericValue = tmpBigDecimalValue.doubleValue( );
                                    tmpColumnValue = new DecimalFormat( tmpFormat ).format( tmpNumericValue );
                                }
                                break;
                            default:
                                tmpColumnValue = resultSet.getString( tmpColumnIndex );
                                break;
                        }
                        tmpStringList.add( ( tmpColumnValue != null ) ? tmpColumnValue
                                                                      : "" );
                    }
                    catch ( Throwable e ) {
                        tmpStringList.add( e.getLocalizedMessage( ) );
                    }
                }
                this.addBodyLine( tmpStringList );
            }
            resultSet.beforeFirst( );
            return this.getResult( );
        }
    }

    public static class ColumnResultSetDumper extends AbstractResultSetDumper {

        protected ColumnsStringBuffer resultBuffer;

        @Override
        protected void startDump( int columnCount ) {
            this.resultBuffer = new ColumnsStringBuffer( columnCount );
        }

        @Override
        protected void addHeaderLine( List< String > headerColumns ) {
            this.resultBuffer.addLine( headerColumns );
        }

        @Override
        protected void addBodyLine( List< String > bodyColumns ) {
            this.resultBuffer.addLine( bodyColumns );
        }

        @Override
        protected String getResult( ) {
            return this.resultBuffer.getResult( 2,
                                                1 );
        }
    }

    public static class TabbedResultSetDumper extends AbstractResultSetDumper {

        protected StringBuilder resultBuffer;

        @Override
        protected void startDump( int columnCount ) {
            this.resultBuffer = new StringBuilder( );
        }

        @Override
        protected void addHeaderLine( List< String > headerColumns ) {
            this.resultBuffer.append( StringHelper.listToSeparated( "\t",
                                                                    headerColumns ) );
            this.resultBuffer.append( "\n" );
        }

        @Override
        protected void addBodyLine( List< String > bodyColumns ) {
            this.resultBuffer.append( StringHelper.listToSeparated( "\t",
                                                                    bodyColumns ) );
            this.resultBuffer.append( "\n" );
        }

        @Override
        protected String getResult( ) {
            return this.resultBuffer.toString( );
        }
    }

    protected static class MethodCall {

        protected Method method;

        public MethodCall( Class< ? > clazz,
                           String methodName,
                           Class< ? >... paramClasses ) {
            try {
                this.method = clazz.getMethod( methodName,
                                               paramClasses );
            }
            catch ( Throwable e ) {
                throw new Error( e );
            }
        }

        public String call( Object object,
                            Object... paramValues ) {
            try {
                Object tmpResult = this.method.invoke( object,
                                                       paramValues );
                return tmpResult.toString( );
            }
            catch ( Throwable e ) {
                throw new Error( e );
            }
        }
    }

    public static String dumpMetadata_Ex( ResultSet resultSet )
        throws SQLException {
        String[ ] tmpMethodNames = new String[ ] { "getCatalogName",
                                                   "getColumnClassName",
                                                   "getColumnDisplaySize",
                                                   "getColumnLabel",
                                                   "getColumnName",
                                                   // 5
                                                   "getColumnType",
                                                   "getColumnTypeName",
                                                   "getPrecision",
                                                   "getScale",
                                                   "getSchemaName",
                                                   // 10
                                                   "getTableName",
                                                   "isAutoIncrement",
                                                   "isCaseSensitive",
                                                   "isCurrency",
                                                   "isDefinitelyWritable",
                                                   // 15
                                                   "isNullable",
                                                   "isReadOnly",
                                                   "isSearchable",
                                                   "isSigned",
                                                   "isWritable" };
        ResultSetMetaData tmpMetadata = resultSet.getMetaData( );
        List< String > tmpColumnHeaders = new ArrayList< String >( );
        List< MethodCall > tmpMethodCalls = new ArrayList< MethodCall >( );
        for ( String tmpMethodName : tmpMethodNames ) {
            tmpColumnHeaders.add( tmpMethodName.replaceAll( "^(is|get)(Column)?",
                                                            "" ) );
            tmpMethodCalls.add( new MethodCall( ResultSetMetaData.class,
                                                tmpMethodName,
                                                Integer.TYPE ) );
        }
        ColumnsStringBuffer resultBuffer = new ColumnsStringBuffer( tmpColumnHeaders );
        List< String > tmpValueList = new ArrayList< String >( );
        int tmpColumnCount = tmpMetadata.getColumnCount( );
        for ( int tmpColumnIndex = 1; tmpColumnIndex <= tmpColumnCount; tmpColumnIndex++ ) {
            tmpValueList.clear( );
            for ( MethodCall tmpMethodCall : tmpMethodCalls ) {
                tmpValueList.add( tmpMethodCall.call( tmpMetadata,
                                                      tmpColumnIndex ) );
            }
            resultBuffer.addLine( tmpValueList );
        }
        return resultBuffer.getResult( 2,
                                       2 );
    }

    public static String dumpMetadata( ResultSet resultSet )
        throws SQLException {
        ColumnsStringBuffer resultBuffer = new ColumnsStringBuffer( "CatalogName",
                                                                    "ClassName",
                                                                    "DisplaySize",
                                                                    "Label",
                                                                    "Name",
                                                                    // 5
                                                                    "Type",
                                                                    "TypeName",
                                                                    "Precision",
                                                                    "Scale",
                                                                    "SchemaName",
                                                                    // 10
                                                                    "TableName",
                                                                    "AutoIncrement",
                                                                    "CaseSensitive",
                                                                    "Currency",
                                                                    "DefinitelyWritable",
                                                                    // 15
                                                                    "Nullable",
                                                                    "ReadOnly",
                                                                    "Searchable",
                                                                    "Signed",
                                                                    "Writable" );
        ResultSetMetaData tmpMetadata = resultSet.getMetaData( );
        int tmpColumnCount = tmpMetadata.getColumnCount( );
        for ( int tmpColumnIndex = 1; tmpColumnIndex <= tmpColumnCount; tmpColumnIndex++ ) {
            resultBuffer.addLine( tmpMetadata.getCatalogName( tmpColumnIndex ),
                                  tmpMetadata.getColumnClassName( tmpColumnIndex ),
                                  Integer.toString( tmpMetadata.getColumnDisplaySize( tmpColumnIndex ) ),
                                  tmpMetadata.getColumnLabel( tmpColumnIndex ),
                                  tmpMetadata.getColumnName( tmpColumnIndex ),
                                  // 5
                                  Integer.toString( tmpMetadata.getColumnType( tmpColumnIndex ) ),
                                  tmpMetadata.getColumnTypeName( tmpColumnIndex ),
                                  Integer.toString( tmpMetadata.getPrecision( tmpColumnIndex ) ),
                                  Integer.toString( tmpMetadata.getScale( tmpColumnIndex ) ),
                                  tmpMetadata.getSchemaName( tmpColumnIndex ),
                                  // 10
                                  tmpMetadata.getTableName( tmpColumnIndex ),
                                  Boolean.toString( tmpMetadata.isAutoIncrement( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isCaseSensitive( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isCurrency( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isDefinitelyWritable( tmpColumnIndex ) ),
                                  // 15
                                  Integer.toString( tmpMetadata.isNullable( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isReadOnly( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isSearchable( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isSigned( tmpColumnIndex ) ),
                                  Boolean.toString( tmpMetadata.isWritable( tmpColumnIndex ) )
            // 20
            );
        }
        return resultBuffer.getResult( 2,
                                       2 );
    }

    public static String dumpResultSet( ResultSet resultSet )
        throws SQLException {
        return dumpResultSet( resultSet,
                              true,
                              true );
    }

    public static String dumpResultSet( ResultSet resultSet,
                                        boolean includeHeader,
                                        boolean formatNumber )
        throws SQLException {
        return new ColumnResultSetDumper( ).dump( resultSet,
                                                  includeHeader,
                                                  formatNumber );
    }

    public void close( ) {
        if ( this.dbConnection != null ) {
            try {
                this.dbConnection.rollback( );
            }
            catch ( SQLException e ) {
            }
            if ( this.sqlStatement != null ) {
                try {
                    this.sqlStatement.close( );
                }
                catch ( SQLException e ) {
                }
                this.sqlStatement = null;
            }
            try {
                this.dbConnection.close( );
            }
            catch ( SQLException e ) {
            }
            this.dbConnection = null;
        }
    }
}
