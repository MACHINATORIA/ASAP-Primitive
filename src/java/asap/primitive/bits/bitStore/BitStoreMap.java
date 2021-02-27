package asap.primitive.bits.bitStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import asap.primitive.bits.BitHelper;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreBitArrayFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreBooleanFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreByteArrayFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreCurrencyFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreDateFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreEnumerationData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreIntegerArrayFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreIntegerFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreItemData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreRUFFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreRecordData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreTimeFieldData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreZeroedData;
import asap.primitive.bits.bitStore.BitStoreHelper.BitStoreViewMapBuilder;
import asap.primitive.pattern.StockPattern.ConfigurationStockItem;
import asap.primitive.process.ProcessHelper;
import asap.primitive.string.ColumnsStringBuffer;
import asap.primitive.string.IndentedStringBuilder;

@XmlRootElement( name = "bitStoreMap",
                 namespace = "##default" )
@XmlType( name = "map",
          propOrder = { "name",
                        "description",
                        "versionMajor",
                        "versionMinor",
                        "versionRelease",
                        "mainViewName",
                        "mainViewDescription",
                        "isLittleEndianBits",
                        "isLittleEndianBytes",
                        "records",
                        "alternativeViews" } )
@XmlAccessorType( XmlAccessType.NONE )
public final class BitStoreMap implements ConfigurationStockItem {

    @SuppressWarnings( "serial" )
    public static class BitStoreMapException extends BitStoreException {

        public BitStoreMapException( Throwable cause ) {
            super( cause );
        }

        public BitStoreMapException( String messageFormat,
                                     Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreMapException( Throwable cause,
                                     String messageFormat,
                                     Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    public static enum BitStoreItemType {
        ZERO,
        RUF,
        Boolean,
        Integer,
        Currency,
        Date,
        Time,
        BitArray,
        ByteArray,
        IntegerArray,
        Enumeration;
    }

    /*
    * 
    */
    @XmlTransient
    @XmlAccessorType( XmlAccessType.NONE )
    public static abstract class BitStoreItemMap {

        @XmlTransient
        protected BitStoreItemType        type;

        @XmlAttribute( required = true )
        protected int                     bitLength;

        @XmlElement( required = true,
                     name = "piece" )
        protected BitStoreViewPieceMap[ ] mainViewPieces;

        @XmlTransient
        protected BitStoreViewFieldMap    mainViewField;

        @XmlTransient
        protected BitStoreRecordMap       parentRecord;

        protected BitStoreItemMap( BitStoreItemType type,
                                   int bitLength,
                                   BitStoreViewPieceMap... mainViewPieces ) {
            this.type = type;
            this.bitLength = bitLength;
            this.mainViewPieces = mainViewPieces;
            this.mainViewField = null;
            this.parentRecord = null;
        }

        protected BitStoreItemMap( BitStoreItemType type ) {
            this.type = type;
            this.bitLength = ( -1 );
            this.mainViewPieces = null;
        }

        protected BitStoreViewFieldMap getMainViewField( ) {
            return this.mainViewField;
        }

        protected BitStoreViewPieceMap[ ] getMainViewPieces( ) {
            return Arrays.copyOf( this.mainViewPieces,
                                  this.mainViewPieces.length );
        }

        protected String[ ][ ] getExtraAttributes( ) {
            return new String[ ][ ] { };
        }

        protected void checkLength( int length,
                                    int modulo,
                                    int minimum,
                                    int maximum,
                                    String onenessString )
            throws BitStoreMapException {
            boolean tmpValidModulo = ( ( length % modulo ) == 0 );
            boolean tmpValidMinimum = ( length >= minimum );
            boolean tmpValidMaximum = ( length <= maximum );
            if ( !tmpValidModulo || !tmpValidMinimum || !tmpValidMaximum ) {
                throw new BitStoreMapException( "Tamanho invalido de %d %s para o campo '%s' do tipo '%s' ( %s %d %s ) no registro '%s' do mapa '%s'",
                                                length,
                                                onenessString,
                                                this.getName( ),
                                                this.type.name( ),
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
            throws BitStoreMapException {
            this.checkLength( this.bitLength,
                              modulo,
                              minimum,
                              maximum,
                              "bits" );
        }

        protected void engage( BitStoreRecordMap parentRecordMap )
            throws BitStoreMapException {
            this.parentRecord = parentRecordMap;
            this.mainViewField = new BitStoreViewFieldMap( this.bitLength,
                                                           this.getName( ),
                                                           this.mainViewPieces );
        }

        protected abstract void validate( )
            throws BitStoreMapException;

        protected abstract BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                              BitStoreRecordData parentRecordData );

        public abstract String getName( );

        public abstract String getDescription( );

        public BitStoreItemType getType( ) {
            return this.type;
        }

        public String getPath( ) {
            return String.format( "%s.%s.%s",
                                  this.parentRecord.mainViewRecord.parentView.getName( ),
                                  this.parentRecord.getName( ),
                                  this.getName( ) );
        }

        public int getBitLength( ) {
            return this.bitLength;
        }

        public int getByteLength( ) {
            return BitHelper.getBitArrayBufferLength( this.bitLength );
        }

        public BitStoreRecordMap getParentRecord( ) {
            return this.parentRecord;
        }

        public int getIndex( ) {
            return this.mainViewField.index;
        }

        @Override
        public String toString( ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            tmpResult.append( "%s {",
                              this.getClass( ).getSimpleName( ) );
            //
            ColumnsStringBuffer tmpAttributesString = new ColumnsStringBuffer( 0,
                                                                               0 );
            tmpAttributesString.addLine( "name",
                                         this.getName( ) );
            String tmpDescription = this.getDescription( );
            if ( tmpDescription.length( ) > 0 ) {
                tmpAttributesString.addLine( "description",
                                             tmpDescription );
            }
            tmpAttributesString.addLine( "bitLength:",
                                         String.format( "%d",
                                                        this.bitLength ) );
            for ( String[ ] tmpExtraAttribute : this.getExtraAttributes( ) ) {
                tmpAttributesString.addLine( tmpExtraAttribute );
            }
            tmpResult.appendIndented( tmpAttributesString.getResult( 4,
                                                                     2 ) );
            //
            tmpResult.append( "}" );
            return tmpResult.getResult( );
        }
    }

    /*
     * 
     */
    @XmlType( name = "ruf_f",
              propOrder = { "mainViewPieces",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreRUFMap extends BitStoreItemMap {

        public static final int MIN_BIT_LENGTH      = 1;

        public static final int MAX_BIT_LENGTH      = 256;

        protected static String DEFAULT_DESCRIPTION = "Reservado para uso futuro";

        public BitStoreRUFMap( int bitLength,
                               BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.RUF,
                   bitLength,
                   mainViewPieces );
        }

        protected BitStoreRUFMap( ) {
            super( BitStoreItemType.RUF );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreRUFMap.MIN_BIT_LENGTH,
                                 BitStoreRUFMap.MAX_BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreRUFFieldData( bitStoreViewFieldMap,
                                             parentRecordData );
        }

        @Override
        public String getName( ) {
            return String.format( "RUF_%d",
                                  this.mainViewPieces[ 0 ].offset );
        }

        @Override
        public String getDescription( ) {
            return BitStoreRUFMap.DEFAULT_DESCRIPTION;
        }
    }

    /*
    * 
    */
    @XmlTransient
    @XmlAccessorType( XmlAccessType.NONE )
    public static abstract class BitStoreFieldMap extends BitStoreItemMap {

        @XmlAttribute( required = true )
        protected String name;

        @XmlAttribute( required = false )
        protected String description;

        protected BitStoreFieldMap( BitStoreItemType type,
                                    int bitLength,
                                    String name,
                                    String description,
                                    BitStoreViewPieceMap... mainViewPieces ) {
            super( type,
                   bitLength,
                   mainViewPieces );
            this.name = name;
            this.description = description;
        }

        protected BitStoreFieldMap( BitStoreItemType type ) {
            super( type );
            this.name = null;
            this.description = null;
        }

        protected void checkArrayLength( int arrayLength,
                                         int minimum,
                                         int maximum )
            throws BitStoreMapException {
            this.checkLength( arrayLength,
                              1,
                              minimum,
                              maximum,
                              "elementos" );
        }

        @Override
        public String getName( ) {
            return this.name;
        }

        @Override
        public String getDescription( ) {
            return ( this.description != null ) ? this.description
                                                : "";
        }
    }

    /*
     * 
     */
    @XmlType( name = "bool",
              propOrder = { "mainViewPieces",
                            "falseString",
                            "trueString",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreBooleanFieldMap extends BitStoreFieldMap {

        public static final int    BIT_LENGTH           = 1;

        public static final String DEFAULT_TRUE_STRING  = "True";

        public static final String DEFAULT_FALSE_STRING = "False";

        @XmlAttribute( required = false )
        protected String           trueString;

        @XmlAttribute( required = false )
        protected String           falseString;

        public BitStoreBooleanFieldMap( int bitLength,
                                        String name,
                                        String description,
                                        String trueString,
                                        String falseString,
                                        BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Boolean,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
            this.trueString = trueString;
            this.falseString = falseString;
        }

        protected BitStoreBooleanFieldMap( ) {
            super( BitStoreItemType.Boolean );
            this.trueString = null;
            this.falseString = null;
        }

        @Override
        protected String[ ][ ] getExtraAttributes( ) {
            String[ ] tmpTrueString = new String[ ] { "trueString",
                                                      this.trueString };
            String[ ] tmpFalseString = new String[ ] { "falseString",
                                                       this.falseString };
            if ( this.trueString == null ) {
                if ( this.falseString == null ) {
                    return new String[ ][ ] { };
                }
                else {
                    return new String[ ][ ] { tmpFalseString };
                }
            }
            else {
                if ( this.falseString == null ) {
                    return new String[ ][ ] { tmpTrueString };
                }
            }
            return new String[ ][ ] { tmpTrueString,
                                      tmpFalseString };
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( BitStoreBooleanFieldMap.BIT_LENGTH,
                                 BitStoreBooleanFieldMap.BIT_LENGTH,
                                 BitStoreBooleanFieldMap.BIT_LENGTH );
            if ( ( this.trueString != null )
                 && ( this.falseString != null )
                 && ( this.trueString.compareToIgnoreCase( this.falseString ) == 0 ) ) {
                throw new BitStoreMapException( "Strings identicas para TRUE e FALSE ( %s ) para o campo '%s' do tipo '%s' no registro '%s' do mapa '%s'",
                                                String.format( ( this.trueString.compareTo( this.falseString ) == 0 ) ? "'%s'"
                                                                                                                      : "'%s' e '%s'",
                                                               this.trueString,
                                                               this.falseString ),
                                                this.name,
                                                BitStoreItemType.Boolean.name( ),
                                                this.parentRecord.name,
                                                this.parentRecord.getParentMap( ).name );
            }
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreBooleanFieldData( this,
                                                 bitStoreViewFieldMap,
                                                 parentRecordData );
        }

        public String getTrueString( String defaultTrueString ) {
            return ( this.trueString != null ) ? this.trueString
                                               : defaultTrueString;
        }

        public String getTrueString( ) {
            return this.getTrueString( BitStoreBooleanFieldMap.DEFAULT_TRUE_STRING );
        }

        public String getFalseString( String defaultFalseString ) {
            return ( this.falseString != null ) ? this.falseString
                                                : defaultFalseString;
        }

        public String getFalseString( ) {
            return this.getFalseString( BitStoreBooleanFieldMap.DEFAULT_FALSE_STRING );
        }
    }

    /*
     * 
     */
    @XmlType( name = "int",
              propOrder = { "mainViewPieces",
                            "defaultBase",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreIntegerFieldMap extends BitStoreFieldMap {

        public static final int MIN_BIT_LENGTH = 2;

        public static final int MAX_BIT_LENGTH = 64;

        @XmlAttribute( required = false )
        protected Integer       defaultBase;

        public BitStoreIntegerFieldMap( int bitLength,
                                        String name,
                                        String description,
                                        Integer defaultBase,
                                        BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Integer,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
            this.defaultBase = defaultBase;
        }

        protected BitStoreIntegerFieldMap( int bitLength,
                                           String name,
                                           String description,
                                           BitStoreViewPieceMap... mainViewPieces ) {
            this( bitLength,
                  name,
                  description,
                  null,
                  mainViewPieces );
        }

        protected BitStoreIntegerFieldMap( ) {
            super( BitStoreItemType.Integer );
            this.defaultBase = null;
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreIntegerFieldMap.MIN_BIT_LENGTH,
                                 BitStoreIntegerFieldMap.MAX_BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreIntegerFieldData( bitStoreViewFieldMap,
                                                 parentRecordData );
        }

        public Integer getDefaultBase( ) {
            return ( this.defaultBase == null ) ? 10
                                                : this.defaultBase;
        }
    }

    /*
     * 
     */
    @XmlType( name = "date",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreDateFieldMap extends BitStoreFieldMap {

        public static final int BIT_LENGTH = 16;

        public BitStoreDateFieldMap( int bitLength,
                                     String name,
                                     String description,
                                     BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Date,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
        }

        protected BitStoreDateFieldMap( ) {
            super( BitStoreItemType.Date );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreDateFieldMap.BIT_LENGTH,
                                 BitStoreDateFieldMap.BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreDateFieldData( bitStoreViewFieldMap,
                                              parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "time",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreTimeFieldMap extends BitStoreFieldMap {

        public static final int BIT_LENGTH = 11;

        public BitStoreTimeFieldMap( int bitLength,
                                     String name,
                                     String description,
                                     BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Time,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
        }

        protected BitStoreTimeFieldMap( ) {
            super( BitStoreItemType.Time );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreTimeFieldMap.BIT_LENGTH,
                                 BitStoreTimeFieldMap.BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreTimeFieldData( bitStoreViewFieldMap,
                                              parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "enum",
              propOrder = { "mainViewPieces",
                            "items",
                            "description",
                            "name",
                            "bitLength",
                            "inverted" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreEnumerationFieldMap extends BitStoreFieldMap {

        public static final int MIN_BIT_LENGTH = 1;

        public static final int MAX_BIT_LENGTH = 64;

        @XmlAttribute( required = false )
        protected Boolean       inverted;

        @XmlType( name = "item",
                  propOrder = { "name",
                                "value" } )
        public static class BitStoreEnumerationFieldItemMap {

            @XmlAttribute( required = true )
            protected int    value;

            @XmlAttribute( required = true )
            protected String name;

            public BitStoreEnumerationFieldItemMap( int value,
                                                    String name ) {
                this.value = value;
                this.name = name;
            }

            protected BitStoreEnumerationFieldItemMap( ) {
                this.value = ( -1 );
                this.name = null;
            }

            public int getValue( ) {
                return this.value;
            }

            public String getName( ) {
                return this.name;
            }
        }

        @XmlElement( required = true,
                     name = "item" )
        protected BitStoreEnumerationFieldItemMap[ ] items;

        public BitStoreEnumerationFieldMap( int bitLength,
                                            String name,
                                            String description,
                                            Boolean inverted,
                                            BitStoreEnumerationFieldItemMap[ ] items,
                                            BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Enumeration,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
            this.items = items;
            this.inverted = inverted;
        }

        protected BitStoreEnumerationFieldMap( int bitLength,
                                               String name,
                                               String description,
                                               BitStoreEnumerationFieldItemMap[ ] items,
                                               BitStoreViewPieceMap... mainViewPieces ) {
            this( bitLength,
                  name,
                  description,
                  null,
                  items,
                  mainViewPieces );
        }

        protected BitStoreEnumerationFieldMap( ) {
            super( BitStoreItemType.Enumeration );
            this.items = null;
            this.inverted = null;
        }

        public BitStoreEnumerationFieldItemMap[ ] getItems( ) {
            return Arrays.copyOf( this.items,
                                  this.items.length );
        }

        @Override
        protected String[ ][ ] getExtraAttributes( ) {
            ColumnsStringBuffer tmpAttributes = new ColumnsStringBuffer( 0,
                                                                         0 );
            tmpAttributes.addLine( "value",
                                   "name" );
            tmpAttributes.addSeparator( '-' );
            for ( BitStoreEnumerationFieldItemMap tmpItem : this.items ) {
                tmpAttributes.addLine( Integer.toString( tmpItem.value ),
                                       tmpItem.name );
            }
            return new String[ ][ ] { { "items:" },
                                      { tmpAttributes.getResult( 2,
                                                                 2 ) } };
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreEnumerationFieldMap.MIN_BIT_LENGTH,
                                 BitStoreEnumerationFieldMap.MAX_BIT_LENGTH );
            if ( ( 1 << this.bitLength ) < this.items.length ) {
                throw new BitStoreMapException( "Tamanho de %d bits insuficiente para o campo '%s' do tipo '%s' com %d items ( deve ser no minimo %d bits ) no registro '%s' do mapa '%s'",
                                                this.bitLength,
                                                this.name,
                                                BitStoreItemType.Enumeration.name( ),
                                                this.items.length,
                                                Math.ceil( Math.log( this.items.length ) / Math.log( 2 ) ),
                                                this.parentRecord.name,
                                                this.parentRecord.getParentMap( ).name );
            }
            for ( int tmpOuterItemIndex = 0; tmpOuterItemIndex < this.items.length; tmpOuterItemIndex++ ) {
                BitStoreEnumerationFieldItemMap tmpOuterItemMap = this.items[ tmpOuterItemIndex ];
                for ( int tmpInnerItemIndex = ( tmpOuterItemIndex
                                                + 1 ); tmpInnerItemIndex < this.items.length; tmpInnerItemIndex++ ) {
                    BitStoreEnumerationFieldItemMap tmpInnerItemMap = this.items[ tmpInnerItemIndex ];
                    if ( tmpOuterItemMap.value == tmpInnerItemMap.value ) {
                        throw new BitStoreMapException( "Valor '%d' duplicado nos itens #%d e #%d do campo '%s' no tipo '%s' no registro '%s' do mapa '%s'",
                                                        tmpOuterItemMap.value,
                                                        tmpOuterItemIndex,
                                                        tmpInnerItemIndex,
                                                        this.name,
                                                        BitStoreItemType.Enumeration.name( ),
                                                        this.parentRecord.name,
                                                        this.parentRecord.getParentMap( ).name );
                    }
                    if ( tmpOuterItemMap.name.compareTo( tmpInnerItemMap.name ) == 0 ) {
                        throw new BitStoreMapException( "Nome '%s' duplicado nos itens #%d e #%d do campo '%s' no tipo '%s' no registro '%s' do mapa '%s'",
                                                        tmpOuterItemMap.name,
                                                        tmpOuterItemIndex,
                                                        tmpInnerItemIndex,
                                                        this.name,
                                                        BitStoreItemType.Enumeration.name( ),
                                                        this.parentRecord.name,
                                                        this.parentRecord.getParentMap( ).name );
                    }
                }
            }
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreEnumerationData( bitStoreViewFieldMap,
                                                parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "cur",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreCurrencyFieldMap extends BitStoreFieldMap {

        public static final int MIN_BIT_LENGTH = 10;

        public static final int MAX_BIT_LENGTH = 64;

        public BitStoreCurrencyFieldMap( int bitLength,
                                         String name,
                                         String description,
                                         BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.Currency,
                   bitLength,
                   name,
                   description,
                   mainViewPieces );
        }

        protected BitStoreCurrencyFieldMap( ) {
            super( BitStoreItemType.Currency );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkBitLength( 1,
                                 BitStoreCurrencyFieldMap.MIN_BIT_LENGTH,
                                 BitStoreCurrencyFieldMap.MAX_BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreCurrencyFieldData( bitStoreViewFieldMap,
                                                  parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "bits",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreBitArrayFieldMap extends BitStoreFieldMap {

        public static final int MIN_ARRAY_LENGTH = 2;

        public static final int MAX_ARRAY_LENGTH = 256;

        public BitStoreBitArrayFieldMap( int arrayLength,
                                         String name,
                                         String description,
                                         BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.BitArray,
                   arrayLength,
                   name,
                   description,
                   mainViewPieces );
        }

        protected BitStoreBitArrayFieldMap( ) {
            super( BitStoreItemType.BitArray );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkArrayLength( this.bitLength,
                                   BitStoreBitArrayFieldMap.MIN_ARRAY_LENGTH,
                                   BitStoreBitArrayFieldMap.MAX_ARRAY_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreBitArrayFieldData( bitStoreViewFieldMap,
                                                  parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "bytes",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "arrayLength",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreByteArrayFieldMap extends BitStoreFieldMap {

        public static final int MIN_ARRAY_LENGTH = 2;

        public static final int MAX_ARRAY_LENGTH = 64;

        @XmlAttribute( required = true )
        protected int           arrayLength;

        public BitStoreByteArrayFieldMap( int arrayLength,
                                          String name,
                                          String description,
                                          BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.ByteArray,
                   ( arrayLength * Byte.SIZE ),
                   name,
                   description,
                   mainViewPieces );
            this.arrayLength = arrayLength;
        }

        protected BitStoreByteArrayFieldMap( ) {
            super( BitStoreItemType.ByteArray );
            this.arrayLength = ( -1 );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            this.checkArrayLength( this.arrayLength,
                                   BitStoreByteArrayFieldMap.MIN_ARRAY_LENGTH,
                                   BitStoreByteArrayFieldMap.MAX_ARRAY_LENGTH );
            this.checkBitLength( Byte.SIZE,
                                 ( this.arrayLength * Byte.SIZE ),
                                 ( this.arrayLength * Byte.SIZE ) );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreByteArrayFieldData( bitStoreViewFieldMap,
                                                   parentRecordData );
        }
    }

    /*
     * 
     */
    @XmlType( name = "ints",
              propOrder = { "mainViewPieces",
                            "description",
                            "name",
                            "arrayLength",
                            "integerLength",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreIntegerArrayFieldMap extends BitStoreFieldMap {

        public static final int MIN_INTEGER_LENGTH   = 2;

        public static final int MAX_INTEGER_LENGTH   = 64;

        public static final int MIN_ARRAY_LENGTH     = 2;

        public static final int MAX_ARRAY_LENGTH     = 64;

        public static final int MAX_TOTAL_BIT_LENGTH = 256;

        @XmlAttribute( required = true )
        protected int           integerLength;

        @XmlAttribute( required = true )
        protected int           arrayLength;

        public BitStoreIntegerArrayFieldMap( int integerLength,
                                             int arrayLength,
                                             String name,
                                             String description,
                                             BitStoreViewPieceMap... mainViewPieces ) {
            super( BitStoreItemType.IntegerArray,
                   ( arrayLength * integerLength ),
                   name,
                   description,
                   mainViewPieces );
            this.integerLength = integerLength;
            this.arrayLength = arrayLength;
        }

        protected BitStoreIntegerArrayFieldMap( ) {
            super( BitStoreItemType.IntegerArray );
            this.integerLength = ( -1 );
            this.arrayLength = ( -1 );
        }

        @Override
        protected String[ ][ ] getExtraAttributes( ) {
            return new String[ ][ ] { new String[ ] { "integerLength",
                                                      Integer.toString( this.integerLength ) },
                                      new String[ ] { "arrayLength",
                                                      Integer.toString( this.arrayLength ) } };
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
            if ( ( this.integerLength < BitStoreIntegerArrayFieldMap.MIN_INTEGER_LENGTH )
                 || ( this.integerLength > BitStoreIntegerArrayFieldMap.MAX_INTEGER_LENGTH ) ) {
                throw new BitStoreMapException( "Tamanho inválido de %d bits para os elementos do campo '%s' do tipo '%s' no registro '%s' do mapa '%s'",
                                                this.integerLength,
                                                this.name,
                                                this.type.name( ),
                                                this.parentRecord.name,
                                                this.parentRecord.getParentMap( ).name );
            }
            this.checkArrayLength( this.arrayLength,
                                   BitStoreIntegerArrayFieldMap.MIN_ARRAY_LENGTH,
                                   BitStoreIntegerArrayFieldMap.MAX_ARRAY_LENGTH );
            this.checkBitLength( ( this.integerLength * this.arrayLength ),
                                 1,
                                 BitStoreIntegerArrayFieldMap.MAX_TOTAL_BIT_LENGTH );
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreViewFieldMap bitStoreViewFieldMap,
                                                     BitStoreRecordData parentRecordData ) {
            return new BitStoreIntegerArrayFieldData( this,
                                                      bitStoreViewFieldMap,
                                                      parentRecordData );
        }

        public int getIntegerLength( ) {
            return integerLength;
        }

        public int getArrayLength( ) {
            return arrayLength;
        }
    }

    @XmlType( name = "record",
              propOrder = { "fields",
                            "description",
                            "name",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreRecordMap {

        @XmlAttribute( required = true )
        protected int                   bitLength;

        @XmlAttribute( required = true )
        protected String                name;

        @XmlAttribute( required = true )
        protected String                description;

        @XmlAttribute( required = true )
        protected int                   dataType;

        @XmlElements( { @XmlElement( required = true,
                                     name = "rufField",
                                     type = BitStoreRUFMap.class ),
                        @XmlElement( required = true,
                                     name = "booleanField",
                                     type = BitStoreBooleanFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "integerField",
                                     type = BitStoreIntegerFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "currencyField",
                                     type = BitStoreCurrencyFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "dateField",
                                     type = BitStoreDateFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "timeField",
                                     type = BitStoreTimeFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "bitArrayField",
                                     type = BitStoreBitArrayFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "byteArrayField",
                                     type = BitStoreByteArrayFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "integerArrayField",
                                     type = BitStoreIntegerArrayFieldMap.class ),
                        @XmlElement( required = true,
                                     name = "enumerationField",
                                     type = BitStoreEnumerationFieldMap.class ) } )
        protected BitStoreItemMap[ ]    fields;

        @XmlTransient
        protected BitStoreMap           parentMap;

        @XmlTransient
        protected BitStoreViewRecordMap mainViewRecord;

        public BitStoreRecordMap( int bitLength,
                                  String name,
                                  String description,
                                  int dataType,
                                  BitStoreItemMap... fields ) {
            this.bitLength = bitLength;
            this.name = name;
            this.description = description;
            this.dataType = dataType;
            this.fields = fields;
            this.parentMap = null;
            this.mainViewRecord = null;
        }

        protected BitStoreRecordMap( )
            throws BitStoreMapException {
            this.bitLength = ( -1 );
            this.name = null;
            this.description = null;
            this.dataType = ( -1 );
            this.fields = null;
        }

        protected void engage( BitStoreMap parentStoreMap )
            throws BitStoreMapException {
            this.parentMap = parentStoreMap;
            BitStoreViewFieldMap[ ] tmpMainViewItems = new BitStoreViewFieldMap[ this.fields.length ];
            for ( int tmpIndex = 0; tmpIndex < tmpMainViewItems.length; tmpIndex++ ) {
                this.fields[ tmpIndex ].engage( this );
                tmpMainViewItems[ tmpIndex ] = this.fields[ tmpIndex ].getMainViewField( );
            }
            this.mainViewRecord = new BitStoreViewRecordMap( this.bitLength,
                                                             this.name,
                                                             this.description,
                                                             this.dataType,
                                                             tmpMainViewItems );
        }

        protected void validate( )
            throws BitStoreMapException {
            int tmpFieldsBitLength = 0;
            for ( int tmpOuterFieldIndex = 0; tmpOuterFieldIndex < this.fields.length; tmpOuterFieldIndex++ ) {
                BitStoreItemMap tmpOuterFieldMap = this.fields[ tmpOuterFieldIndex ];
                for ( int tmpInnerFieldIndex = ( tmpOuterFieldIndex
                                                 + 1 ); tmpInnerFieldIndex < this.fields.length; tmpInnerFieldIndex++ ) {
                    BitStoreItemMap tmpInnerFieldMap = this.fields[ tmpInnerFieldIndex ];
                    if ( tmpOuterFieldMap.getName( ).compareTo( tmpInnerFieldMap.getName( ) ) == 0 ) {
                        throw new BitStoreMapException( "Nome '%s' duplicado nos campos #%d do tipo '%s' e #%d do tipo '%s' no registro '%s' do mapa '%s'",
                                                        tmpOuterFieldMap.getName( ),
                                                        ( tmpOuterFieldIndex + 1 ),
                                                        tmpOuterFieldMap.type.name( ),
                                                        ( tmpInnerFieldIndex + 1 ),
                                                        tmpInnerFieldMap.type.name( ),
                                                        this.name,
                                                        this.parentMap.name );
                    }
                }
                tmpOuterFieldMap.validate( );
                tmpFieldsBitLength += tmpOuterFieldMap.bitLength;
            }
            if ( this.bitLength != tmpFieldsBitLength ) {
                throw new BitStoreMapException( "Tamanho incoerente da visão principal do registro '%s' ( visão %d bits, campos %d bits )",
                                                this.name,
                                                this.bitLength,
                                                tmpFieldsBitLength );
            }
        }

        public int getBitLength( ) {
            return this.bitLength;
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return this.description;
        }

        public int getDataType( ) {
            return this.dataType;
        }

        public BitStoreMap getParentMap( ) {
            return this.parentMap;
        }

        public boolean isLittleEndianBytes( ) {
            return this.parentMap.isLittleEndianBytes( );
        }

        public boolean isLittleEndianBits( ) {
            return this.parentMap.isLittleEndianBits( );
        }

        public BitStoreViewRecordMap getMainViewRecord( ) {
            return this.mainViewRecord;
        }

        public int getByteLength( ) {
            return BitHelper.getBitArrayBufferLength( this.bitLength );
        }

        public int getIndex( ) {
            return this.mainViewRecord.index;
        }

        public BitStoreItemMap searchField( String fieldName ) {
            for ( BitStoreItemMap tmpFieldMap : fields ) {
                if ( tmpFieldMap.getName( ).compareTo( fieldName ) == 0 ) {
                    return tmpFieldMap;
                }
            }
            return null;
        }

        public BitStoreItemMap getField( String fieldName )
            throws BitStoreMapException {
            BitStoreItemMap tmpResult = searchField( fieldName );
            if ( tmpResult == null ) {
                throw new BitStoreMapException( "Não há campo '%s' no registro '%s' do mapa '%s'",
                                                fieldName,
                                                this.name,
                                                this.parentMap.name );
            }
            return tmpResult;
        }

        public BitStoreItemMap[ ] getFields( ) {
            return Arrays.copyOf( this.fields,
                                  this.fields.length );
        }

        @Override
        public String toString( ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            tmpResult.append( "%s {",
                              this.getClass( ).getSimpleName( ) );
            tmpResult.increaseIndent( );
            //
            ColumnsStringBuffer tmpHeaderString = new ColumnsStringBuffer( new int[ ] { 0,
                                                                                        0 } );
            tmpHeaderString.addLine( "name:",
                                     this.name );
            if ( this.description != null ) {
                tmpHeaderString.addLine( "description:",
                                         this.description );
            }
            tmpHeaderString.addLine( "length:",
                                     String.format( "%d bits / %d/%d bytes",
                                                    this.bitLength,
                                                    ( this.bitLength / Byte.SIZE ),
                                                    ( this.bitLength % Byte.SIZE ) ) );
            tmpResult.append( tmpHeaderString.getResult( 4,
                                                         2 ) );
            //
            ColumnsStringBuffer tmpFieldsString = new ColumnsStringBuffer( 0,
                                                                           0,
                                                                           0,
                                                                           0 );
            tmpFieldsString.addLine( "field",
                                     "type",
                                     "bitLength",
                                     "description" );
            tmpFieldsString.addSeparator( '-' );
            for ( BitStoreItemMap tmpField : this.fields ) {
                tmpFieldsString.addLine( tmpField.getName( ),
                                         tmpField.type.name( ), //
                                         Integer.toString( tmpField.bitLength ),
                                         tmpField.getDescription( ) );
            }
            //
            tmpResult.appendIndented( tmpFieldsString.getResult( 4,
                                                                 2 ) );
            tmpResult.decreaseIndent( );
            tmpResult.append( "}" );
            return tmpResult.getResult( );
        }
    }

    @XmlType( name = "piece",
              propOrder = { "length",
                            "offset" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreViewPieceMap {

        @XmlAttribute( required = true )
        protected int                 offset;

        @XmlAttribute( required = true )
        protected int                 length;

        @XmlTransient
        protected BitStoreViewItemMap parentViewItem;

        @XmlTransient
        protected int                 index;

        public BitStoreViewPieceMap( int offset,
                                     int length ) {
            this.offset = offset;
            this.length = length;
            this.parentViewItem = null;
            this.index = ( -1 );
        }

        protected BitStoreViewPieceMap( ) {
            this( ( -1 ),
                  ( -1 ) );
        }

        protected void engage( BitStoreViewItemMap parentViewItem,
                               int index ) {
            this.parentViewItem = parentViewItem;
            this.index = index;
        }

        public BitStoreViewItemMap getParentViewItem( ) {
            return parentViewItem;
        }

        public int getIndex( ) {
            return this.index;
        }

        public int getOffset( ) {
            return this.offset;
        }

        public int getLength( ) {
            return this.length;
        }
    }

    @XmlTransient
    public static abstract class BitStoreViewItemMap {

        @XmlTransient
        public static enum ItemType {
            Invalid,
            RUF,
            Field
        }

        @XmlAttribute( required = true )
        protected int                          bitLength;

        @XmlTransient
        protected BitStoreViewItemMap.ItemType itemType;

        @XmlElement( required = true,
                     name = "piece" )
        protected BitStoreViewPieceMap[ ]      pieces;

        @XmlTransient
        protected BitStoreViewRecordMap        parentViewRecord;

        @XmlTransient
        protected int                          index;

        public BitStoreViewItemMap( BitStoreViewItemMap.ItemType itemType,
                                    int bitLength,
                                    BitStoreViewPieceMap... pieces ) {
            this.itemType = itemType;
            this.pieces = pieces;
            this.bitLength = bitLength;
            this.parentViewRecord = null;
            this.index = ( -1 );
        }

        protected BitStoreViewItemMap( BitStoreViewItemMap.ItemType itemType ) {
            this.itemType = itemType;
            this.bitLength = ( -1 );
            this.pieces = null;
            this.index = ( -1 );
        }

        protected void engage( BitStoreViewRecordMap parentViewRecord,
                               int index )
            throws BitStoreMapException {
            this.parentViewRecord = parentViewRecord;
            this.index = index;
            for ( int tmpPieceIndex = 0; tmpPieceIndex < this.pieces.length; tmpPieceIndex++ ) {
                this.pieces[ tmpPieceIndex ].engage( this,
                                                     tmpPieceIndex );
            }
        }

        protected abstract void validate( )
            throws BitStoreMapException;

        protected abstract BitStoreItemData createDataObject( BitStoreRecordData parentRecordData );

        public abstract String getName( );

        public abstract String getDescription( );

        public abstract BitStoreItemType getType( );

        public int getBitLength( ) {
            return this.bitLength;
        }

        public int getByteLength( ) {
            return BitHelper.getBitArrayBufferLength( this.bitLength );
        }

        public BitStoreViewItemMap.ItemType getItemType( ) {
            return itemType;
        }

        public String getPath( ) {
            return String.format( "%s.%s.%s",
                                  this.parentViewRecord.parentView.getName( ),
                                  this.parentViewRecord.getName( ),
                                  this.getName( ) );
        }

        public int getIndex( ) {
            return this.index;
        }

        public BitStoreViewRecordMap getParentViewRecord( ) {
            return this.parentViewRecord;
        }

        public BitStoreViewPieceMap[ ] getPieces( ) {
            return Arrays.copyOf( this.pieces,
                                  this.pieces.length );
        }
    }

    @XmlType( name = "ruf",
              propOrder = { "pieces",
                            "bitLength" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreViewRUFMap extends BitStoreViewItemMap {

        @XmlTransient
        protected int rufIndex;

        public BitStoreViewRUFMap( int bitLength,
                                   BitStoreViewPieceMap... pieces ) {
            super( ItemType.RUF,
                   bitLength,
                   pieces );
        }

        protected BitStoreViewRUFMap( ) {
            super( ItemType.RUF );
        }

        @Override
        protected void validate( )
            throws BitStoreMapException {
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreRecordData parentRecordViewData ) {
            return new BitStoreZeroedData( this,
                                           parentRecordViewData );
        }

        @Override
        public BitStoreItemType getType( ) {
            return BitStoreItemType.ZERO;
        }

        @Override
        public String getName( ) {
            return String.format( "ZERO_%d",
                                  this.pieces[ 0 ].offset,
                                  ( this.rufIndex + 1 ) );
        }

        @Override
        public String getDescription( ) {
            return BitStoreRUFMap.DEFAULT_DESCRIPTION;
        }
    }

    @XmlType( name = "field",
              propOrder = { "pieces",
                            "bitLength",
                            "name" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreViewFieldMap extends BitStoreViewItemMap {

        @XmlAttribute( required = true )
        protected String          name;

        @XmlTransient
        protected BitStoreItemMap fieldMap;

        public BitStoreViewFieldMap( int bitLength,
                                     String name,
                                     BitStoreViewPieceMap... pieces ) {
            super( ItemType.Field,
                   bitLength,
                   pieces );
            this.name = name;
        }

        protected BitStoreViewFieldMap( ) {
            super( ItemType.Field );
            this.name = null;
        }

        @Override
        protected void engage( BitStoreViewRecordMap parentViewRecord,
                               int index )
            throws BitStoreMapException {
            super.engage( parentViewRecord,
                          index );
            this.fieldMap = parentViewRecord.getRecordMap( ).getField( this.name );
        }

        protected void checkBitLength( int modulo,
                                       int minimum,
                                       int maximum )
            throws BitStoreMapException {
            boolean tmpValidModulo = ( ( this.bitLength % modulo ) == 0 );
            boolean tmpValidMinimum = ( this.bitLength >= minimum );
            boolean tmpValidMaximum = ( this.bitLength <= maximum );
            if ( !tmpValidModulo || !tmpValidMinimum || !tmpValidMaximum ) {
                throw new BitStoreMapException( "Tamanho invalido de %d bits para o campo '%s' do tipo '%s' ( %s %d bits ) no registro '%s' da visão '%s' do mapa '%s'",
                                                this.bitLength,
                                                this.fieldMap.getName( ),
                                                this.fieldMap.type.name( ),
                                                ( ( minimum == maximum ) ? "deve ser"
                                                                         : ( !tmpValidModulo ? "multiplo de"
                                                                                             : ( !tmpValidMinimum ? "no minimo"
                                                                                                                  : "no maximo" ) ) ),
                                                ( ( minimum == maximum ) ? minimum
                                                                         : ( !tmpValidModulo ? modulo
                                                                                             : ( !tmpValidMinimum ? minimum
                                                                                                                  : maximum ) ) ),
                                                this.fieldMap.getParentRecord( ).name,
                                                this.parentViewRecord.parentView.name,
                                                this.fieldMap.getParentRecord( ).getParentMap( ).name );
            }
        }

        @Override
        @SuppressWarnings( "unused" )
        protected void validate( )
            throws BitStoreMapException {
            if ( false ) {
                if ( this.bitLength != this.fieldMap.bitLength ) {
                    throw new BitStoreMapException( "Tamanho de %d bits do campo '%s' do registro '%s' na visão '%s' do mapa '%s' é diferente do tamanho na visão principal ( %d bits )",
                                                    this.bitLength,
                                                    this.name,
                                                    this.parentViewRecord.name,
                                                    this.parentViewRecord.parentView.name,
                                                    this.parentViewRecord.parentView.parentStore.name,
                                                    this.fieldMap.bitLength );
                }
            }
        }

        @Override
        protected BitStoreItemData createDataObject( BitStoreRecordData parentRecordData ) {
            return this.fieldMap.createDataObject( this,
                                                   parentRecordData );
        }

        @Override
        public BitStoreItemType getType( ) {
            return this.fieldMap.type;
        }

        @Override
        public String getName( ) {
            return this.name;
        }

        @Override
        public String getDescription( ) {
            return this.fieldMap.getDescription( );
        }

        public BitStoreItemMap getFieldMap( ) {
            return this.fieldMap;
        }
    }

    @XmlType( name = "record_v",
              propOrder = { "viewItems",
                            "dataType",
                            "bitLength",
                            "name" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreViewRecordMap {

        @XmlAttribute( required = true )
        protected int                     bitLength;

        @XmlAttribute( required = true )
        protected String                  name;

        @XmlAttribute( required = false )
        protected String                  description;

        @XmlAttribute( required = false )
        protected Integer                 dataType;

        @XmlElements( { @XmlElement( required = true,
                                     name = "ruf",
                                     type = BitStoreViewRUFMap.class ),
                        @XmlElement( required = true,
                                     name = "field",
                                     type = BitStoreViewFieldMap.class ) } )
        protected BitStoreViewItemMap[ ]  viewItems;

        @XmlTransient
        protected BitStoreViewFieldMap[ ] viewFields;

        @XmlTransient
        protected BitStoreViewRUFMap[ ]   viewRUFs;

        @XmlTransient
        protected BitStoreViewMap         parentView;

        @XmlTransient
        protected BitStoreRecordMap       recordMap;

        @XmlTransient
        protected int                     index;

        public BitStoreViewRecordMap( int bitLength,
                                      String name,
                                      String description,
                                      int dataType,
                                      BitStoreViewItemMap... viewItems ) {
            this.bitLength = bitLength;
            this.name = name;
            this.description = description;
            this.dataType = dataType;
            this.viewItems = viewItems;
            for ( BitStoreViewItemMap tmpItem : this.viewItems ) {
                tmpItem.parentViewRecord = this;
            }
            this.index = ( -1 );
        }

        protected BitStoreViewRecordMap( ) {
            this.bitLength = ( -1 );
            this.name = null;
            this.description = null;
            this.dataType = ( -1 );
            this.viewItems = null;
            this.index = ( -1 );
        }

        protected void engage( BitStoreViewMap parentView,
                               int index )
            throws BitStoreMapException {
            this.parentView = parentView;
            this.index = index;
            this.recordMap = this.parentView.parentStore.getRecord( this.name );
            int tmpFieldCount = 0;
            int tmpRUFCount = 0;
            for ( int tmpItemIndex = 0; tmpItemIndex < this.viewItems.length; tmpItemIndex++ ) {
                BitStoreViewItemMap tmpViewItem = this.viewItems[ tmpItemIndex ];
                tmpViewItem.engage( this,
                                    tmpItemIndex );
                if ( tmpViewItem.getItemType( ) == BitStoreViewItemMap.ItemType.Field ) {
                    ++tmpFieldCount;
                }
                else if ( tmpViewItem.getItemType( ) == BitStoreViewItemMap.ItemType.RUF ) {
                    ++tmpRUFCount;
                }
                else {
                    // THROW
                }
            }
            this.viewFields = new BitStoreViewFieldMap[ tmpFieldCount ];
            this.viewRUFs = new BitStoreViewRUFMap[ tmpRUFCount ];
            int tmpViewFieldIndex = 0;
            int tmpViewRUFMapIndex = 0;
            for ( BitStoreViewItemMap tmpViewItem : this.viewItems ) {
                if ( tmpViewItem.getItemType( ) == BitStoreViewItemMap.ItemType.Field ) {
                    this.viewFields[ tmpViewFieldIndex ] = (BitStoreViewFieldMap) tmpViewItem;
                    ++tmpViewFieldIndex;
                }
                else if ( tmpViewItem.getItemType( ) == BitStoreViewItemMap.ItemType.RUF ) {
                    this.viewRUFs[ tmpViewRUFMapIndex ] = (BitStoreViewRUFMap) tmpViewItem;
                    this.viewRUFs[ tmpViewRUFMapIndex ].rufIndex = tmpViewRUFMapIndex;
                    ++tmpViewRUFMapIndex;
                }
            }
        }

        protected void validate( )
            throws BitStoreMapException {
            for ( BitStoreViewItemMap tmpItem : this.viewItems ) {
                tmpItem.validate( );
            }
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return ( this.description != null ) ? this.description
                                                : "";
        }

        public int getBitLength( ) {
            return this.bitLength;
        }

        public int getDataType( ) {
            return this.dataType;
        }

        public BitStoreRecordMap getRecordMap( ) {
            return this.recordMap;
        }

        public BitStoreViewMap getParentView( ) {
            return parentView;
        }

        public int getIndex( ) {
            return this.index;
        }

        public boolean isLittleEndianBytes( ) {
            return this.recordMap.isLittleEndianBytes( );
        }

        public boolean isLittleEndianBits( ) {
            return this.recordMap.isLittleEndianBits( );
        }

        public BitStoreViewItemMap[ ] getItems( ) {
            return Arrays.copyOf( this.viewItems,
                                  this.viewItems.length );
        }

        public BitStoreViewFieldMap searchField( String fieldName ) {
            for ( BitStoreViewFieldMap tmpFieldView : this.viewFields ) {
                if ( tmpFieldView.name.compareTo( fieldName ) == 0 ) {
                    return tmpFieldView;
                }
            }
            return null;
        }

        public BitStoreViewFieldMap getField( String fieldName )
            throws BitStoreMapException {
            BitStoreViewFieldMap tmpResult = this.searchField( fieldName );
            if ( tmpResult == null ) {
                throw new BitStoreMapException( "Nenhum campo '%s' no registro '%s' da visao '%s' do mapa '%s'",
                                                fieldName,
                                                this.name,
                                                this.parentView.name,
                                                this.parentView.parentStore.name );
            }
            return tmpResult;
        }

        public int getByteLength( ) {
            return BitHelper.getBitArrayBufferLength( this.bitLength );
        }

        @Override
        public String toString( ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            tmpResult.append( "%s {",
                              getClass( ).getSimpleName( ) );
            tmpResult.increaseIndent( );
            //
            ColumnsStringBuffer tmpHeaderString = new ColumnsStringBuffer( 0,
                                                                           0 );
            tmpHeaderString.addLine( "name:",
                                     this.name );
            if ( this.description != null ) {
                tmpHeaderString.addLine( "description:",
                                         this.description );
            }
            tmpHeaderString.addLine( "totalLength:",
                                     String.format( "%d/%d bytes",
                                                    ( this.bitLength / Byte.SIZE ),
                                                    ( this.bitLength % Byte.SIZE ) ) );
            tmpResult.append( tmpHeaderString.getResult( 4,
                                                         2 ) );
            //
            ColumnsStringBuffer tmpFieldsString = new ColumnsStringBuffer( 0,
                                                                           0,
                                                                           0 );
            tmpFieldsString.addLine( "field",
                                     "type",
                                     "bitLength" );
            tmpFieldsString.addSeparator( '-' );
            for ( BitStoreViewItemMap tmpViewItemMap : this.viewItems ) {
                tmpFieldsString.addLine( tmpViewItemMap.getName( ),
                                         tmpViewItemMap.getType( ).name( ), //
                                         Integer.toString( tmpViewItemMap.getBitLength( ) ) );
            }
            //
            tmpResult.appendIndented( tmpFieldsString.getResult( 4,
                                                                 2 ) );
            tmpResult.decreaseIndent( );
            tmpResult.append( "}" );
            return tmpResult.getResult( );
        }
    }

    @XmlType( name = "view",
              propOrder = { "viewRecords",
                            "description",
                            "name" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class BitStoreViewMap {

        @XmlAttribute( required = true )
        protected String                   name;

        @XmlAttribute( required = false )
        protected String                   description;

        @XmlElement( required = true,
                     name = "record" )
        protected BitStoreViewRecordMap[ ] viewRecords;

        @XmlTransient
        protected BitStoreMap              parentStore;

        @XmlTransient
        protected int                      bitLength;

        @XmlTransient
        protected int                      parentMapIndex;

        public BitStoreViewMap( String name,
                                String description,
                                BitStoreViewRecordMap... viewRecords ) {
            this.name = name;
            this.description = description;
            this.viewRecords = viewRecords;
            this.parentStore = null;
            this.bitLength = ( -1 );
            this.parentMapIndex = ( -1 );
        }

        protected BitStoreViewMap( ) {
            this.name = null;
            this.description = null;
            this.viewRecords = null;
            this.parentStore = null;
            this.bitLength = ( -1 );
            this.parentMapIndex = ( -1 );
        }

        protected void engage( BitStoreMap parentStore )
            throws BitStoreMapException {
            this.parentStore = parentStore;
            this.bitLength = 0;
            for ( int tmpRecordIndex = 0; tmpRecordIndex < this.viewRecords.length; tmpRecordIndex++ ) {
                BitStoreViewRecordMap tmpViewRecord = this.viewRecords[ tmpRecordIndex ];
                tmpViewRecord.engage( this,
                                      tmpRecordIndex );
                this.bitLength += tmpViewRecord.bitLength;
            }
        }

        protected void validate( )
            throws BitStoreMapException {
            for ( BitStoreViewRecordMap tmpViewRecord : this.viewRecords ) {
                tmpViewRecord.validate( );
            }
        }

        public String getName( ) {
            return this.name;
        }

        public String getDescription( ) {
            return ( this.description != null ) ? this.description
                                                : "";
        }

        public int getIndex( ) {
            if ( this.parentMapIndex < 1 ) {
                this.parentMapIndex = 0;
                for ( BitStoreViewMap tmpRecord : this.parentStore.views ) {
                    if ( tmpRecord.equals( this ) ) {
                        break;
                    }
                    ++this.parentMapIndex;
                }
            }
            return this.parentMapIndex;
        }

        public BitStoreMap getStoreMap( ) {
            return this.parentStore;
        }

        public int getBitLength( ) {
            return this.bitLength;
        }

        public int getByteLength( ) {
            return BitHelper.getBitArrayBufferLength( this.bitLength );
        }

        public BitStoreViewRecordMap searchRecord( String recordName ) {
            for ( BitStoreViewRecordMap tmpViewRecord : this.viewRecords ) {
                if ( tmpViewRecord.name.compareTo( recordName ) == 0 ) {
                    return tmpViewRecord;
                }
            }
            return null;
        }

        public BitStoreViewRecordMap getRecord( String recordName )
            throws BitStoreMapException {
            BitStoreViewRecordMap tmpViewRecord = searchRecord( recordName );
            if ( tmpViewRecord == null ) {
                throw new BitStoreMapException( "Nenhum registro '%s' na visao '%s' do mapa '%s'",
                                                recordName,
                                                this.name,
                                                this.parentStore.name );
            }
            return tmpViewRecord;
        }

        public BitStoreViewRecordMap[ ] getRecords( ) {
            return Arrays.copyOf( this.viewRecords,
                                  this.viewRecords.length );
        }

        @Override
        public String toString( ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            tmpResult.append( "%s {",
                              getClass( ).getSimpleName( ) );
            tmpResult.increaseIndent( );
            //
            ColumnsStringBuffer tmpHeaderString = new ColumnsStringBuffer( 0,
                                                                           0 );
            tmpHeaderString.addLine( "name:",
                                     this.name );
            if ( this.description != null ) {
                tmpHeaderString.addLine( "description:",
                                         this.description );
            }
            tmpResult.append( tmpHeaderString.getResult( 4,
                                                         2 ) );
            //
            ColumnsStringBuffer tmpRecordsString = new ColumnsStringBuffer( 0,
                                                                            0,
                                                                            0 );
            tmpRecordsString.addLine( "record",
                                      "totalLength",
                                      "description" );
            tmpRecordsString.addSeparator( '-' );
            for ( BitStoreViewRecordMap tmpViewRecord : this.viewRecords ) {
                tmpRecordsString.addLine( tmpViewRecord.name, //
                                          String.format( "%d/%d bytes",
                                                         ( tmpViewRecord.bitLength / Byte.SIZE ),
                                                         ( tmpViewRecord.bitLength % Byte.SIZE ) ),
                                          tmpViewRecord.getRecordMap( ).description );
            }
            //
            tmpResult.appendIndented( tmpRecordsString.getResult( 4,
                                                                  2 ) );
            tmpResult.decreaseIndent( );
            tmpResult.append( "}" );
            return tmpResult.getResult( );
        }
    }

    @XmlAttribute( required = true )
    protected String               name;

    @XmlAttribute( required = false )
    protected String               description;

    @XmlAttribute( required = false )
    protected Integer              versionMajor;

    @XmlAttribute( required = false )
    protected Integer              versionMinor;

    @XmlAttribute( required = false )
    protected Integer              versionRelease;

    @XmlAttribute( required = false )
    protected Boolean              isLittleEndianBytes;

    @XmlAttribute( required = false )
    protected Boolean              isLittleEndianBits;

    @XmlElement( required = true,
                 name = "record" )
    protected BitStoreRecordMap[ ] records;

    @XmlAttribute( required = true )
    protected String               mainViewName;

    @XmlAttribute( required = false )
    protected String               mainViewDescription;

    @XmlTransient
    protected BitStoreViewMap      mainView;

    @XmlElement( required = false,
                 name = "alternativeView" )
    protected BitStoreViewMap[ ]   alternativeViews;

    @XmlTransient
    protected BitStoreViewMap[ ]   views;

    public BitStoreMap( String name,
                        String description,
                        int versionMajor,
                        int versionMinor,
                        int versionRelease,
                        Boolean isLittleEndianBytes,
                        Boolean isLittleEndianBits,
                        BitStoreRecordMap[ ] records,
                        String mainViewName,
                        String mainViewDescription,
                        BitStoreViewMapBuilder... viewMapBuilders )
        throws BitStoreMapException {
        this.name = name;
        this.description = description;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.versionRelease = versionRelease;
        this.isLittleEndianBytes = isLittleEndianBytes;
        this.isLittleEndianBits = isLittleEndianBits;
        this.records = records;
        this.mainViewName = mainViewName;
        this.mainViewDescription = mainViewDescription;
        this.mainView = null;
        this.alternativeViews = new BitStoreViewMap[ viewMapBuilders.length ];
        for ( int tmpIndex = 0; tmpIndex < this.alternativeViews.length; tmpIndex++ ) {
            this.alternativeViews[ tmpIndex ] = viewMapBuilders[ tmpIndex ].createView( this );
        }
        this.views = null;
        this.engage( );
        this.validate( );
    }

    public BitStoreMap( String name,
                        String description,
                        int versionMajor,
                        int versionMinor,
                        int versionRelease,
                        BitStoreRecordMap[ ] records,
                        String mainViewName,
                        String mainViewDescription,
                        BitStoreViewMapBuilder... viewMapBuilders )
        throws BitStoreMapException {
        this( name,
              description,
              versionMajor,
              versionMinor,
              versionRelease,
              null,
              null,
              records,
              mainViewName,
              mainViewDescription,
              viewMapBuilders );
    }

    public BitStoreMap( String name,
                        String description,
                        int versionMajor,
                        int versionMinor,
                        int versionRelease,
                        Boolean isLittleEndianBytes,
                        Boolean isLittleEndianBits,
                        BitStoreRecordMap[ ] records,
                        String mainViewName,
                        String mainViewDescription,
                        BitStoreViewMap... alternativeViews )
        throws BitStoreMapException {
        this.name = name;
        this.description = description;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.versionRelease = versionRelease;
        this.isLittleEndianBytes = isLittleEndianBytes;
        this.isLittleEndianBits = isLittleEndianBits;
        this.records = records;
        this.mainViewName = mainViewName;
        this.mainViewDescription = mainViewDescription;
        this.mainView = null;
        this.alternativeViews = alternativeViews;
        this.views = null;
        this.engage( );
        this.validate( );
    }

    protected BitStoreMap( )
        throws BitStoreMapException {
        this.name = null;
        this.description = null;
        this.versionMajor = null;
        this.versionMinor = null;
        this.versionRelease = null;
        this.isLittleEndianBytes = null;
        this.isLittleEndianBits = null;
        this.records = null;
        this.mainViewName = null;
        this.mainViewDescription = null;
        this.alternativeViews = null;
    }

    protected void engage( )
        throws BitStoreMapException {
        BitStoreViewRecordMap[ ] tmpViewRecords = new BitStoreViewRecordMap[ this.records.length ];
        for ( int tmpIndex = 0; tmpIndex < tmpViewRecords.length; tmpIndex++ ) {
            this.records[ tmpIndex ].engage( this );
            tmpViewRecords[ tmpIndex ] = this.records[ tmpIndex ].getMainViewRecord( );
        }
        this.mainView = new BitStoreViewMap( this.mainViewName,
                                             this.mainViewDescription,
                                             tmpViewRecords );
        this.mainView.engage( this );
        for ( BitStoreViewMap tmpView : this.alternativeViews ) {
            tmpView.engage( this );
        }
        this.views = new BitStoreViewMap[ 1 + this.alternativeViews.length ];
        this.views[ 0 ] = this.mainView;
        System.arraycopy( this.alternativeViews,
                          0,
                          this.views,
                          1,
                          this.alternativeViews.length );
    }

    protected void validate( )
        throws BitStoreMapException {
        for ( BitStoreRecordMap tmpRecord : this.records ) {
            tmpRecord.validate( );
        }
        this.mainView.validate( );
        for ( BitStoreViewMap tmpAlternativeView : this.alternativeViews ) {
            tmpAlternativeView.validate( );
        }
    }

    public String getName( ) {
        return this.name;
    }

    public String getDescription( ) {
        return ( this.description != null ) ? this.description
                                            : "";
    }

    @Override
    public String getVersionName( ) {
        return this.name;
    }

    @Override
    public int getVersionMajor( ) {
        return ( this.versionMajor != null ) ? this.versionMajor
                                             : 0;
    }

    @Override
    public int getVersionMinor( ) {
        return ( this.versionMinor != null ) ? this.versionMinor
                                             : 0;
    }

    @Override
    public int getVersionRelease( ) {
        return ( this.versionRelease != null ) ? this.versionRelease
                                               : 0;
    }

    @Override
    public String getVersionString( ) {
        return String.format( "%d.%02d.%d",
                              this.getVersionMajor( ),
                              this.getVersionMinor( ),
                              this.getVersionRelease( ) );
    }

    public boolean isLittleEndianBytes( ) {
        return ( this.isLittleEndianBytes == null ) ? false
                                                    : this.isLittleEndianBytes;
    }

    public boolean isLittleEndianBits( ) {
        return ( this.isLittleEndianBits == null ) ? false
                                                   : this.isLittleEndianBits;
    }

    public BitStoreRecordMap[ ] getRecords( ) {
        return Arrays.copyOf( this.records,
                              this.records.length );
    }

    public BitStoreRecordMap searchRecord( String recordName )
        throws BitStoreMapException {
        for ( BitStoreRecordMap tmpRecord : this.records ) {
            if ( tmpRecord.name.compareTo( recordName ) == 0 ) {
                return tmpRecord;
            }
        }
        return null;
    }

    public BitStoreRecordMap getRecord( String recordName )
        throws BitStoreMapException {
        BitStoreRecordMap tmpRecord = this.searchRecord( recordName );
        if ( tmpRecord == null ) {
            throw new BitStoreMapException( "Nenhum registro '%s' no mapa '%s'",
                                            recordName,
                                            this.name );
        }
        return tmpRecord;
    }

    public BitStoreViewRecordMap getRecordView( String recordPath )
        throws BitStoreMapException {
        String[ ] tmpItemPath = recordPath.split( "\\." );
        if ( tmpItemPath.length != 2 ) {
            throw new BitStoreMapException( "Caminho '%s' inválido para registro no mapa '%s'",
                                            recordPath,
                                            this.getName( ) );
        }
        return this.getView( tmpItemPath[ 0 ] ).getRecord( tmpItemPath[ 1 ] );
    }

    public BitStoreViewMap[ ] getViews( ) {
        return Arrays.copyOf( this.views,
                              this.views.length );
    }

    public BitStoreViewMap getMainView( ) {
        return this.mainView;
    }

    public BitStoreViewMap[ ] getAlternativeViews( ) {
        return Arrays.copyOf( this.alternativeViews,
                              this.alternativeViews.length );
    }

    public BitStoreViewMap searchView( String viewName ) {
        for ( BitStoreViewMap tmpView : this.views ) {
            if ( tmpView.name.compareTo( viewName ) == 0 ) {
                return tmpView;
            }
        }
        return null;
    }

    public BitStoreViewMap getView( String viewName )
        throws BitStoreMapException {
        BitStoreViewMap tmpResult = this.searchView( viewName );
        if ( tmpResult == null ) {
            throw new BitStoreMapException( "Nenhuma visão '%s' no mapa '%s'",
                                            viewName,
                                            this.name );
        }
        return tmpResult;
    }

    @Override
    public String toString( ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "%s {",
                          getClass( ).getSimpleName( ) );
        tmpResult.increaseIndent( );
        //
        ColumnsStringBuffer tmpHeaderString = new ColumnsStringBuffer( 0,
                                                                       0 );
        tmpHeaderString.addLine( new String[ ] { "name:",
                                                 this.name } );
        if ( this.description != null ) {
            tmpHeaderString.addLine( new String[ ] { "description:",
                                                     this.description } );
        }
        if ( this.isLittleEndianBytes != null ) {
            tmpHeaderString.addLine( new String[ ] { "isLittleEndianBytes:",
                                                     this.isLittleEndianBytes ? "YES"
                                                                              : "NO" } );
        }
        if ( this.isLittleEndianBits != null ) {
            tmpHeaderString.addLine( new String[ ] { "isLittleEndianBits:",
                                                     this.isLittleEndianBits ? "YES"
                                                                             : "NO" } );
        }
        if ( this.versionMajor != null ) {
            tmpHeaderString.addLine( new String[ ] { "version:",
                                                     String.format( "%d.%d.%d",
                                                                    this.versionMajor,
                                                                    this.versionMinor,
                                                                    this.versionRelease ) } );
        }
        tmpResult.append( tmpHeaderString.getResult( 4,
                                                     2 ) );
        tmpResult.increaseIndent( );
        ColumnsStringBuffer tmpRecordsString = new ColumnsStringBuffer( 0,
                                                                        0,
                                                                        0 );
        for ( BitStoreViewMap tmpViews : this.getViews( ) ) {
            tmpResult.append( "View '%s - %s'",
                              tmpViews.name,
                              tmpViews.getDescription( ) );
            tmpRecordsString.reset( );
            tmpRecordsString.addLine( new String[ ] { "record",
                                                      "totalLength",
                                                      "description" } );
            tmpRecordsString.addSeparator( '-' );
            for ( BitStoreViewRecordMap tmpViewRecord : tmpViews.getRecords( ) ) {
                tmpRecordsString.addLine( tmpViewRecord.getName( ), //
                                          String.format( "%d/%d bytes",
                                                         ( tmpViewRecord.getBitLength( ) / Byte.SIZE ),
                                                         ( tmpViewRecord.getBitLength( ) % Byte.SIZE ) ),
                                          tmpViewRecord.getDescription( ) );
            }
            tmpResult.appendIndented( tmpRecordsString.getResult( 4,
                                                                  2 ) );
        }
        tmpResult.decreaseIndent( 2 );
        tmpResult.append( "}" );
        return tmpResult.getResult( );
    }

    public String toXml( )
        throws BitStoreMapException {
        try {
            JAXBContext tmpJaxbContext = JAXBContext.newInstance( BitStoreMap.class );
            Marshaller tmpMarshaller = tmpJaxbContext.createMarshaller( );
            tmpMarshaller.setProperty( "jaxb.encoding",
                                       System.getProperty( "file.encoding" ) );
            ByteArrayOutputStream tmpBytes = new ByteArrayOutputStream( );
            tmpMarshaller.marshal( this,
                                   tmpBytes );
            return new String( tmpBytes.toByteArray( ) );
        }
        catch ( Throwable e ) {
            throw new BitStoreMapException( e,
                                            "Erro na geracao da visao XML do mapa: %s",
                                            e.getMessage( ) );
        }
    }

    public static BitStoreMap fromXml( String xmlString )
        throws BitStoreMapException {
        try {
            JAXBContext tmpJaxbContext = JAXBContext.newInstance( BitStoreMap.class );
            Unmarshaller tmpUnmarshaller = tmpJaxbContext.createUnmarshaller( );
            //
            ByteArrayInputStream tmpInputStream = new ByteArrayInputStream( xmlString.getBytes( ) );
            /*
             * Forja referência aos construtores 'default',
             * para que não sejam removidos em alguma otimização
             */
            BitStoreMap tmpStoreMap = new BitStoreMap( );
            BitStoreRecordMap tmpRecordMap = new BitStoreRecordMap( );
            tmpRecordMap.getName( );
            BitStoreItemMap tmpFieldMap = new BitStoreBooleanFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreIntegerFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreDateFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreTimeFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreEnumerationFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreCurrencyFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreBitArrayFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreByteArrayFieldMap( );
            tmpFieldMap.getName( );
            tmpFieldMap = new BitStoreIntegerArrayFieldMap( );
            tmpFieldMap.getName( );
            //
            tmpStoreMap = (BitStoreMap) tmpUnmarshaller.unmarshal( tmpInputStream );
            //
            tmpStoreMap.engage( );
            tmpStoreMap.validate( );
            return tmpStoreMap;
        }
        catch ( JAXBException e ) {
            throw new BitStoreMapException( e,
                                            "Erro ao interpretar/construir mapa a partir da visao XML: %s",
                                            ProcessHelper.stackTrace( e ) );
        }
    }
}
