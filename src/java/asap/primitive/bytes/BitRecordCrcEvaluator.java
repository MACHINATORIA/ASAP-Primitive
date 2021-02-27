package asap.primitive.bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import asap.primitive.bits.BitHelper;
import asap.primitive.bits.bitStore.BitStoreException;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreItemData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreRecordData;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewPieceMap;

public class BitRecordCrcEvaluator {

    public static class CrcInstance {

        public final BitStoreItemData    crcField;

        public final BitStoreItemData[ ] enclosedItems;

        public byte[ ]                   computedValue;

        public boolean                   valid;

        protected int                    enclosedBitStart;

        protected int                    enclosedBitCount;

        protected CrcInstance( BitStoreItemData crcField,
                               List< BitStoreItemData > enclosedItems ) {
            this.crcField = crcField;
            this.enclosedItems = enclosedItems.toArray( new BitStoreItemData[ 0 ] );
            this.enclosedBitStart = Integer.MAX_VALUE;
            this.enclosedBitCount = 0;
            for ( BitStoreItemData tmpItem : this.enclosedItems ) {
                for ( BitStoreViewPieceMap tmpPieceMap : tmpItem.getPieces( ) ) {
                    if ( this.enclosedBitStart > tmpPieceMap.getOffset( ) ) {
                        this.enclosedBitStart = tmpPieceMap.getOffset( );
                    }
                }
                this.enclosedBitCount += tmpItem.getBitLength( );
            }
            this.reset( );
        }

        public void reset( ) {
            this.computedValue = null;
            this.valid = false;
        }

        public void compute( )
            throws BitStoreException {
            byte[ ] tmpEnclosedBytes = BitHelper.createBitArrayBuffer( this.enclosedBitCount );
            BitHelper.bitArrayCopy( this.crcField.getParentRecord( ).getBytes( ),
                                    this.enclosedBitStart,
                                    tmpEnclosedBytes,
                                    0,
                                    this.enclosedBitCount );
            this.computedValue = ByteHelper.toBigEndian( CRC16BU.compute( tmpEnclosedBytes ),
                                                         2 );
            this.valid = Arrays.equals( this.crcField.getBytes( ),
                                        this.computedValue );
        }
    }

    public final BitRecordCrcEvaluator.CrcInstance[ ] crcInstances;

    public boolean                                    checked;

    public boolean                                    valid;

    public BitRecordCrcEvaluator( BitStoreRecordData record ) {
        List< BitRecordCrcEvaluator.CrcInstance > tmpCrcInstances = new ArrayList< BitRecordCrcEvaluator.CrcInstance >( );
        List< BitStoreItemData > tmpEnclosedItens = new ArrayList< BitStoreItemData >( );
        for ( BitStoreItemData tmpItem : record.getItems( ) ) {
            if ( tmpItem.getName( ).matches( "(crc|Crc|CRC)\\d*" ) ) {
                BitRecordCrcEvaluator.CrcInstance tmpCrcInstance = new CrcInstance( tmpItem,
                                                                                    tmpEnclosedItens );
                tmpCrcInstances.add( tmpCrcInstance );
                tmpEnclosedItens = new ArrayList< BitStoreItemData >( );
            }
            else {
                tmpEnclosedItens.add( tmpItem );
            }
        }
        this.crcInstances = tmpCrcInstances.toArray( new BitRecordCrcEvaluator.CrcInstance[ 0 ] );
        this.checked = false;
        this.valid = false;
    }

    public void reset( ) {
        for ( CrcInstance tmpInstance : this.crcInstances ) {
            tmpInstance.reset( );
        }
        this.checked = false;
        this.valid = false;
    }

    public void compute( )
        throws BitStoreException {
        boolean tmpValid = true;
        for ( CrcInstance tmpInstance : this.crcInstances ) {
            tmpInstance.compute( );
            if ( !tmpInstance.valid ) {
                tmpValid = false;
            }
        }
        this.checked = true;
        this.valid = tmpValid;
    }

    public static boolean validate( BitStoreRecordData record )
        throws BitStoreException {
        return new BitRecordCrcEvaluator( record ).valid;
    }

    public static void update( BitStoreRecordData record )
        throws BitStoreException {
        BitRecordCrcEvaluator tmpRecordCRC = new BitRecordCrcEvaluator( record );
        if ( !tmpRecordCRC.valid ) {
            for ( BitRecordCrcEvaluator.CrcInstance tmpInstance : tmpRecordCRC.crcInstances ) {
                tmpInstance.compute( );
                if ( !tmpInstance.valid ) {
                    tmpInstance.crcField.setBytes( tmpInstance.computedValue );
                }
            }
        }
    }
}
