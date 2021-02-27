package asap.primitive.bytes.codec;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import asap.primitive.string.IndentedStringBuilder;

public class ByteCodecMap {

    @SuppressWarnings( "serial" )
    public static class ByteCodecMapException extends ByteCodecException {

        public ByteCodecMapException( Throwable cause ) {
            super( cause );
        }

        public ByteCodecMapException( String messageFormat,
                                      Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public ByteCodecMapException( Throwable cause,
                                      String messageFormat,
                                      Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    public static enum ByteCodecItemDataType {
        Invalid,
        ByteArray,
        Integer,
        Date,
        Time,
        DateTime,
        Record,
        RecordArray,
        File
    }

    public static enum ByteCodecItemLengthType {
        Invalid,
        FixedLength, //      Record/field
        PreviousField, //    Record/field
        ArbitraryField, //   Record/field
        FirstInnerField, //  Record
        SumOfInnerFields, // Record
        RemainingOfRecord // Record/field
    }

    public static abstract class ByteCodecItemMap {

        public final String                  name;

        public final ByteCodecItemDataType   dataType;

        public final ByteCodecItemLengthType lengthType;

        public final Integer                 fixedLength;

        public final String                  lengthFieldName;

        protected ByteCodecContainerMap      parent;

        protected ByteCodecItemMap( String name,
                                    ByteCodecItemDataType dataType,
                                    ByteCodecItemLengthType lengthType,
                                    Integer fixedLength,
                                    String lengthFieldName ) {
            this.name = name;
            this.dataType = dataType;
            this.lengthType = lengthType;
            this.fixedLength = fixedLength;
            this.lengthFieldName = lengthFieldName;
            this.parent = null;
        }

        protected ByteCodecItemMap( String name,
                                    ByteCodecItemDataType dataType,
                                    int fixedLength ) {
            this( name,
                  dataType,
                  ByteCodecItemLengthType.FixedLength,
                  fixedLength,
                  null );
        }

        protected ByteCodecItemMap( String name,
                                    ByteCodecItemDataType dataType,
                                    ByteCodecItemLengthType lengthType ) {
            this( name,
                  dataType,
                  lengthType,
                  null,
                  null );
        }

        protected ByteCodecItemMap( String name,
                                    ByteCodecItemDataType dataType,
                                    String lengthFieldName ) {
            this( name,
                  dataType,
                  ByteCodecItemLengthType.ArbitraryField,
                  null,
                  lengthFieldName );
        }

        public ByteCodecFileMap getRoot( ) {
            return this.parent.getRoot( );
        }

        public String getPath( ) {
            return String.format( "%s.%s",
                                  this.parent.getPath( ),
                                  this.name );
        }

        protected void engage( ByteCodecContainerMap parent,
                               int index )
            throws ByteCodecMapException {
            this.parent = parent;
            if ( ( index > 0 )
                 && ( parent.items[ index - 1 ].lengthType == ByteCodecItemLengthType.RemainingOfRecord ) ) {
                throw new ByteCodecMapException( "Campo '%s' configurado com tamanho '%s' não é o último",
                                                 this.name,
                                                 parent.items[ index - 1 ].lengthType.name( ) );
            }
            switch ( this.lengthType ) {
                case FixedLength:
                    if ( ( this.fixedLength == null ) || ( this.fixedLength < 0 ) ) {
                        throw new ByteCodecMapException( "Omitido o tamanho constante para o campo '%s'",
                                                         this.name );
                    }
                    break;
                case PreviousField:
                    if ( index < 1 ) {
                        throw new ByteCodecMapException( "Não há campo anterior a '%s' para definir seu tamanho",
                                                         this.name );
                    }
                    break;
                case ArbitraryField:
                    if ( this.lengthFieldName == null ) {
                        throw new ByteCodecMapException( "Omitido o nome do campo para definir o tamanho de '%s'",
                                                         this.name );
                    }
                    else {
                        ByteCodecItemMap tmpLengthField = this.getRoot( ).getItem( this.lengthFieldName );
                        if ( tmpLengthField == null ) {
                            throw new ByteCodecMapException( "Não existe o campo '%s' para definir o tamanho de '%s'",
                                                             this.lengthFieldName,
                                                             this.name );
                        }
                        else if ( tmpLengthField.dataType != ByteCodecItemDataType.Integer ) {
                            throw new ByteCodecMapException( "Tipo do campo '%s' não é válido para definir o tamanho de '%s'",
                                                             this.lengthFieldName,
                                                             this.name );
                        }
                    }
                    break;
                case FirstInnerField:
                case SumOfInnerFields:
                    if ( ( this.dataType != ByteCodecItemDataType.Record )
                         && ( this.dataType != ByteCodecItemDataType.File ) ) {
                        throw new ByteCodecMapException( "Configuração '%s' incoerente para o tamanho do campo '%s'",
                                                         this.lengthType.name( ),
                                                         this.name );
                    }
                    break;
                case RemainingOfRecord:
                    if ( this.parent == null ) {
                        throw new ByteCodecMapException( "Configuração de tamanho '%s' incoerente para o campo raiz '%s'",
                                                         this.lengthType.name( ),
                                                         this.name );
                    }
                    else {
                        if ( ( this.parent.lengthType == ByteCodecItemLengthType.SumOfInnerFields )
                             && ( this.dataType != ByteCodecItemDataType.RecordArray ) ) {
                            throw new ByteCodecMapException( "Configuração '%s' do campo '%s' incoerente com '%s' do ancestral '%s'",
                                                             this.lengthFieldName,
                                                             this.name,
                                                             this.parent.lengthType.name( ),
                                                             this.parent.name );
                        }
                    }
                    break;
                default:
                    throw new ByteCodecMapException( "Configuração '%s' do campo '%s' não foi identificada",
                                                     this.lengthType.name( ),
                                                     this.name );
            }
        }

        @Override
        public String toString( ) {
            return String.format( "name: %s, dataType: %s, lengthType: %s, fixedLength: %s, lenghtField: %s",
                                  this.name,
                                  this.dataType.name( ),
                                  this.lengthType.name( ),
                                  this.fixedLength == null ? "null"
                                                           : Integer.toString( this.fixedLength ),
                                  this.lengthFieldName == null ? "null"
                                                               : lengthFieldName );
        }
    }

    public static class ByteCodecByteArrayFieldMap extends ByteCodecItemMap {

        public ByteCodecByteArrayFieldMap( String name,
                                           int byteLength ) {
            super( name,
                   ByteCodecItemDataType.ByteArray,
                   byteLength );
        }

        public ByteCodecByteArrayFieldMap( String name,
                                           String lengthFieldName ) {
            super( name,
                   ByteCodecItemDataType.ByteArray,
                   lengthFieldName );
        }

        public ByteCodecByteArrayFieldMap( String name,
                                           ByteCodecItemLengthType lengthType ) {
            super( name,
                   ByteCodecItemDataType.ByteArray,
                   lengthType );
        }
    }

    public static class ByteCodecIntegerFieldMap extends ByteCodecItemMap {

        public ByteCodecIntegerFieldMap( String name,
                                         int byteLength ) {
            super( name,
                   ByteCodecItemDataType.Integer,
                   byteLength );
        }
    }

    public static class ByteCodecDateFieldMap extends ByteCodecItemMap {

        public static SimpleDateFormat ORDERED_DATE_FORMAT = new SimpleDateFormat( "yyyyMMdd" );

        public static SimpleDateFormat PRINT_DATE_FORMAT   = new SimpleDateFormat( "dd/MM/yyyy" );

        public ByteCodecDateFieldMap( String name ) {
            super( name,
                   ByteCodecItemDataType.Date,
                   4 );
        }
    }

    public static class ByteCodecTimeFieldMap extends ByteCodecItemMap {

        public ByteCodecTimeFieldMap( String name ) {
            super( name,
                   ByteCodecItemDataType.Time,
                   3 );
        }
    }

    public static class ByteCodecDateTimeFieldMap extends ByteCodecItemMap {

        public static SimpleDateFormat ORDERED_DATE_TIME_FORMAT = new SimpleDateFormat( "yyyyMMddhhmm" );

        public static SimpleDateFormat PRINT_DATE_TIME_FORMAT   = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );

        public ByteCodecDateTimeFieldMap( String name ) {
            super( name,
                   ByteCodecItemDataType.DateTime,
                   7 );
        }
    }

    public static class ByteCodecContainerMap extends ByteCodecItemMap {

        public ByteCodecItemMap[ ] items;

        protected ByteCodecContainerMap( String name,
                                         ByteCodecItemDataType dataType,
                                         int fixedLength,
                                         ByteCodecItemMap... items ) {
            super( name,
                   dataType,
                   fixedLength );
            this.items = Arrays.copyOf( items,
                                        items.length );
        }

        protected ByteCodecContainerMap( String name,
                                         ByteCodecItemDataType dataType,
                                         ByteCodecItemLengthType lengthType,
                                         ByteCodecItemMap... items ) {
            super( name,
                   dataType,
                   lengthType );
            this.items = Arrays.copyOf( items,
                                        items.length );
        }

        protected ByteCodecContainerMap( String name,
                                         ByteCodecItemDataType dataType,
                                         String lengthFieldName,
                                         ByteCodecItemMap... items ) {
            super( name,
                   dataType,
                   lengthFieldName );
            this.items = Arrays.copyOf( items,
                                        items.length );
        }

        @Override
        protected void engage( ByteCodecContainerMap parent,
                               int index )
            throws ByteCodecMapException {
            super.engage( parent,
                          index );
            int tmpIndex = 0;
            for ( ByteCodecItemMap tmpItem : this.items ) {
                tmpItem.engage( this,
                                tmpIndex );
                ++tmpIndex;
            }
        }

        public String getPath( ) {
            return ( this.parent.parent != null ) ? String.format( "%s.%s",
                                                                   this.parent.getPath( ),
                                                                   this.name )
                                                  : this.name;
        }

        public ByteCodecItemMap getItem( String[ ] path ) {
            ByteCodecItemMap tmpResult = null;
            for ( ByteCodecItemMap tmpItem : this.items ) {
                if ( tmpItem.name.compareTo( path[ 0 ] ) == 0 ) {
                    if ( path.length == 1 ) {
                        tmpResult = tmpItem;
                    }
                    else {
                        tmpResult = ( tmpItem instanceof ByteCodecContainerMap ) ? ( (ByteCodecContainerMap) tmpItem ).getItem( Arrays.copyOfRange( path,
                                                                                                                                                    1,
                                                                                                                                                    path.length ) )
                                                                                 : null;
                    }
                    break;
                }
            }
            return tmpResult;
        }

        public ByteCodecItemMap getItem( String path ) {
            return this.getItem( path.replaceAll( " +",
                                                  "" ).split( "\\." ) );
        }

        @Override
        public String toString( ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( super.toString( ) );
            for ( ByteCodecItemMap tmpItem : this.items ) {
                tmpResult.appendIndented( tmpItem.toString( ) );
            }
            return tmpResult.getResult( );
        }
    }

    public static class ByteCodecRecordMap extends ByteCodecContainerMap {

        public ByteCodecRecordMap( String name,
                                   int fixedLength,
                                   ByteCodecItemMap... items ) {
            super( name,
                   ByteCodecItemDataType.Record,
                   fixedLength,
                   items );
        }

        public ByteCodecRecordMap( String name,
                                   ByteCodecItemLengthType lengthType,
                                   ByteCodecItemMap... items ) {
            super( name,
                   ByteCodecItemDataType.Record,
                   lengthType,
                   items );
        }

        public ByteCodecRecordMap( String name,
                                   String lengthFieldName,
                                   ByteCodecItemMap... items ) {
            super( name,
                   ByteCodecItemDataType.Record,
                   lengthFieldName,
                   items );
        }
    }

    public static class ByteCodecRecordArrayMap extends ByteCodecItemMap {

        public final ByteCodecRecordMap elementMap;

        public ByteCodecRecordArrayMap( String name,
                                        int fixedLength,
                                        ByteCodecRecordMap elementMap ) {
            super( name,
                   ByteCodecItemDataType.RecordArray,
                   fixedLength );
            this.elementMap = elementMap;
        }

        public ByteCodecRecordArrayMap( String name,
                                        ByteCodecItemLengthType lengthType,
                                        ByteCodecRecordMap elementMap ) {
            super( name,
                   ByteCodecItemDataType.RecordArray,
                   lengthType );
            this.elementMap = elementMap;
        }

        public ByteCodecRecordArrayMap( String name,
                                        String lengthFieldName,
                                        ByteCodecRecordMap elementMap ) {
            super( name,
                   ByteCodecItemDataType.RecordArray,
                   lengthFieldName );
            this.elementMap = elementMap;
        }

        public ByteCodecItemMap getItem( String[ ] path ) {
            return this.elementMap.getItem( path );
        }

        @Override
        protected void engage( ByteCodecContainerMap parent,
                               int index )
            throws ByteCodecMapException {
            super.engage( parent,
                          index );
        }
    }

    public static class ByteCodecFileMap extends ByteCodecContainerMap {

        @Override
        public ByteCodecFileMap getRoot( ) {
            return this;
        }

        public ByteCodecFileMap( String name,
                                 ByteCodecItemLengthType lengthType,
                                 ByteCodecItemMap... items )
            throws ByteCodecMapException {
            super( name,
                   ByteCodecItemDataType.File,
                   lengthType,
                   items );
            this.engage( null,
                         ( -1 ) );
        }
    }
}
