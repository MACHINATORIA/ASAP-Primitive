package asap.primitive.bits.bitStore;

import asap.primitive.bits.BitHelper;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreDataException;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreMapException;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreRecordMap;
import asap.primitive.bits.bitStore.BitStoreMap.BitStoreViewPieceMap;
import asap.primitive.bytes.ByteHelper;

public abstract class BitStoreSource {

    @SuppressWarnings( "serial" )
    public static class BitStoreSourceException extends BitStoreException {

        public BitStoreSourceException( Throwable cause ) {
            super( cause );
        }

        public BitStoreSourceException( String messageFormat,
                                        Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreSourceException( Throwable cause,
                                        String messageFormat,
                                        Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @SuppressWarnings( "serial" )
    public static class BitStoreSetupException extends BitStoreSourceException {

        public BitStoreSetupException( Throwable cause ) {
            super( cause );
        }

        public BitStoreSetupException( String messageFormat,
                                       Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreSetupException( Throwable cause,
                                       String messageFormat,
                                       Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @SuppressWarnings( "serial" )
    public static class BitStoreDeviceException extends BitStoreSourceException {

        public BitStoreDeviceException( Throwable cause ) {
            super( cause );
        }

        public BitStoreDeviceException( String messageFormat,
                                        Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreDeviceException( Throwable cause,
                                        String messageFormat,
                                        Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @SuppressWarnings( "serial" )
    public static class BitStoreSessionException extends BitStoreSourceException {

        public BitStoreSessionException( Throwable cause ) {
            super( cause );
        }

        public BitStoreSessionException( String messageFormat,
                                         Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreSessionException( Throwable cause,
                                         String messageFormat,
                                         Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @SuppressWarnings( "serial" )
    public static class BitStoreAuthenticationException extends BitStoreSourceException {

        public BitStoreAuthenticationException( Throwable cause ) {
            super( cause );
        }

        public BitStoreAuthenticationException( String messageFormat,
                                                Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreAuthenticationException( Throwable cause,
                                                String messageFormat,
                                                Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @SuppressWarnings( "serial" )
    public static class BitStoreAccessException extends BitStoreSourceException {

        public BitStoreAccessException( Throwable cause ) {
            super( cause );
        }

        public BitStoreAccessException( String messageFormat,
                                        Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public BitStoreAccessException( Throwable cause,
                                        String messageFormat,
                                        Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    public static abstract class BitStoreRecordSource {

        public final String         name;

        public final BitStoreSource parentStore;

        protected BitStoreRecordSource( String name,
                                        BitStoreSource parentStore ) {
            this.name = name;
            this.parentStore = parentStore;
        }

        public abstract String[ ] describe( boolean isLittleEndianBits )
            throws BitStoreMapException;

        public abstract String[ ] describe( BitStoreViewPieceMap pieceMap )
            throws BitStoreMapException;

        public abstract void getPiece( BitStoreViewPieceMap pieceMap,
                                       boolean isLittleEndianBits,
                                       byte[ ] targetArray,
                                       int targetOffset )
            throws BitStoreMapException,
                BitStoreDataException,
                BitStoreSetupException,
                BitStoreDeviceException,
                BitStoreSessionException,
                BitStoreAuthenticationException,
                BitStoreAccessException;

        public abstract void setPiece( BitStoreViewPieceMap pieceMap,
                                       boolean isLittleEndianBits,
                                       byte[ ] sourceArray,
                                       int sourceOffset )
            throws BitStoreMapException,
                BitStoreDataException,
                BitStoreSetupException,
                BitStoreDeviceException,
                BitStoreSessionException,
                BitStoreAuthenticationException,
                BitStoreAccessException;

        public abstract void flush( )
            throws BitStoreMapException,
                BitStoreDataException,
                BitStoreSetupException,
                BitStoreDeviceException,
                BitStoreSessionException,
                BitStoreAuthenticationException,
                BitStoreAccessException;
    }

    public static class BitStoreRecordMemorySource extends BitStoreRecordSource {

        protected final byte[ ]       recordData;

        protected static final String DESCRIPTION = "Memoria";

        protected BitStoreRecordMemorySource( BitStoreRecordMap bitStoreRecordMap,
                                              BitStoreSource parentStoreSource ) {
            super( bitStoreRecordMap.getName( ),
                   parentStoreSource );
            this.recordData = new byte[ bitStoreRecordMap.getByteLength( ) ];
        }

        @Override
        public String[ ] describe( boolean isLittleEndianBits )
            throws BitStoreMapException {
            return new String[ ] { DESCRIPTION };
        }

        @Override
        public String[ ] describe( BitStoreViewPieceMap pieceMap )
            throws BitStoreMapException {
            return this.describe( pieceMap.getParentViewItem( ).getParentViewRecord( ).isLittleEndianBits( ) );
        }

        @Override
        public void getPiece( BitStoreViewPieceMap pieceMap,
                              boolean isLittleEndianBits,
                              byte[ ] targetArray,
                              int targetOffset ) {
            BitHelper.bitArrayCopy( isLittleEndianBits,
                                    this.recordData,
                                    pieceMap.offset,
                                    targetArray,
                                    targetOffset,
                                    pieceMap.length );
        }

        @Override
        public void setPiece( BitStoreViewPieceMap pieceMap,
                              boolean isLittleEndianBits,
                              byte[ ] sourceArray,
                              int sourceOffset ) {
            BitHelper.bitArrayCopy( isLittleEndianBits,
                                    sourceArray,
                                    sourceOffset,
                                    this.recordData,
                                    pieceMap.offset,
                                    pieceMap.length );
        }

        @Override
        public void flush( ) {
        }
    }

    public static class BitStoreMemorySource extends BitStoreSource {

        protected final BitStoreRecordSource[ ] records;

        public BitStoreMemorySource( BitStoreMap bitStoreMap ) {
            this( bitStoreMap,
                  null );
        }

        public BitStoreMemorySource( BitStoreMap bitStoreMap,
                                     byte[ ] mediaId ) {
            super( "MemorySource",
                   "Fonte de dados em memória",
                   bitStoreMap.getName( ),
                   bitStoreMap.getDescription( ),
                   mediaId );
            this.records = new BitStoreRecordSource[ bitStoreMap.getRecords( ).length ];
            for ( int tmpIndex = 0; tmpIndex < this.records.length; tmpIndex++ ) {
                this.records[ tmpIndex ] = new BitStoreRecordMemorySource( bitStoreMap.getRecords( )[ tmpIndex ],
                                                                           this );
            }
        }

        @Override
        public byte[ ] getMediaId( ) {
            return ByteHelper.copyOf( this.mediaId );
        }

        @Override
        public boolean isActive( ) {
            return true;
        }

        @Override
        public void clearCaches( ) {
        }

        @Override
        protected BitStoreRecordSource[ ] getRecords( ) {
            return this.records;
        }
    }

    public final String     name;

    public final String     description;

    public final String     mapName;

    public final String     mapDescription;

    protected final byte[ ] mediaId;

    public byte[ ] getMediaId( ) {
        return ByteHelper.copyOf( this.mediaId );
    }

    public abstract boolean isActive( );

    public abstract void clearCaches( );

    protected BitStoreSource( String name,
                              String description,
                              String mapName,
                              String mapDescription,
                              byte[ ] mediaId ) {
        this.name = name;
        this.description = description;
        this.mapName = mapName;
        this.mapDescription = mapDescription;
        this.mediaId = ByteHelper.copyOf( mediaId );
    }

    protected abstract BitStoreRecordSource[ ] getRecords( );

    public BitStoreRecordSource getRecord( String recordName )
        throws BitStoreMapException {
        for ( BitStoreRecordSource tmpRecord : this.getRecords( ) ) {
            if ( tmpRecord.name.compareTo( recordName ) == 0 ) {
                return tmpRecord;
            }
        }
        throw new BitStoreMapException( "Não há registro '%s' em '%s' para o mapa '%s'",
                                        recordName,
                                        this.name,
                                        this.mapName );
    }

    public void flush( )
        throws BitStoreMapException,
            BitStoreDataException,
            BitStoreSetupException,
            BitStoreDeviceException,
            BitStoreSessionException,
            BitStoreAuthenticationException,
            BitStoreAccessException {
        this.flush( false );
    }

    public void flush( boolean force )
        throws BitStoreMapException,
            BitStoreDataException,
            BitStoreSetupException,
            BitStoreDeviceException,
            BitStoreSessionException,
            BitStoreAuthenticationException,
            BitStoreAccessException {
        for ( BitStoreRecordSource tmpRecord : this.getRecords( ) ) {
            tmpRecord.flush( );
        }
    }
}
