package asap.primitive.bits.bitStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import asap.primitive.bits.BitHelper;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreDataException;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreItemData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreRecordData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreViewData;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreBitArrayFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreBooleanFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreByteArrayFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreCurrencyFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreDateFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreEnumerationFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreIntegerArrayFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreIntegerFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreItemMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreMapException;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreRUFMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreRecordMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreTimeFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewFieldMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewItemMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewPieceMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewRUFMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewRecordMap;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreAccessException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreAuthenticationException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreDeviceException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreMemorySource;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreSessionException;
import asap.primitive.bits.bitStore.BitStoreSource.BitStoreSetupException;
import asap.primitive.bytes.ByteHelper;
import asap.primitive.file.FileHelper;
import asap.primitive.string.ColumnsStringBuffer;
import asap.primitive.string.IndentedStringBuilder;
import asap.primitive.string.StringHelper;

public class BitStoreHelper {

    public static class BitStoreFieldSequence {

        protected int offset;

        public BitStoreFieldSequence( ) {
            this.reset( );
        }

        public int getOffset( ) {
            return this.offset;
        }

        public BitStoreViewPieceMap createPiece( int pieceLength ) {
            BitStoreViewPieceMap tmpPiece = new BitStoreViewPieceMap( this.offset,
                                                                      pieceLength );
            this.offset += pieceLength;
            return tmpPiece;
        }

        public BitStoreFieldSequence reset( ) {
            this.offset = 0;
            return this;
        }
    }

    public static class BitStoreMapHelper {

        protected String                booleanYesValue;

        protected String                booleanNoValue;

        protected BitStoreFieldSequence sequence;

        public BitStoreMapHelper( String booleanYesValue,
                                  String booleanNoValue ) {
            this.booleanYesValue = booleanYesValue;
            this.booleanNoValue = booleanNoValue;
            this.sequence = new BitStoreFieldSequence( );
        }

        public BitStoreMapHelper reset( ) {
            this.sequence.reset( );
            return this;
        }

        public BitStoreRecordMap createRecord( String name,
                                               String description,
                                               int dataType,
                                               BitStoreItemMap... fieldMaps ) {
            int tmpBitLength = 0;
            for ( BitStoreItemMap tmpFieldMap : fieldMaps ) {
                tmpBitLength += tmpFieldMap.getBitLength( );
            }
            return new BitStoreRecordMap( tmpBitLength,
                                          name,
                                          description,
                                          dataType,
                                          fieldMaps );
        }

        public BitStoreRUFMap createRUFField( int bitLength ) {
            return new BitStoreRUFMap( bitLength,
                                       this.sequence.createPiece( bitLength ) );
        }

        public BitStoreBooleanFieldMap createBooleanField( String name,
                                                           String description,
                                                           String trueString,
                                                           String falseString ) {
            return new BitStoreBooleanFieldMap( BitStoreBooleanFieldMap.BIT_LENGTH,
                                                name,
                                                description,
                                                trueString,
                                                falseString,
                                                this.sequence.createPiece( BitStoreBooleanFieldMap.BIT_LENGTH ) );
        }

        public BitStoreBooleanFieldMap createBooleanField_TrueFalse( String name,
                                                                     String description ) {
            return new BitStoreBooleanFieldMap( BitStoreBooleanFieldMap.BIT_LENGTH,
                                                name,
                                                description,
                                                null,
                                                null,
                                                this.sequence.createPiece( BitStoreBooleanFieldMap.BIT_LENGTH ) );
        }

        public BitStoreBooleanFieldMap createBooleanField_YesNo( String name,
                                                                 String description ) {
            return this.createBooleanField( name,
                                            description,
                                            this.booleanYesValue,
                                            this.booleanNoValue );
        }

        public BitStoreIntegerFieldMap createIntegerField( int bitLength,
                                                           String name,
                                                           String description ) {
            return new BitStoreIntegerFieldMap( bitLength,
                                                name,
                                                description,
                                                (Integer) null,
                                                this.sequence.createPiece( bitLength ) );
        }

        public BitStoreIntegerFieldMap createHexaIntegerField( int bitLength,
                                                               String name,
                                                               String description ) {
            return new BitStoreIntegerFieldMap( bitLength,
                                                name,
                                                description,
                                                16,
                                                this.sequence.createPiece( bitLength ) );
        }

        public BitStoreCurrencyFieldMap createCurrencyField( int bitLength,
                                                             String name,
                                                             String description ) {
            return new BitStoreCurrencyFieldMap( bitLength,
                                                 name,
                                                 description,
                                                 this.sequence.createPiece( bitLength ) );
        }

        public BitStoreDateFieldMap createDateField( String name,
                                                     String description ) {
            return new BitStoreDateFieldMap( BitStoreDateFieldMap.BIT_LENGTH,
                                             name,
                                             description,
                                             this.sequence.createPiece( BitStoreDateFieldMap.BIT_LENGTH ) );
        }

        public BitStoreTimeFieldMap createTimeField( String name,
                                                     String description ) {
            return new BitStoreTimeFieldMap( BitStoreTimeFieldMap.BIT_LENGTH,
                                             name,
                                             description,
                                             this.sequence.createPiece( BitStoreTimeFieldMap.BIT_LENGTH ) );
        }

        public BitStoreBitArrayFieldMap createBitArrayField( int arrayLength,
                                                             String name,
                                                             String description ) {
            return new BitStoreBitArrayFieldMap( arrayLength,
                                                 name,
                                                 description,
                                                 this.sequence.createPiece( arrayLength ) );
        }

        public BitStoreByteArrayFieldMap createByteArrayField( int arrayLength,
                                                               String name,
                                                               String description ) {
            return new BitStoreByteArrayFieldMap( arrayLength,
                                                  name,
                                                  description,
                                                  this.sequence.createPiece( ( arrayLength * Byte.SIZE ) ) );
        }

        public BitStoreIntegerArrayFieldMap createIntegerArrayField( int integerLength,
                                                                     int arrayLength,
                                                                     String name,
                                                                     String description ) {
            return new BitStoreIntegerArrayFieldMap( integerLength,
                                                     arrayLength,
                                                     name,
                                                     description,
                                                     this.sequence.createPiece( arrayLength * integerLength ) );
        }

        public BitStoreEnumerationFieldMap createEnumerationField( int bitLength,
                                                                   String name,
                                                                   String description,
                                                                   BitStoreEnumerationFieldMap.BitStoreEnumerationFieldItemMap... items ) {
            return new BitStoreEnumerationFieldMap( bitLength,
                                                    name,
                                                    description,
                                                    items,
                                                    this.sequence.createPiece( bitLength ) );
        }

        public BitStoreEnumerationFieldMap createLittleEndianEnumerationField( int bitLength,
                                                                               String name,
                                                                               String description,
                                                                               BitStoreEnumerationFieldMap.BitStoreEnumerationFieldItemMap... items ) {
            return new BitStoreEnumerationFieldMap( bitLength,
                                                    name,
                                                    description,
                                                    true,
                                                    items,
                                                    this.sequence.createPiece( bitLength ) );
        }

        public BitStoreEnumerationFieldMap.BitStoreEnumerationFieldItemMap createEnumerationFieldItem( int value,
                                                                                                       String name ) {
            return new BitStoreEnumerationFieldMap.BitStoreEnumerationFieldItemMap( value,
                                                                                    name );
        }
    }

    public static abstract class BitStoreViewItemMapBuilder {

        protected abstract BitStoreViewItemMap createViewItem( BitStoreViewMapBuilder parentMapViewMapBuilder,
                                                               BitStoreMap bitStoreMap,
                                                               BitStoreRecordMap bitStoreRecordMap,
                                                               BitStoreFieldSequence bitStoreFieldSequence )
            throws BitStoreMapException;
    }

    public static class BitStoreViewRUFMapBuilder extends BitStoreViewItemMapBuilder {

        protected final int bitLength;

        public BitStoreViewRUFMapBuilder( int bitLength ) {
            this.bitLength = bitLength;
        }

        @Override
        protected BitStoreViewItemMap createViewItem( BitStoreViewMapBuilder parentMapViewMapBuilder,
                                                      BitStoreMap bitStoreMap,
                                                      BitStoreRecordMap bitStoreRecordMap,
                                                      BitStoreFieldSequence bitStoreFieldSequence )
            throws BitStoreMapException {
            return new BitStoreViewRUFMap( this.bitLength,
                                           bitStoreFieldSequence.createPiece( this.bitLength ) );
        }
    }

    public static class BitStoreViewFieldMapBuilder extends BitStoreViewItemMapBuilder {

        protected final String fieldName;

        protected final int    bitLength;

        public BitStoreViewFieldMapBuilder( String fieldName ) {
            this.fieldName = fieldName;
            this.bitLength = ( -1 );
        }

        public BitStoreViewFieldMapBuilder( String fieldName,
                                            int bitLength ) {
            this.fieldName = fieldName;
            this.bitLength = bitLength;
        }

        @Override
        protected BitStoreViewItemMap createViewItem( BitStoreViewMapBuilder parentMapViewMapBuilder,
                                                      BitStoreMap bitStoreMap,
                                                      BitStoreRecordMap bitStoreRecordMap,
                                                      BitStoreFieldSequence bitStoreFieldSequence )
            throws BitStoreMapException {
            BitStoreItemMap tmpFieldMap = bitStoreRecordMap.searchField( this.fieldName );
            if ( tmpFieldMap == null ) {
                throw new BitStoreMapException( "Não há campo '%s' no registro '%s' do mapa '%s' para a visão '%s'",
                                                this.fieldName,
                                                bitStoreRecordMap.name,
                                                bitStoreMap.name,
                                                parentMapViewMapBuilder.viewName );
            }
            int tmpBitLength = ( ( this.bitLength > 0 ) ? this.bitLength
                                                        : tmpFieldMap.getBitLength( ) );
            return new BitStoreViewFieldMap( tmpBitLength,
                                             this.fieldName,
                                             bitStoreFieldSequence.createPiece( tmpBitLength ) );
        }
    }

    public static class BitStoreViewRecordMapBuilder {

        protected final String                        recordName;

        protected final BitStoreViewItemMapBuilder[ ] itemViewBuilders;

        public BitStoreViewRecordMapBuilder( String recordName,
                                             BitStoreViewItemMapBuilder... itemViewBuilders ) {
            this.recordName = recordName;
            this.itemViewBuilders = itemViewBuilders;
        }

        protected BitStoreViewRecordMap createViewRecord( BitStoreViewMapBuilder parentMapViewMapBuilder,
                                                          BitStoreMap bitStoreMap )
            throws BitStoreMapException {
            BitStoreFieldSequence tmpSequence = new BitStoreFieldSequence( );
            BitStoreRecordMap tmpRecordMap = bitStoreMap.searchRecord( this.recordName );
            if ( tmpRecordMap == null ) {
                throw new BitStoreMapException( "Não há registro '%s' no mapa '%s' para a visão '%s'",
                                                this.recordName,
                                                bitStoreMap.name,
                                                parentMapViewMapBuilder.viewName );
            }
            BitStoreViewItemMap[ ] tmpItemMaps = new BitStoreViewItemMap[ this.itemViewBuilders.length ];
            for ( int tmpIndex = 0; tmpIndex < tmpItemMaps.length; tmpIndex++ ) {
                tmpItemMaps[ tmpIndex ] = this.itemViewBuilders[ tmpIndex ].createViewItem( parentMapViewMapBuilder,
                                                                                            bitStoreMap,
                                                                                            tmpRecordMap,
                                                                                            tmpSequence );
            }
            return new BitStoreViewRecordMap( tmpSequence.getOffset( ),
                                              tmpRecordMap.name,
                                              tmpRecordMap.description,
                                              tmpRecordMap.dataType,
                                              tmpItemMaps );
        }
    }

    public static class BitStoreViewMapBuilder {

        protected final String                          viewName;

        protected final String                          viewDescription;

        protected final BitStoreViewRecordMapBuilder[ ] recordViewBuilders;

        public BitStoreViewMapBuilder( String viewName,
                                       String viewDescription,
                                       BitStoreViewRecordMapBuilder... recordViewMapBuilders ) {
            this.viewName = viewName;
            this.viewDescription = viewDescription;
            this.recordViewBuilders = recordViewMapBuilders;
        }

        protected BitStoreViewMap createView( BitStoreMap bitStoreMap )
            throws BitStoreMapException {
            BitStoreViewRecordMap[ ] tmpRecordMaps = new BitStoreViewRecordMap[ this.recordViewBuilders.length ];
            for ( int tmpIndex = 0; tmpIndex < tmpRecordMaps.length; tmpIndex++ ) {
                tmpRecordMaps[ tmpIndex ] = this.recordViewBuilders[ tmpIndex ].createViewRecord( this,
                                                                                                  bitStoreMap );
            }
            return new BitStoreViewMap( this.viewName,
                                        this.viewDescription,
                                        tmpRecordMaps );
        }
    }

    public static String toTextImage( BitStoreData bitStore )
        throws BitStoreSetupException,
            BitStoreDeviceException,
            BitStoreSessionException,
            BitStoreAuthenticationException,
            BitStoreAccessException,
            BitStoreMapException,
            BitStoreDataException {
        StringBuilder tmpTextImage = new StringBuilder( );
        tmpTextImage.append( String.format( "MediaId=%s\n",
                                            ByteHelper.hexify( bitStore.getSource( ).getMediaId( ),
                                                               "" ) ) );
        BitStoreMap tmpDataMap = bitStore.getMap( );
        tmpTextImage.append( String.format( "DataMapName=%s\n",
                                            tmpDataMap.getName( ) ) );
        tmpTextImage.append( String.format( "DataMapVersion=%s\n",
                                            tmpDataMap.getVersionString( ) ) );
        for ( BitStoreRecordData tmpRecord : bitStore.getMainView( ).getRecords( ) ) {
            tmpTextImage.append( String.format( "%s=%s\n",
                                                tmpRecord.getName( ),
                                                ByteHelper.hexify( tmpRecord.getBytes( ),
                                                                   "" ) ) );
        }
        return tmpTextImage.toString( );
    }

    public static BitStoreData fromTextImage( String textImage,
                                              BitStoreMap... bitStoreMaps )
        throws BitStoreSetupException,
            BitStoreDeviceException,
            BitStoreSessionException,
            BitStoreAuthenticationException,
            BitStoreAccessException,
            BitStoreMapException,
            BitStoreDataException {
        byte[ ] tmpMediaId = null;
        String tmpMapName = null;
        String tmpMapVersion = null;
        BitStoreData tmpResult = null;
        for ( String tmpImageLine : StringHelper.linesToList( textImage ) ) {
            String[ ] tmpImageEntry = tmpImageLine.split( "=" );
            if ( tmpImageEntry[ 0 ].compareTo( "MediaId" ) == 0 ) {
                tmpMediaId = ByteHelper.parseHexString( tmpImageEntry[ 1 ] );
            }
            else if ( tmpImageEntry[ 0 ].compareTo( "DataMapName" ) == 0 ) {
                tmpMapName = tmpImageEntry[ 1 ];
            }
            else if ( tmpImageEntry[ 0 ].compareTo( "DataMapVersion" ) == 0 ) {
                tmpMapVersion = tmpImageEntry[ 1 ];
            }
            else {
                if ( tmpResult == null ) {
                    if ( ( tmpMediaId == null ) || ( tmpMapName == null ) || ( tmpMapVersion == null ) ) {
                        throw new BitStoreMapException( "Imagem de dados não contém informações da media ou do mapa" );
                    }
                    for ( BitStoreMap tmpMap : bitStoreMaps ) {
                        if ( ( tmpMap.getName( ).compareTo( tmpMapName ) == 0 )
                             && ( tmpMap.getVersionString( ).compareTo( tmpMapVersion ) == 0 ) ) {
                            tmpResult = new BitStoreData( tmpMap,
                                                          new BitStoreMemorySource( tmpMap,
                                                                                    tmpMediaId ) );
                            break;
                        }
                    }
                    if ( tmpResult == null ) {
                        throw new BitStoreMapException( "Mapa '%s %s' é desconhecido" );
                    }
                }
                tmpResult.getMainView( ).getRecord( tmpImageEntry[ 0 ] ).setBytes( ByteHelper.parseHexString( tmpImageEntry[ 1 ] ) );
            }
        }
        return tmpResult;
    }

    public static BitStoreMap fromXmlMapFile( String xmlMapFilePath )
        throws BitStoreMapException {
        try {
            return BitStoreMap.fromXml( FileHelper.loadTextFile( xmlMapFilePath ) );
        }
        catch ( IOException e ) {
            throw new BitStoreMapException( e.getLocalizedMessage( ) );
        }
    }

    public static BitStoreMap fromXmlMapResource( String xmlMapResourcePath )
        throws BitStoreMapException {
        try {
            return BitStoreMap.fromXml( FileHelper.loadTextResource( xmlMapResourcePath ) );
        }
        catch ( IOException e ) {
            throw new BitStoreMapException( e.getLocalizedMessage( ) );
        }
    }

    public static BitStoreMap fromXmlMapResource( Class< ? > clazz,
                                                  String xmlMapResourcePath )
        throws BitStoreMapException {
        try {
            return BitStoreMap.fromXml( FileHelper.loadTextResource( clazz,
                                                                     xmlMapResourcePath ) );
        }
        catch ( IOException e ) {
            throw new BitStoreMapException( e.getLocalizedMessage( ) );
        }
    }

    public static String dumpStoreMap( BitStoreMap bitStoreMap ) {
        IndentedStringBuilder tmpMapDump = new IndentedStringBuilder( );
        ColumnsStringBuffer tmpMapHeader = new ColumnsStringBuffer( 13,
                                                                    0 );
        tmpMapHeader.addLine( "Mapa:",
                              bitStoreMap.name );
        tmpMapHeader.addLine( "Descrição:",
                              bitStoreMap.getDescription( ) );
        tmpMapHeader.addLine( new String[ ] { "LittleEndian:",
                                              bitStoreMap.isLittleEndianBytes( ) ? "SIM"
                                                                                 : "NÃO" } );
        tmpMapHeader.addLine( "Versão:",
                              String.format( "%dv%dr%d",
                                             bitStoreMap.getVersionMajor( ),
                                             bitStoreMap.getVersionMinor( ),
                                             bitStoreMap.getVersionRelease( ) ) );
        tmpMapDump.append( tmpMapHeader.getResult( 2,
                                                   2 ) );
        for ( BitStoreViewMap tmpMapView : bitStoreMap.getViews( ) ) {
            tmpMapDump.appendIndented( BitStoreHelper.dumpStoreViewMap( tmpMapView ) );
        }
        return tmpMapDump.getResult( );
    }

    public static String dumpStoreViewMap( BitStoreViewMap bitStoreViewMap ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "%3d bits - %2d/%d bytes - Visão: %s",
                          bitStoreViewMap.getBitLength( ),
                          ( bitStoreViewMap.getBitLength( ) / Byte.SIZE ),
                          ( bitStoreViewMap.getBitLength( ) % Byte.SIZE ),
                          bitStoreViewMap.name );
        for ( BitStoreViewRecordMap tmpRecordView : bitStoreViewMap.getRecords( ) ) {
            tmpResult.appendIndented( BitStoreHelper.dumpStoreRecordMap( tmpRecordView ) );
        }
        return tmpResult.getResult( );
    }

    public static String dumpStoreRecordMap( BitStoreViewRecordMap viewRecord ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "%3d bits - %2d/%d bytes - Registro '%s'",
                          viewRecord.bitLength,
                          ( viewRecord.bitLength / Byte.SIZE ),
                          ( viewRecord.bitLength % Byte.SIZE ),
                          viewRecord.name );
        tmpResult.appendIndented( dumpStoreFieldsMap( viewRecord ) );
        return tmpResult.getResult( );
    }

    public static String dumpStoreFieldsMap( BitStoreViewRecordMap viewRecord ) {
        ColumnsStringBuffer tmpFieldsString = new ColumnsStringBuffer( 30,
                                                                       15,
                                                                       10,
                                                                       -1 );
        tmpFieldsString.addLine( "campo",
                                 "tipo",
                                 "tamanho",
                                 "posicao bit/byte" );
        tmpFieldsString.addSeparator( '-' );
        for ( BitStoreViewItemMap tmpRecordViewItemMap : viewRecord.getItems( ) ) {
            int tmpBitLength = tmpRecordViewItemMap.getBitLength( );
            String tmpPieces = null;
            for ( BitStoreViewPieceMap tmpFieldPieceMap : tmpRecordViewItemMap.getPieces( ) ) {
                if ( tmpPieces == null ) {
                    tmpPieces = "";
                }
                else {
                    tmpPieces += "\n";
                }
                int tmpFinalPos = ( tmpFieldPieceMap.offset + tmpFieldPieceMap.length );
                tmpPieces += String.format( "#%d - %3d a %3d / %2d:%d a %2d:%d",
                                            tmpFieldPieceMap.index,
                                            tmpFieldPieceMap.offset,
                                            tmpFinalPos,
                                            ( tmpFieldPieceMap.offset / Byte.SIZE ),
                                            ( tmpFieldPieceMap.offset % Byte.SIZE ),
                                            ( tmpFinalPos / Byte.SIZE ),
                                            ( tmpFinalPos % Byte.SIZE ) );
            }
            tmpFieldsString.addLine( tmpRecordViewItemMap.getName( ),
                                     tmpRecordViewItemMap.getType( ).name( ), //
                                     String.format( "%4d",
                                                    tmpBitLength ),
                                     tmpPieces );
        }
        return tmpFieldsString.getResult( 2,
                                          2 );
    }

    public static String dumpStoreData( BitStoreData bitStoreData ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "Mapa '%s'",
                          bitStoreData.getMap( ).getName( ) );
        for ( BitStoreViewData tmpViewData : bitStoreData.getViews( ) ) {
            tmpResult.appendIndented( BitStoreHelper.dumpStoreViewData( tmpViewData ) );
        }
        return tmpResult.getResult( );
    }

    public static String dumpStoreViewData( BitStoreViewData bitStoreViewData ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        int tmpBitLength = bitStoreViewData.getBitLength( );
        tmpResult.append( "%3d bits - %2d/%d bytes - Visão: %s",
                          tmpBitLength,
                          ( tmpBitLength / Byte.SIZE ),
                          ( tmpBitLength % Byte.SIZE ),
                          bitStoreViewData.getName( ) );
        for ( BitStoreRecordData tmpRecordData : bitStoreViewData.getRecords( ) ) {
            tmpResult.appendIndented( BitStoreHelper.dumpStoreRecordData( tmpRecordData ) );
        }
        return tmpResult.getResult( );
    }

    public static String dumpStoreRecordData( BitStoreRecordData bitStoreRecordData ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "%3d bits - %2d/%d bytes - Registro '%s'",
                          bitStoreRecordData.getBitLength( ),
                          ( bitStoreRecordData.getBitLength( ) / Byte.SIZE ),
                          ( bitStoreRecordData.getBitLength( ) % Byte.SIZE ),
                          bitStoreRecordData.getName( ) );
        tmpResult.increaseIndent( );
        String tmpRecordDump = null;
        try {
            tmpRecordDump = ByteHelper.dump( bitStoreRecordData.getBytes( ),
                                             ByteHelper.DumpMode.Hexa,
                                             16 );
        }
        catch ( BitStoreException e ) {
            tmpRecordDump = String.format( "Erro ao obter bytes: %s",
                                           e.getLocalizedMessage( ) );
        }
        tmpResult.append( tmpRecordDump );
        tmpResult.increaseIndent( );
        tmpResult.append( dumpStoreFieldsData( bitStoreRecordData ) );
        return tmpResult.getResult( );
    }

    public static String dumpStoreFieldsData( BitStoreRecordData bitStoreRecordData ) {
        ColumnsStringBuffer tmpFieldsString = new ColumnsStringBuffer( 30,
                                                                       15,
                                                                       9,
                                                                       38 );
        tmpFieldsString = new ColumnsStringBuffer( 6 );
        tmpFieldsString.addLine( "Campo",
                                 "Tipo",
                                 "QtdeBits",
                                 "Valor",
                                 "Posicao",
                                 "Stream" );
        tmpFieldsString.addSeparator( '-' );
        for ( BitStoreItemData tmpViewItemData : bitStoreRecordData.getItems( ) ) {
            String tmpValue;
            byte[ ] tmpBytes;
            try {
                tmpValue = tmpViewItemData.getAs( String.class );
                tmpBytes = tmpViewItemData.getBytes( );
            }
            catch ( BitStoreException e ) {
                tmpValue = e.getLocalizedMessage( );
                tmpBytes = new byte[ tmpViewItemData.getByteLength( ) ];
            }
            List< String > tmpPositions = new ArrayList< String >( );
            for ( BitStoreViewPieceMap tmpPiece : tmpViewItemData.getPieces( ) ) {
                int tmpStart = tmpPiece.getOffset( );
                int tmpEnd = ( tmpStart + tmpPiece.getLength( ) - 1 );
                tmpPositions.add( String.format( "%2d:%d a %2d:%d",
                                                 ( tmpStart / 8 ),
                                                 ( tmpStart % 8 ),
                                                 ( tmpEnd / 8 ),
                                                 ( tmpEnd % 8 ) ) );
            }
            tmpFieldsString.addLine( tmpViewItemData.getName( ),
                                     tmpViewItemData.getDataType( ).name( ), //
                                     Integer.toString( tmpViewItemData.getBitLength( ) ), //
                                     tmpValue,
                                     StringHelper.listToSeparated( ", ",
                                                                   tmpPositions ),
                                     BitHelper.bitArrayToString( tmpBytes,
                                                                 BitHelper.getBitArrayPadLength( tmpViewItemData.getBitLength( ),
                                                                                                 tmpBytes ),
                                                                 tmpViewItemData.getBitLength( ),
                                                                 false ) );
        }
        return tmpFieldsString.getResult( 2,
                                          2 );
    }
}
