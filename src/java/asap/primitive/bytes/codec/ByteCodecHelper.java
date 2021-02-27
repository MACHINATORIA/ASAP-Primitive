package asap.primitive.bytes.codec;

import asap.primitive.bytes.ByteHelper;
import asap.primitive.bytes.codec.ByteCodecData.ByteCodecContainerData;
import asap.primitive.bytes.codec.ByteCodecData.ByteCodecFileData;
import asap.primitive.bytes.codec.ByteCodecData.ByteCodecItemData;
import asap.primitive.bytes.codec.ByteCodecData.ByteCodecRecordArrayData;
import asap.primitive.bytes.codec.ByteCodecData.ByteCodecRecordData;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecContainerMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecFileMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecItemDataType;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecItemMap;
import asap.primitive.bytes.codec.ByteCodecMap.ByteCodecRecordArrayMap;
import asap.primitive.string.ColumnsStringBuffer;
import asap.primitive.string.IndentedStringBuilder;
import asap.primitive.string.StringHelper;

public class ByteCodecHelper {

    protected static final int DUMP_INDENT_LENGTH = 2;

    protected static String dumpItemLength( ByteCodecItemMap map ) {
        String tmpLengthType = "";
        switch ( map.lengthType ) {
            case FixedLength:
                tmpLengthType = String.format( "Fixo em %d",
                                               map.fixedLength );
                break;
            case PreviousField:
                tmpLengthType = "Campo anterior";
                break;
            case ArbitraryField:
                tmpLengthType = String.format( "Campo '%s'",
                                               map.lengthFieldName );
                break;
            case FirstInnerField:
                tmpLengthType = "Primeiro campo";
                break;
            case SumOfInnerFields:
                tmpLengthType = "Soma dos campos";
                break;
            case RemainingOfRecord:
                tmpLengthType = "Restante do registro";
                break;
            default:
                break;
        }
        return tmpLengthType;
    }

    protected static void appendItemMap( int level,
                                         ColumnsStringBuffer buffer,
                                         ByteCodecItemMap itemMap ) {
        String tmpItemIndent = StringHelper.spaces( level * DUMP_INDENT_LENGTH );
        ++level;
        buffer.addLine( tmpItemIndent.concat( itemMap.name ),
                        itemMap.dataType.name( ),
                        dumpItemLength( itemMap ) );
        switch ( itemMap.dataType ) {
            case File:
            case Record:
                for ( ByteCodecItemMap tmpItem : ( (ByteCodecContainerMap) itemMap ).items ) {
                    appendItemMap( level,
                                   buffer,
                                   tmpItem );
                }
                break;
            case RecordArray:
                for ( ByteCodecItemMap tmpItem : ( (ByteCodecRecordArrayMap) itemMap ).elementMap.items ) {
                    appendItemMap( level,
                                   buffer,
                                   tmpItem );
                }
                break;
            default:
                break;
        }
    }

    public static String dumpCodecMap( ByteCodecFileMap fileMap ) {
        ColumnsStringBuffer tmpItemBuffer = new ColumnsStringBuffer( 3 );
        tmpItemBuffer.addLine( "Nome",
                               "Tipo",
                               "Tamanho" );
        tmpItemBuffer.addSeparator( '-' );
        appendItemMap( 0,
                       tmpItemBuffer,
                       fileMap );
        return tmpItemBuffer.getResult( );
    }

    protected static ColumnsStringBuffer appendItemData( int level,
                                                         ColumnsStringBuffer buffer,
                                                         ByteCodecItemData itemData ) {
        String tmpItemIndent = StringHelper.spaces( level * DUMP_INDENT_LENGTH );
        ++level;
        String tmpItemName = itemData.map.name;
        String tmpItemData = "";
        String tmpItemDump = ByteHelper.dump( itemData.getBytes( ),
                                              16 );
        switch ( itemData.map.dataType ) {
            case ByteArray:
                tmpItemData = tmpItemDump;
                tmpItemDump = "";
                break;
            case Integer:
            case Date:
            case Time:
            case DateTime:
                tmpItemData = itemData.getString( );
                break;
            case Record:
                if ( itemData.parent.map.dataType == ByteCodecItemDataType.RecordArray ) {
                    tmpItemName += String.format( "[%d]",
                                                  itemData.index );
                }
                tmpItemName += " (authenticatorRecord)";
                tmpItemDump = "";
                break;
            case RecordArray:
                tmpItemName += " (array)";
                tmpItemDump = "";
                break;
            case File:
                tmpItemName += " (file)";
                tmpItemDump = "";
                break;
            default:
                break;
        }
        buffer.addLine( tmpItemIndent.concat( tmpItemName ),
                        tmpItemData,
                        tmpItemDump );
        switch ( itemData.map.dataType ) {
            case File:
            case Record:
                for ( ByteCodecItemData tmpItem : ( (ByteCodecContainerData) itemData ).items ) {
                    appendItemData( level,
                                    buffer,
                                    tmpItem );
                }
                break;
            case RecordArray:
                for ( ByteCodecRecordData tmpItem : ( (ByteCodecRecordArrayData) itemData ).arrayItems ) {
                    appendItemData( level,
                                    buffer,
                                    tmpItem );
                }
                break;
            default:
                break;
        }
        return buffer;
    }

    public static String dumpCodecData( ByteCodecFileData fileData ) {
        IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
        tmpResult.append( "Codec: %s",
                          fileData.map.name );
        tmpResult.increaseIndent( );
        tmpResult.append( "Bytes:" );
        tmpResult.appendIndented( ByteHelper.dump( fileData.buffer,
                                                   32 ) );
        tmpResult.append( "Decode:" );
        tmpResult.appendIndented( appendItemData( 1,
                                                  new ColumnsStringBuffer( 3 ),
                                                  fileData ).getResult( ) );
        return tmpResult.getResult( );
    }
}
