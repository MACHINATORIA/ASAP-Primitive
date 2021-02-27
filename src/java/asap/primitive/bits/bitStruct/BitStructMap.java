package asap.primitive.bits.bitStruct;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import asap.primitive.bits.bitStore.BitStoreMap.BitStoreMapException;
import asap.primitive.exception.ExceptionTemplate;

public class BitStructMap {

    @SuppressWarnings( "serial" )
    public static class BitStructMapException extends ExceptionTemplate {

        public BitStructMapException( String messageFormat,
                                      Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }
    }

    public static enum BitStructDataType {
        ZERO,
        RUF,
        Boolean,
        Integer,
        Enumeration,
        Currency,
        Date,
        Time,
        Datetime,
        BitArray,
        ByteArray,
        IntegerArray;
    }

    @XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
    @XmlType( name = "fragment",
              propOrder = { "length",
                            "offset" } )
    public static class BitStructFragmentMap {

        @XmlAttribute( required = true,
                       name = "offset" )
        protected int offset;

        @XmlAttribute( required = true,
                       name = "length" )
        protected int length;

        public BitStructFragmentMap( int offset,
                                     int length ) {
            this.offset = offset;
            this.length = length;
        }

        public int getOffset( ) {
            return this.offset;
        }

        public int getLength( ) {
            return this.length;
        }
    }

    public static abstract class BitStructItemMap {

        protected BitStructRecordMap parentRecord;

        protected int                bitLength;

        protected BitStructDataType  dataType;

        protected BitStructItemMap( BitStructDataType dataType,
                                    int bitLength ) {
            this.dataType = dataType;
            this.bitLength = bitLength;
            this.parentRecord = null;
        }

        protected void checkLength( int length,
                                    int modulo,
                                    int minimum,
                                    int maximum,
                                    String onenessString )
            throws BitStructMapException {
            boolean tmpValidModulo = ( ( length % modulo ) == 0 );
            boolean tmpValidMinimum = ( length >= minimum );
            boolean tmpValidMaximum = ( length <= maximum );
            if ( !tmpValidModulo || !tmpValidMinimum || !tmpValidMaximum ) {
                throw new BitStructMapException( "Tamanho invalido de %d %s para o campo '%s' do tipo '%s' ( %s %d %s ) no registro '%s' do mapa '%s'",
                                                 length,
                                                 onenessString,
                                                 this.getName( ),
                                                 this.dataType.name( ),
                                                 ( ( minimum == maximum ) ? "deve ser"
                                                                          : ( !tmpValidModulo ? "multiplo de"
                                                                                              : ( !tmpValidMinimum ? "no minimo"
                                                                                                                   : "no maximo" ) ) ),
                                                 ( ( minimum == maximum ) ? minimum
                                                                          : ( !tmpValidModulo ? modulo
                                                                                              : ( !tmpValidMinimum ? minimum
                                                                                                                   : maximum ) ) ),
                                                 onenessString,
                                                 this.parentRecord.name,
                                                 this.parentRecord.name );
            }
        }

        protected void checkBitLength( int modulo,
                                       int minimum,
                                       int maximum )
            throws BitStructMapException {
            this.checkLength( this.bitLength,
                              modulo,
                              minimum,
                              maximum,
                              "bits" );
        }

        protected abstract void validate( )
            throws BitStoreMapException;

        public BitStructRecordMap getParentRecord( ) {
            return this.parentRecord;
        }

        public String getPath( ) {
            return String.format( "%s.%s",
                                  this.getParentRecord( ).getPath( ),
                                  this.getName( ) );
        }

        public abstract String getName( );

        public abstract String getDescription( );

        public int getBitlength( ) {
            return this.bitLength;
        }

        public BitStructDataType getDataType( ) {
            return this.dataType;
        }
    }

    public static abstract class BitStructFieldMap extends BitStructItemMap {

        protected String name;

        protected String description;

        protected BitStructFieldMap( String name,
                                     String description,
                                     BitStructDataType dataType,
                                     int bitLength ) {
            super( dataType,
                   bitLength );
            this.name = name;
            this.description = description;
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return this.description;
        }
    }

    public static class BitStructRecordMap {

        protected BitStoreViewMap parentView;

        protected String          name;

        protected String          description;

        protected int             bitLength;

        protected BitStructRecordMap( String name,
                                      int bitLength ) {
            this.name = name;
            this.bitLength = bitLength;
            this.parentView = null;
        }

        public BitStoreViewMap getParentView( ) {
            return parentView;
        }

        public String getPath( ) {
            return String.format( "%s.%s",
                                  this.getParentView( ).getPath( ),
                                  this.getName( ) );
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return this.description;
        }

        public int getBitlength( ) {
            return this.bitLength;
        }
    }

    public static class BitStoreViewMap {

        protected String       name;

        protected String       description;

        protected int          bitLength;

        protected BitStructMap parentMap;

        protected BitStoreViewMap( String name,
                                   int bitLength ) {
            this.name = name;
            this.bitLength = bitLength;
            this.parentMap = null;
        }

        public BitStructMap getParentMap( ) {
            return this.parentMap;
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return this.description;
        }

        public String getPath( ) {
            return this.getName( );
        }

        public int getBitlength( ) {
            return this.bitLength;
        }
    }

    protected String name;

    protected String description;

    public BitStructMap( String name,
                         String description ) {
        this.name = name;
        this.description = description;
    }

    public String getName( ) {
        return this.name;
    }

    public String getDescription( ) {
        return this.description;
    }
}
