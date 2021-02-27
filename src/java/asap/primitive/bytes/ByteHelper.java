package asap.primitive.bytes;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import asap.primitive.string.StringHelper;

public class ByteHelper {

    public static final byte[ ] EMPTY_BYTE_ARRAY = new byte[ 0 ];

    public static enum DumpMode {
        Hexa,
        Ascii,
        ArrayInit
    }

    private static int      dumpColumns = 32;

    private static DumpMode dumpMode    = DumpMode.Ascii;

    public static String dump( byte[ ] buffer,
                               int columns ) {
        return dump( buffer,
                     DumpMode.Hexa,
                     columns );
    }

    public static String dump( byte[ ] buffer,
                               int columns,
                               int indentation ) {
        return dumpHexa( buffer,
                         columns,
                         "",
                         "",
                         "",
                         "",
                         StringHelper.spaces( indentation ),
                         "",
                         "",
                         " ",
                         "\n",
                         "" );
    }

    public static String dump( byte[ ] buffer,
                               DumpMode mode,
                               int columns ) {
        switch ( mode ) {
            case Ascii:
                return dumpAscii( null,
                                  buffer,
                                  columns );
            case Hexa:
            default:
                return dumpHexa( buffer,
                                 columns,
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 " ",
                                 "\n",
                                 "" );
        }
    }

    public static String dump( byte[ ] buffer,
                               DumpMode mode,
                               int columns,
                               int indentation,
                               String descriptionFormat,
                               Object... descriptionArgs ) {
        String tmpDescription;
        tmpDescription = StringHelper.flawlessFormat( ( ( ( descriptionArgs == null )
                                                          || ( descriptionArgs.length == 0 ) ) ? StringHelper.escapeFormat( descriptionFormat )
                                                                                               : descriptionFormat ),
                                                      descriptionArgs );
        switch ( mode ) {
            case Hexa:
                return dumpHexa( buffer,
                                 columns,
                                 "",
                                 "",
                                 tmpDescription,
                                 String.format( " ( %d bytes ):\n",
                                                buffer.length ),
                                 StringHelper.spaces( indentation ),
                                 "",
                                 "",
                                 " ",
                                 "\n",
                                 "\n" );
            case Ascii:
                return dumpAscii( tmpDescription,
                                  buffer,
                                  columns );
            case ArrayInit:
                return dumpHexa( buffer,
                                 columns,
                                 "",
                                 "byte ",
                                 tmpDescription,
                                 " = {\n",
                                 StringHelper.spaces( indentation ),
                                 "0x",
                                 "",
                                 ", ",
                                 "\n",
                                 " };\n\n" );
            default:
                break;
        }
        return "";
    }

    public static String dump( byte[ ] buffer,
                               DumpMode mode,
                               int columns,
                               String descriptionFormat,
                               Object... descriptionArgs ) {
        return dump( buffer,
                     mode,
                     columns,
                     0,
                     descriptionFormat,
                     descriptionArgs );
    }

    public static String dump( byte[ ] buffer,
                               int columns,
                               String descriptionFormat,
                               Object... descriptionArgs ) {
        return dump( buffer,
                     dumpMode,
                     columns,
                     descriptionFormat,
                     descriptionArgs );
    }

    public static String dump( byte[ ] buffer,
                               String descriptionFormat,
                               Object... descriptionArgs ) {
        return dump( buffer,
                     dumpMode,
                     dumpColumns,
                     descriptionFormat,
                     descriptionArgs );
    }

    public static String dumpAscii( byte[ ] buffer,
                                    int columns ) {
        return dumpAscii( null,
                          buffer,
                          columns,
                          false );
    }

    public static String dumpAscii( String description,
                                    byte[ ] buffer,
                                    int columns ) {
        return dumpAscii( description,
                          buffer,
                          columns,
                          false );
    }

    public static String dumpAscii( String description,
                                    byte[ ] buffer,
                                    int columns,
                                    boolean appendNewLine ) {
        StringBuilder tmpReturn = new StringBuilder( );
        if ( ( description != null ) && ( description.length( ) > 0 ) ) {
            tmpReturn.append( String.format( "%s:",
                                             description ) );
        }
        int tmpByteIndex = 0;
        int tmpBufferIndex = 0;
        do {
            if ( tmpReturn.length( ) > 0 ) {
                tmpReturn.append( "\n" );
            }
            for ( int tmpHexaIndex = 0; tmpHexaIndex < columns; tmpHexaIndex++ ) {
                if ( tmpHexaIndex > 0 ) {
                    tmpReturn.append( " " );
                }
                tmpByteIndex = ( tmpBufferIndex + tmpHexaIndex );
                if ( tmpByteIndex < buffer.length ) {
                    tmpReturn.append( String.format( "%02X",
                                                     buffer[ tmpByteIndex ] ) );
                }
                else {
                    tmpReturn.append( "  " );
                }
            }
            tmpReturn.append( " - " );
            for ( int tmpHexaIndex = 0; tmpHexaIndex < columns; tmpHexaIndex++ ) {
                tmpByteIndex = ( tmpBufferIndex + tmpHexaIndex );
                if ( tmpByteIndex < buffer.length ) {
                    byte tmpByte = buffer[ tmpByteIndex ];
                    if ( ( tmpByte < 0x20 ) || ( tmpByte > 0x7f ) ) {
                        tmpByte = '.';
                    }
                    tmpReturn.append( String.format( "%c",
                                                     tmpByte ) );
                }
                else {
                    tmpReturn.append( " " );
                }
            }
            tmpBufferIndex += columns;
        }
        while ( tmpBufferIndex < buffer.length );
        if ( appendNewLine ) {
            tmpReturn.append( "\n" );
        }
        return tmpReturn.toString( );
    }

    public static String dumpHexa( byte[ ] buffer,
                                   int columns,
                                   String dumpPrefix,
                                   String descriptionPrefix,
                                   String description,
                                   String descriptionSufix,
                                   String linePrefix,
                                   String bytePrefix,
                                   String byteSufix,
                                   String byteSeparator,
                                   String lineSufix,
                                   String dumpSufix ) {
        StringBuilder tmpResult = new StringBuilder( );
        tmpResult.append( dumpPrefix );
        if ( description.length( ) > 0 ) {
            tmpResult.append( descriptionPrefix );
            tmpResult.append( description );
            tmpResult.append( descriptionSufix );
        }
        int tmpByteIndex = 0;
        for ( tmpByteIndex = 0; tmpByteIndex < buffer.length; tmpByteIndex++ ) {
            if ( tmpByteIndex > 0 ) {
                tmpResult.append( byteSeparator );
            }
            if ( ( tmpByteIndex % columns ) == 0 ) {
                if ( tmpByteIndex > 0 ) {
                    tmpResult.append( lineSufix );
                }
                tmpResult.append( linePrefix );
            }
            tmpResult.append( bytePrefix );
            tmpResult.append( String.format( "%02X",
                                             buffer[ tmpByteIndex ] ) );
            tmpResult.append( byteSufix );
        }
        tmpResult.append( lineSufix );
        tmpResult.append( dumpSufix );
        return tmpResult.toString( );
    }

    public static void configureDump( DumpMode mode ) {
        ByteHelper.dumpMode = mode;
    }

    public static void configureDump( DumpMode mode,
                                      int columns ) {
        ByteHelper.dumpMode = mode;
        ByteHelper.dumpColumns = columns;
    }

    public static void configureDump( int columns ) {
        ByteHelper.dumpColumns = columns;
    }

    public static byte[ ] array( int... values ) {
        byte[ ] tmpResult = new byte[ values.length ];
        for ( int tmpIndex = 0; tmpIndex < tmpResult.length; tmpIndex++ ) {
            tmpResult[ tmpIndex ] = (byte) ( values[ tmpIndex ] & 0x00ff );
        }
        return tmpResult;
    }

    public static List< Byte > asList( byte[ ] array ) {
        List< Byte > tmpList = new ArrayList< Byte >( );
        for ( byte tmpValue : array ) {
            tmpList.add( tmpValue );
        }
        return tmpList;
    }

    public static byte[ ] asArray( List< Byte > list ) {
        byte[ ] tmpArray = new byte[ list.size( ) ];
        for ( int tmpIndex = 0; tmpIndex < tmpArray.length; tmpIndex++ ) {
            Byte tmpValue = list.get( tmpIndex );
            tmpArray[ tmpIndex ] = ( tmpValue == null ) ? 0
                                                        : tmpValue;
        }
        return tmpArray;
    }

    public static boolean isAllZeroes( byte[ ] bytes ) {
        return isAllZeroes( bytes,
                            bytes.length );
    }

    public static boolean isAllZeroes( byte[ ] bytes,
                                       int length ) {
        return isAllZeroes( bytes,
                            0,
                            length );
    }

    public static boolean isAllZeroes( byte[ ] bytes,
                                       int offset,
                                       int length ) {
        int tmpEndIndex = ( offset + length );
        if ( ( offset < 0 ) || ( offset >= bytes.length ) || ( length < 0 ) || ( tmpEndIndex > bytes.length ) ) {
            return false;
        }
        for ( int tmpIndex = offset; tmpIndex < tmpEndIndex; tmpIndex++ ) {
            if ( bytes[ tmpIndex ] != 0 ) {
                return false;
            }
        }
        return true;
    }

    public static byte[ ] arrayCopy( byte[ ] source,
                                     byte[ ] target ) {
        System.arraycopy( source,
                          0,
                          target,
                          0,
                          ( target.length < source.length ) ? target.length
                                                            : source.length );
        return target;
    }

    public static byte[ ] copyOf( byte[ ] sourceArray ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOf( sourceArray,
                                                        sourceArray.length );
    }

    public static byte[ ] copyOf( byte[ ] sourceArray,
                                  int newLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOf( sourceArray,
                                                        newLength );
    }

    public static byte[ ] copyOfRange( byte[ ] sourceArray,
                                       int rangeOffset,
                                       int rangeLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             rangeOffset,
                                                             ( rangeOffset + rangeLength ) );
    }

    public static byte[ ] subArray( byte[ ] sourceArray,
                                    int subOffset,
                                    int subLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             subOffset,
                                                             ( subOffset + subLength ) );
    }

    public static byte[ ] leftSubArray( byte[ ] sourceArray,
                                        int leftLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             0,
                                                             leftLength );
    }

    public static byte[ ] rightSubArray( byte[ ] sourceArray,
                                         int rightLength ) {
        return ( sourceArray == null ) ? null
                                       : Arrays.copyOfRange( sourceArray,
                                                             ( sourceArray.length - rightLength ),
                                                             sourceArray.length );
    }

    public static byte[ ] blockPadding( byte[ ] packed,
                                        int blockSize ) {
        return copyOf( packed,
                       ( ( ( packed.length + blockSize - 1 ) / blockSize ) * blockSize ) );
    }

    public static byte[ ] concatenate( byte[ ]... byteArrays ) {
        int tmpResultLength = 0;
        for ( byte[ ] tmpByteArray : byteArrays ) {
            if ( tmpByteArray != null ) {
                tmpResultLength += tmpByteArray.length;
            }
        }
        byte[ ] tmpResult = new byte[ tmpResultLength ];
        int tmpResultIndex = 0;
        for ( byte[ ] tmpByteArray : byteArrays ) {
            if ( tmpByteArray != null ) {
                System.arraycopy( tmpByteArray,
                                  0,
                                  tmpResult,
                                  tmpResultIndex,
                                  tmpByteArray.length );
                tmpResultIndex += tmpByteArray.length;
            }
        }
        return tmpResult;
    }

    public static byte[ ] fill( int length,
                                int value ) {
        byte[ ] tmpByteArray = new byte[ length ];
        Arrays.fill( tmpByteArray,
                     (byte) ( value & 0x000000ff ) );
        return tmpByteArray;
    }

    public static byte[ ] fill( byte[ ] byteArray,
                                int value ) {
        Arrays.fill( byteArray,
                     (byte) ( value & 0x000000ff ) );
        return byteArray;
    }

    public static byte[ ] fill( byte[ ] byteArray,
                                int regionStart,
                                int regionLength,
                                int value ) {
        int tmpRegionEnd = ( regionStart + regionLength );
        if ( tmpRegionEnd > byteArray.length ) {
            tmpRegionEnd = byteArray.length;
        }
        for ( int tmpRegionIndex = regionStart; tmpRegionIndex < tmpRegionEnd; tmpRegionIndex++ ) {
            byteArray[ tmpRegionIndex ] = (byte) ( value & 0x000000ff );
        }
        return byteArray;
    }

    public static byte[ ] negate( byte[ ] byteArray,
                                  int regionOffset,
                                  int regionLength ) {
        for ( int tmpIndex = regionOffset; tmpIndex < ( regionOffset + regionLength ); tmpIndex++ ) {
            byteArray[ tmpIndex ] ^= (byte) 0xff;
        }
        return byteArray;
    }

    public static byte[ ] negate( byte[ ] byteArray ) {
        return negate( byteArray,
                       0,
                       byteArray.length );
    }

    public static byte[ ] getNegated( byte[ ] byteArray,
                                      int regionOffset,
                                      int regionLength ) {
        return negate( subArray( byteArray,
                                 regionOffset,
                                 regionLength ) );
    }

    public static byte[ ] getNegated( byte[ ] byteArray ) {
        return negate( ByteHelper.copyOf( byteArray ) );
    }

    public static byte[ ] invert( byte[ ] byteArray,
                                  int regionOffset,
                                  int regionLength ) {
        int tmpStartIndex = regionOffset;
        int tmpEndIndex = ( regionOffset + regionLength - 1 );
        while ( tmpStartIndex < tmpEndIndex ) {
            byte tmpByte = byteArray[ tmpStartIndex ];
            byteArray[ tmpStartIndex ] = byteArray[ tmpEndIndex ];
            byteArray[ tmpEndIndex ] = tmpByte;
            ++tmpStartIndex;
            --tmpEndIndex;
        }
        return byteArray;
    }

    public static byte[ ] invert( byte[ ] byteArray ) {
        return invert( byteArray,
                       0,
                       byteArray.length );
    }

    public static byte[ ] getInverted( byte[ ] byteArray,
                                       int regionOffset,
                                       int regionLength ) {
        return invert( subArray( byteArray,
                                 regionOffset,
                                 regionLength ) );
    }

    public static byte[ ] getInverted( byte[ ] byteArray ) {
        return invert( ByteHelper.copyOf( byteArray ) );
    }

    public static long fromBigEndian( byte... buffer ) {
        long tmpResult = 0L;
        for ( int tmpIndex = 0; tmpIndex < buffer.length; tmpIndex++ ) {
            tmpResult |= ( buffer[ buffer.length - tmpIndex - 1 ] & 0xFFL ) << ( 8 * tmpIndex );
        }
        return ( tmpResult & 0xFFFFFFFFFFFFFFL );
    }

    public static Long fromBigEndian( byte[ ] buffer,
                                      int index,
                                      int length ) {
        long tmpResult = 0L;
        int tmpByteCount = 0;
        for ( int tmpIndex = ( index + length - 1 ); tmpIndex >= index; tmpIndex-- ) {
            tmpResult |= ( buffer[ tmpIndex ] & 0xFFL ) << ( 8 * tmpByteCount );
            ++tmpByteCount;
        }
        return ( tmpResult & 0xFFFFFFFFFFFFFFL );
    }

    public static long fromLittleEndian( byte... buffer ) {
        long tmpResult = 0L;
        for ( int tmpIndex = 0; tmpIndex < buffer.length; tmpIndex++ ) {
            tmpResult |= ( buffer[ tmpIndex ] & 0xFFL ) << ( 8 * tmpIndex );
        }
        return ( tmpResult & 0xFFFFFFFFFFFFFFL );
    }

    public static byte[ ] toBigEndian( short valor ) {
        byte[ ] tmpResult = new byte[ 2 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0xFF00L ) >>> ( 8 * 1 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0x00FFL ) >>> ( 8 * 0 ) );
        return tmpResult;
    }

    public static byte[ ] toBigEndian( int valor ) {
        byte[ ] tmpResult = new byte[ 4 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0xFF000000L ) >>> ( 8 * 3 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0x00FF0000L ) >>> ( 8 * 2 ) );
        tmpResult[ 2 ] = (byte) ( ( valor & 0x0000FF00L ) >>> ( 8 * 1 ) );
        tmpResult[ 3 ] = (byte) ( ( valor & 0x000000FFL ) >>> ( 8 * 0 ) );
        return tmpResult;
    }

    public static byte[ ] toBigEndian( long valor ) {
        byte[ ] tmpResult = new byte[ 8 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0xFF00000000000000L ) >>> ( 8 * 7 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0x00FF000000000000L ) >>> ( 8 * 6 ) );
        tmpResult[ 2 ] = (byte) ( ( valor & 0x0000FF0000000000L ) >>> ( 8 * 5 ) );
        tmpResult[ 3 ] = (byte) ( ( valor & 0x000000FF00000000L ) >>> ( 8 * 4 ) );
        tmpResult[ 4 ] = (byte) ( ( valor & 0x00000000FF000000L ) >>> ( 8 * 3 ) );
        tmpResult[ 5 ] = (byte) ( ( valor & 0x0000000000FF0000L ) >>> ( 8 * 2 ) );
        tmpResult[ 6 ] = (byte) ( ( valor & 0x000000000000FF00L ) >>> ( 8 * 1 ) );
        tmpResult[ 7 ] = (byte) ( ( valor & 0x00000000000000FFL ) >>> ( 8 * 0 ) );
        return tmpResult;
    }

    public static byte[ ] toBigEndian( long valor,
                                       int qtdBytes ) {
        byte[ ] tmpBuffer = toBigEndian( valor );
        return rightSubArray( tmpBuffer,
                              qtdBytes );
    }

    public static byte[ ] toLittleEndian( short valor ) {
        byte[ ] tmpResult = new byte[ 2 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0x00FFL ) >>> ( 8 * 0 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0xFF00L ) >>> ( 8 * 1 ) );
        return tmpResult;
    }

    public static byte[ ] toLittleEndian( int valor ) {
        byte[ ] tmpResult = new byte[ 4 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0x000000FFL ) >>> ( 8 * 0 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0x0000FF00L ) >>> ( 8 * 1 ) );
        tmpResult[ 2 ] = (byte) ( ( valor & 0x00FF0000L ) >>> ( 8 * 2 ) );
        tmpResult[ 3 ] = (byte) ( ( valor & 0xFF000000L ) >>> ( 8 * 3 ) );
        return tmpResult;
    }

    public static byte[ ] toLittleEndian( long valor ) {
        byte[ ] tmpResult = new byte[ 8 ];
        tmpResult[ 0 ] = (byte) ( ( valor & 0x00000000000000FFL ) >>> ( 8 * 0 ) );
        tmpResult[ 1 ] = (byte) ( ( valor & 0x000000000000FF00L ) >>> ( 8 * 1 ) );
        tmpResult[ 2 ] = (byte) ( ( valor & 0x0000000000FF0000L ) >>> ( 8 * 2 ) );
        tmpResult[ 3 ] = (byte) ( ( valor & 0x00000000FF000000L ) >>> ( 8 * 3 ) );
        tmpResult[ 4 ] = (byte) ( ( valor & 0x000000FF00000000L ) >>> ( 8 * 4 ) );
        tmpResult[ 5 ] = (byte) ( ( valor & 0x0000FF0000000000L ) >>> ( 8 * 5 ) );
        tmpResult[ 6 ] = (byte) ( ( valor & 0x00FF000000000000L ) >>> ( 8 * 6 ) );
        tmpResult[ 7 ] = (byte) ( ( valor & 0xFF00000000000000L ) >>> ( 8 * 7 ) );
        return tmpResult;
    }

    public static byte[ ] toLittleEndian( long valor,
                                          int qtdBytes ) {
        return leftSubArray( toLittleEndian( valor ),
                             qtdBytes );
    }

    public static String asciify( byte[ ] bytes ) {
        return asciify( bytes,
                        bytes.length );
    }

    public static String asciify( byte[ ] bytes,
                                  int width ) {
        return asciify( bytes,
                        width,
                        ' ',
                        ".",
                        " | " );
    }

    public static String asciify( byte[ ] bytes,
                                  int width,
                                  char hexaSeparator,
                                  String hexaFill,
                                  String asciiSeparator ) {
        if ( bytes == null ) {
            return "null";
        }
        StringBuilder tmpResult = new StringBuilder( 256 );
        for ( int tmpIndex = 0; tmpIndex < width; tmpIndex++ ) {
            if ( tmpIndex > 0 ) {
                tmpResult.append( hexaSeparator );
            }
            if ( tmpIndex < bytes.length ) {
                tmpResult.append( String.format( "%02X",
                                                 bytes[ tmpIndex ] ) );
            }
            else {
                tmpResult.append( String.format( "%-2.2s",
                                                 hexaFill ) );
            }
        }
        tmpResult.append( asciiSeparator );
        for ( int tmpIndex = 0; tmpIndex < width; tmpIndex++ ) {
            if ( tmpIndex < bytes.length ) {
                byte tmpByte = bytes[ tmpIndex ];
                if ( ( tmpByte < 0x20 ) || ( tmpByte > 0x7f ) ) {
                    tmpByte = '.';
                }
                tmpResult.append( String.format( "%c",
                                                 tmpByte ) );
            }
            else {
                tmpResult.append( " " );
            }
        }
        return tmpResult.toString( );
    }

    public static String hexify( byte[ ] bytes ) {
        return hexify( bytes,
                       " " );
    }

    public static String hexify( byte[ ] bytes,
                                 String separator ) {
        if ( bytes == null ) {
            return "null";
        }
        StringBuilder tmpResult = new StringBuilder( 256 );
        for ( int tmpDataIndex = 0; tmpDataIndex < bytes.length; tmpDataIndex++ ) {
            if ( tmpDataIndex > 0 ) {
                tmpResult.append( separator );
            }
            tmpResult.append( String.format( "%02X",
                                             bytes[ tmpDataIndex ] ) );
        }
        return tmpResult.toString( );
    }

    public static byte[ ] parseHexString( String bytesString ) {
        if ( bytesString == null ) {
            return null;
        }
        ByteArrayOutputStream tmpResult = new ByteArrayOutputStream( 256 );
        int tmpByte = 0;
        boolean tmpWaitingSecondDigit = false;
        for ( int tmpIndex = 0; tmpIndex < bytesString.length( ); tmpIndex++ ) {
            char tmpChar = bytesString.charAt( tmpIndex );
            if ( ( ( tmpChar >= '0' ) && ( tmpChar <= '9' ) )
                 || ( ( tmpChar >= 'A' ) && ( tmpChar <= 'F' ) )
                 || ( ( tmpChar >= 'a' ) && ( tmpChar <= 'f' ) ) ) {
                if ( !tmpWaitingSecondDigit ) {
                    tmpByte = ( Character.digit( tmpChar,
                                                 16 ) << 4 );
                    tmpWaitingSecondDigit = true;
                }
                else {
                    tmpByte |= Character.digit( tmpChar,
                                                16 );
                    tmpResult.write( tmpByte );
                    tmpWaitingSecondDigit = false;
                }
            }
            else if ( StringHelper.isSpace( tmpChar ) ) {
                if ( tmpWaitingSecondDigit ) {
                    throw new IllegalArgumentException( String.format( "Invalid hexadecimal character '%c'/0x%2X",
                                                                       StringHelper.printable( tmpChar ),
                                                                       (int) tmpChar ) );
                }
            }
        }
        return tmpResult.toByteArray( );
    }

    public static byte[ ][ ] parseHexStringArray( String[ ] bytesStringArray ) {
        byte[ ][ ] tmpResult = new byte[ bytesStringArray.length ][ ];
        for ( int tmpIndex = 0; tmpIndex < bytesStringArray.length; tmpIndex++ ) {
            tmpResult[ tmpIndex ] = parseHexString( bytesStringArray[ tmpIndex ] );
        }
        return tmpResult;
    }

    /*
    private static final String URL_SAFE_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    */
    private static final String SIMPLE_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String encodeBase64( byte[ ] binary ) {
        String tmpAlphabet = SIMPLE_BASE64_ALPHABET;
        String tmpResult = "";
        int tmpBlock;
        int tmpIndex;
        int tmpIntegralLen = ( ( binary.length / 3 ) * 3 );
        int tmpParcialLen = ( binary.length % 3 );
        for ( tmpIndex = 0; tmpIndex < tmpIntegralLen; tmpIndex += 3 ) {
            tmpBlock = ( ( ( (int) binary[ tmpIndex + 0 ] & 0xff ) * 0x00010000 )
                         + ( ( (int) binary[ tmpIndex + 1 ] & 0xff ) * 0x00000100 )
                         + ( ( (int) binary[ tmpIndex + 2 ] & 0xff ) * 0x00000001 ) );
            tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00040000 ) & 0x3f );
            tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00001000 ) & 0x3f );
            tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00000040 ) & 0x3f );
            tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00000001 ) & 0x3f );
        }
        switch ( tmpParcialLen ) {
            case 1:
                tmpBlock = ( ( ( (int) binary[ tmpIndex ] & 0xff ) * 0x00010000 ) );
                tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00040000 ) & 0x0000003f );
                tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00001000 ) & 0x0000003f );
                tmpResult += "==";
                break;
            case 2:
                tmpBlock = ( ( ( (int) binary[ tmpIndex + 0 ] & 0xff ) * 0x00010000 )
                             + ( ( (int) binary[ tmpIndex + 1 ] & 0xff ) * 0x00000100 ) );
                tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00040000 ) & 0x0000003f );
                tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00001000 ) & 0x0000003f );
                tmpResult += tmpAlphabet.charAt( ( tmpBlock / 0x00000040 ) & 0x0000003f );
                tmpResult += "=";
                break;
            default:
                break;
        }
        return tmpResult;
    }

    public static byte[ ] decodeBase64( String text )
        throws ParseException {
        if ( text.length( ) == 0 ) {
            return new byte[ 0 ];
        }
        if ( ( text.length( ) % 4 ) != 0 ) {
            throw new ParseException( "Tamanho inválido de código Base64",
                                      text.length( ) );
        }
        String tmpAlphabet = SIMPLE_BASE64_ALPHABET;
        byte[ ] tmpDraft = new byte[ ( text.length( ) / 4 ) * 3 ];
        int tmpResultLen = 0;
        int tmpBlock = 0;
        int tmpMultiplier = 0x00040000;
        int tmpIndex;
        char tmpChar;
        int tmpAlphabetIndex;
        for ( tmpIndex = 0; tmpIndex < text.length( ); tmpIndex++ ) {
            tmpChar = text.charAt( tmpIndex );
            if ( tmpChar == '=' ) {
                break;
            }
            tmpAlphabetIndex = tmpAlphabet.indexOf( tmpChar );
            if ( tmpAlphabetIndex < 0 ) {
                throw new ParseException( "Alfabeto Base64 inválido",
                                          tmpIndex );
            }
            tmpBlock += ( tmpAlphabetIndex * tmpMultiplier );
            if ( ( tmpIndex % 4 ) != 3 ) {
                tmpMultiplier /= 0x00000040;
            }
            else {
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00010000 ) & 0x000000ff );
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00000100 ) & 0x000000ff );
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00000001 ) & 0x000000ff );
                tmpBlock = 0;
                tmpMultiplier = 0x00040000;
            }
        }
        switch ( tmpIndex % 4 ) {
            case 0:
                break;
            case 2:
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00010000 ) & 0x000000ff );
                break;
            case 3:
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00010000 ) & 0x000000ff );
                tmpDraft[ tmpResultLen++ ] = (byte) ( ( tmpBlock / 0x00000100 ) & 0x000000ff );
                break;
            default:
                throw new ParseException( "Codificação Base64 inválida",
                                          tmpIndex );
        }
        return copyOf( tmpDraft,
                       tmpResultLen );
    }
}
