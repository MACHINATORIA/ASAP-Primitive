package asap.primitive.bytes.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import asap.primitive.bytes.ByteHelper;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecContainerMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecDateFieldMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecDateTimeFieldMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecFileMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecItemDataType;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecItemLengthType;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecItemMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecRecordArrayMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecRecordMap;

public class ByteCodecData {

    @SuppressWarnings( "serial" )
    public static class ByteCodecDataException extends ByteCodecException {

        public ByteCodecDataException( Throwable cause ) {
            super( cause );
        }

        public ByteCodecDataException( String messageFormat,
                                       Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public ByteCodecDataException( Throwable cause,
                                       String messageFormat,
                                       Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    /*
     * 
     */
    public static abstract class ByteCodecItemData {

        public final ByteCodecItemMap  map;

        public final ByteCodecItemData parent;

        public final int               index;

        public final int               offset;

        protected Integer              length;

        protected ByteCodecItemData( ByteCodecItemMap map,
                                     ByteCodecItemData parent,
                                     int index,
                                     int offset ) {
            this.map = map;
            this.parent = parent;
            this.index = index;
            this.offset = offset;
            this.length = null;
        }

        public ByteCodecFileData getRoot( ) {
            return this.parent.getRoot( );
        }

        public String getName( ) {
            return this.map.name;
        }

        public String getPath( ) {
            if ( this.parent != null ) {
                return String.format( "%s.%s",
                                      this.parent.getPath( ),
                                      this.getName( ) );
            }
            return this.getName( );
        }

        public int getLength( ) {
            return this.length;
        }

        public byte[ ] getBytes( int offset,
                                 int length ) {
            return this.parent.getBytes( ( this.offset + offset ),
                                         length );
        }

        public byte[ ] getBytes( ) {
            return this.getBytes( 0,
                                  this.getLength( ) );
        }

        public Long getInteger( ) {
            switch ( this.map.dataType ) {
                case ByteArray:
                case Integer:
                case Date:
                case Time:
                    return ByteHelper.fromBigEndian( this.getBytes( ) );
                default:
                    break;
            }
            return null;
        }

        public Date getDate( ) {
            if ( ( this.map.dataType == ByteCodecItemDataType.Date )
                 || ( this.map.dataType == ByteCodecItemDataType.DateTime ) ) {
                byte[ ] tmpBytes = this.getBytes( );
                GregorianCalendar tmpCalendar = new GregorianCalendar( (int) ByteHelper.fromBigEndian( Arrays.copyOfRange( tmpBytes,
                                                                                                                           0,
                                                                                                                           2 ) ),
                                                                       (int) ( ( tmpBytes[ 2 ] > 0 ) ? ( tmpBytes[ 2 ]
                                                                                                         - 1 )
                                                                                                     : 0 ),
                                                                       (int) tmpBytes[ 3 ] );
                if ( this.map.dataType == ByteCodecItemDataType.DateTime ) {
                    tmpCalendar.add( Calendar.HOUR,
                                     (int) tmpBytes[ 4 ] );
                    tmpCalendar.add( Calendar.MINUTE,
                                     (int) tmpBytes[ 5 ] );
                    tmpCalendar.add( Calendar.SECOND,
                                     (int) tmpBytes[ 6 ] );
                }
                return tmpCalendar.getTime( );
            }
            return null;
        }

        public Integer getTime( ) {
            if ( ( this.map.dataType == ByteCodecItemDataType.Time )
                 || ( this.map.dataType == ByteCodecItemDataType.DateTime ) ) {
                byte[ ] tmpBytes = this.getBytes( );
                int tmpOffset = ( this.map.dataType == ByteCodecItemDataType.Time ) ? 0
                                                                                    : 4;
                return ( ( tmpBytes[ tmpOffset + 0 ] * 60 * 60 )
                         + ( tmpBytes[ tmpOffset + 1 ] * 60 )
                         + tmpBytes[ tmpOffset + 2 ] );
            }
            return null;
        }

        public String getString( ) {
            switch ( this.map.dataType ) {
                case ByteArray:
                    return ByteHelper.hexify( this.getBytes( ) );
                case Integer:
                    return String.format( "%,d",
                                          this.getInteger( ) );
                case Date:
                    return ByteCodecDateFieldMap.PRINT_DATE_FORMAT.format( this.getDate( ) );
                case Time:
                    int tmpTime = this.getTime( );
                    return String.format( "%02d:%02d:%02d",
                                          ( ( tmpTime / ( 60 * 60 ) ) % 24 ),
                                          ( ( tmpTime / 60 ) % 60 ),
                                          ( tmpTime % 60 ) );
                case DateTime:
                    return ByteCodecDateTimeFieldMap.PRINT_DATE_TIME_FORMAT.format( this.getDate( ) );
                case File:
                case Record: {
                    StringBuilder tmpBuffer = new StringBuilder( );
                    tmpBuffer.append( "{" );
                    boolean tmpFirstItem = true;
                    for ( ByteCodecItemData tmpItem : ( (ByteCodecContainerData) this ).items ) {
                        if ( tmpFirstItem ) {
                            tmpFirstItem = false;
                        }
                        else {
                            tmpBuffer.append( ", " );
                        }
                        tmpBuffer.append( String.format( "%s: %s",
                                                         tmpItem.map.name,
                                                         tmpItem.getString( ) ) );
                    }
                    tmpBuffer.append( "}" );
                    return tmpBuffer.toString( );
                }
                case RecordArray: {
                    StringBuilder tmpBuffer = new StringBuilder( );
                    tmpBuffer.append( "{" );
                    boolean tmpFirstItem = true;
                    for ( ByteCodecItemData tmpItem : ( (ByteCodecRecordArrayData) this ).arrayItems ) {
                        if ( tmpFirstItem ) {
                            tmpFirstItem = false;
                        }
                        else {
                            tmpBuffer.append( ", " );
                        }
                        tmpBuffer.append( String.format( "%s[%d]: %s",
                                                         tmpItem.map.name,
                                                         tmpItem.index,
                                                         tmpItem.getString( ) ) );
                    }
                    tmpBuffer.append( "}" );
                    return tmpBuffer.toString( );
                }
                default:
                    break;
            }
            return null;
        }

        @Override
        public String toString( ) {
            return String.format( "%s - %s - %s",
                                  this.map.dataType.name( ),
                                  this.map.name,
                                  this.getString( ) );
        }
    }

    public static class ByteCodecFieldData extends ByteCodecItemData {

        protected ByteCodecFieldData( ByteCodecItemMap map,
                                      ByteCodecContainerData parent,
                                      int index,
                                      int offset ) {
            super( map,
                   parent,
                   index,
                   offset );
            switch ( this.map.lengthType ) {
                case FixedLength:
                    this.length = this.map.fixedLength;
                    break;
                case PreviousField:
                    this.length = parent.items[ index - 1 ].getInteger( ).intValue( );
                    break;
                case ArbitraryField:
                    this.length = this.getRoot( ).getItem( this.map.lengthFieldName ).getInteger( ).intValue( );
                    break;
                //    case FirstInnerField:
                //    case SumOfInnerFields:
                //        break;
                case RemainingOfRecord:
                    this.length = ( parent.getLength( ) - offset );
                    break;
                default:
                    /*
                     * Tipo inválido de tamanho para item do tipo 'campo'
                     */
                    break;
            }
        }
    }

    public static class ByteCodecContainerData extends ByteCodecItemData {

        protected static final String     INDEXED_NAME_REGEX = "(\\w+)\\[(\\d+)\\]";

        public final ByteCodecItemData[ ] items;

        protected ByteCodecContainerData( ByteCodecContainerMap map ) {
            super( map,
                   null,
                   0,
                   0 );
            this.items = new ByteCodecItemData[ map.items.length ];
        }

        protected ByteCodecContainerData( ByteCodecContainerMap map,
                                          ByteCodecItemData parent,
                                          int index,
                                          int offset )
            throws ByteCodecDataException {
            super( map,
                   parent,
                   index,
                   offset );
            this.items = new ByteCodecItemData[ map.items.length ];
            this.engage( );
        }

        public ByteCodecItemData getItem( String path ) {
            return this.getItem( path.replaceAll( " +",
                                                  "" ).split( "\\[|(\\]\\.?)|\\." ) );
        }

        public ByteCodecItemData getItem( String[ ] path ) {
            ByteCodecItemData tmpResult = null;
            for ( ByteCodecItemData tmpItem : this.items ) {
                if ( tmpItem.map.name.compareTo( path[ 0 ] ) == 0 ) {
                    if ( path.length == 1 ) {
                        tmpResult = tmpItem;
                    }
                    else {
                        String[ ] tmpSubPath = Arrays.copyOfRange( path,
                                                                   1,
                                                                   path.length );
                        if ( tmpItem instanceof ByteCodecContainerData ) {
                            tmpResult = ( (ByteCodecContainerData) tmpItem ).getItem( tmpSubPath );
                        }
                        else if ( tmpItem instanceof ByteCodecRecordArrayData ) {
                            int tmpIndex = Integer.parseInt( tmpSubPath[ 0 ] );
                            tmpResult = ( (ByteCodecRecordArrayData) tmpItem ).arrayItems[ tmpIndex ];
                            if ( tmpSubPath.length > 1 ) {
                                if ( tmpResult instanceof ByteCodecContainerData ) {
                                    tmpSubPath = Arrays.copyOfRange( tmpSubPath,
                                                                     1,
                                                                     tmpSubPath.length );
                                    tmpResult = ( (ByteCodecContainerData) tmpResult ).getItem( tmpSubPath );
                                }
                                else {
                                    tmpResult = null;
                                }
                            }
                        }
                        else {
                            tmpResult = null;
                        }
                    }
                    break;
                }
            }
            return tmpResult;
        }

        protected void engage( )
            throws ByteCodecDataException {
            //
            switch ( this.map.lengthType ) {
                case FixedLength:
                    this.length = this.map.fixedLength;
                    break;
                case PreviousField:
                    this.length = ( (ByteCodecContainerData) this.parent ).items[ this.index
                                                                                  - 1 ].getInteger( ).intValue( );
                    break;
                case ArbitraryField:
                    this.length = this.getRoot( ).getItem( this.map.lengthFieldName ).getInteger( ).intValue( );
                    break;
                case FirstInnerField:
                case SumOfInnerFields:
                    break;
                case RemainingOfRecord:
                    this.length = ( this.parent.getLength( ) - this.offset );
                    break;
                default:
                    /*
                     * Tipo inválido de tamanho para item do tipo 'container'
                     */
                    break;
            }
            //
            int tmpOffset = 0;
            ByteCodecContainerMap tmpMap = (ByteCodecContainerMap) this.map;
            for ( int tmpItemIndex = 0; tmpItemIndex < this.items.length; tmpItemIndex++ ) {
                ByteCodecItemData tmpItem = null;
                switch ( tmpMap.items[ tmpItemIndex ].dataType ) {
                    case ByteArray:
                    case Integer:
                    case Date:
                    case Time:
                    case DateTime:
                        tmpItem = new ByteCodecFieldData( tmpMap.items[ tmpItemIndex ],
                                                          this,
                                                          tmpItemIndex,
                                                          tmpOffset );
                        break;
                    case Record:
                        tmpItem = new ByteCodecRecordData( (ByteCodecRecordMap) tmpMap.items[ tmpItemIndex ],
                                                           this,
                                                           tmpItemIndex,
                                                           tmpOffset );
                        break;
                    case RecordArray:
                        tmpItem = new ByteCodecRecordArrayData( (ByteCodecRecordArrayMap) tmpMap.items[ tmpItemIndex ],
                                                                this,
                                                                tmpItemIndex,
                                                                tmpOffset );
                        break;
                    //    case File:
                    //        break;
                    default:
                        /*
                         * Tipo inválido de tamanho para item do tipo 'registro'
                         */
                        break;
                }
                tmpOffset += tmpItem.getLength( );
                this.items[ tmpItemIndex ] = tmpItem;
                if ( ( tmpItemIndex == 0 ) && ( this.map.lengthType == ByteCodecItemLengthType.FirstInnerField ) ) {
                    this.length = tmpItem.getInteger( ).intValue( );
                }
            }
            if ( this.map.lengthType == ByteCodecItemLengthType.SumOfInnerFields ) {
                this.length = tmpOffset;
            }
            if ( this.length == null ) {
                throw new ByteCodecDataException( "Tamanho indefinido para '%s'",
                                                  this.getPath( ) );
            }
            else if ( tmpOffset != this.length ) {
                throw new ByteCodecDataException( "Tamanho dos items incoerente com o tamanho de '%s'",
                                                  this.getPath( ) );
            }
        }
    }

    public static class ByteCodecRecordData extends ByteCodecContainerData {

        protected ByteCodecRecordData( ByteCodecRecordMap map,
                                       ByteCodecItemData parent,
                                       int index,
                                       int offset )
            throws ByteCodecDataException {
            super( map,
                   parent,
                   index,
                   offset );
        }
    }

    public static class ByteCodecRecordArrayData extends ByteCodecItemData {

        public final ByteCodecRecordData[ ] arrayItems;

        protected ByteCodecRecordArrayData( ByteCodecRecordArrayMap map,
                                            ByteCodecContainerData parent,
                                            int index,
                                            int offset )
            throws ByteCodecDataException {
            super( map,
                   parent,
                   index,
                   offset );
            //
            Integer tmpArrayLength = null;
            switch ( this.map.lengthType ) {
                case FixedLength:
                    tmpArrayLength = this.map.fixedLength;
                    break;
                case PreviousField:
                    tmpArrayLength = parent.items[ index - 1 ].getInteger( ).intValue( );
                    break;
                case ArbitraryField:
                    tmpArrayLength = this.getRoot( ).getItem( this.map.lengthFieldName ).getInteger( ).intValue( );
                    break;
                //    case FirstInnerField:
                //    case SumOfInnerFields:
                //        break;
                case RemainingOfRecord:
                    break;
                default:
                    /*
                     * Tipo inválido de tamanho para item do tipo 'array de registros'
                     */
                    break;
            }
            List< ByteCodecRecordData > tmpElements = new ArrayList< ByteCodecRecordData >( );
            int tmpOffset = 0;
            while ( ( tmpArrayLength == null ) || ( tmpArrayLength > 0 ) ) {
                ByteCodecRecordData tmpElement = new ByteCodecRecordData( map.elementMap,
                                                                          this,
                                                                          tmpElements.size( ),
                                                                          tmpOffset );
                tmpElements.add( tmpElement );
                tmpOffset += tmpElement.getLength( );
                if ( tmpArrayLength != null ) {
                    --tmpArrayLength;
                }
                else if ( tmpOffset >= this.parent.getLength( ) ) {
                    tmpArrayLength = 0;
                }
            }
            this.arrayItems = tmpElements.toArray( new ByteCodecRecordData[ 0 ] );
            this.length = tmpOffset;
        }

        public ByteCodecItemData getItem( String[ ] path ) {
            ByteCodecItemData tmpResult = null;
            if ( path[ 0 ].matches( "\\d+" ) ) {
                int tmpIndex = Integer.parseInt( path[ 0 ] );
                if ( tmpIndex < this.arrayItems.length ) {
                    tmpResult = this.arrayItems[ tmpIndex ];
                    if ( path.length > 1 ) {
                        tmpResult = ( tmpResult instanceof ByteCodecContainerData ) ? ( (ByteCodecContainerData) tmpResult ).getItem( Arrays.copyOfRange( path,
                                                                                                                                                          1,
                                                                                                                                                          path.length ) )
                                                                                    : null;
                    }
                }
            }
            return tmpResult;
        }
    }

    public static class ByteCodecFileData extends ByteCodecContainerData {

        public final byte[ ] buffer;

        public ByteCodecFileData( ByteCodecFileMap fileMap,
                                  byte[ ] buffer )
            throws ByteCodecDataException {
            super( fileMap );
            this.buffer = ByteHelper.copyOf( buffer );
            this.engage( );
            if ( this.length != buffer.length ) {
                throw new ByteCodecDataException( "Dados inválidos para '%s'",
                                                  fileMap.name );
            }
        }

        @Override
        public int getLength( ) {
            return this.buffer.length;
        }

        @Override
        public ByteCodecFileData getRoot( ) {
            return this;
        }

        @Override
        public byte[ ] getBytes( int offset,
                                 int length ) {
            return Arrays.copyOfRange( this.buffer,
                                       offset,
                                       ( offset + length ) );
        }
    }
}
