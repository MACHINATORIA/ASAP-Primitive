package asap.primitive.bits;

import asap.primitive.bytes.ByteHelper;

/**
 * 
 * Funções de auxílio à manipulação de arrays de bits
 *
 */
public class BitHelper {

    /**
     * Calcula o tamanho mínimo de um array de bytes, para que caiba nele um array de bits.
     * 
     * @param bitCount
     *            Tamanho em bits do array de bits
     * @return Tamanho mínimo em bytes para o array de bytes
     */
    public static int getBitArrayBufferLength( int bitCount ) {
        return ( ( bitCount + Byte.SIZE - 1 ) / Byte.SIZE );
    }

    /**
     * Calcula a quantidade mínima de bits a concatenar com um array de bits, para que o tamanho
     * total seja múltiplo do tamanho de um byte (8 bits).
     * 
     * @param bitCount
     *            Tamanho em bits do array de bits
     * @return Quantidade mínima de bits a concatenar com o array de bits
     */
    public static int getBitArrayPadLength( int bitCount ) {
        return getBitArrayPadLength( bitCount,
                                     1 );
    }

    /**
     * Calcula a quantidade de bits a concatenar com um array de bits, para que o tamanho total
     * ocupe completamente um array de bytes.<br>
     * <br>
     * Se o tamanho do array de bytes for menor que o mínimo necessário para caber o array de bits,
     * o resultado será equivalente ao da função "{@link #getBitArrayPadLength( int ) int
     * getBitArrayPadLength( int bitCount )}"
     * 
     * @see #getBitArrayPadLength( int )
     * 
     * @param bitCount
     *            Tamanho em bits do array de bits
     * @param byteCount
     *            Tamanho em bytes do array de bytes
     * @return Quantidade de bits a concatenar com o array de bits
     */
    public static int getBitArrayPadLength( int bitCount,
                                            int byteCount ) {
        return ( ( ( byteCount * Byte.SIZE ) - ( bitCount % ( byteCount * Byte.SIZE ) ) ) % ( byteCount * Byte.SIZE ) );
    }

    /**
     * Calcula a quantidade de bits a concatenar com um array de bits, para que o tamanho total
     * ocupe completamente um array de bytes.<br>
     * <br>
     * Se o tamanho do array de bytes for menor que o mínimo necessário para caber o array de bits,
     * o resultado será equivalente ao da função "{@link #getBitArrayPadLength( int ) int
     * getBitArrayPadLength( int bitCount )}"
     * 
     * @see #getBitArrayPadLength( int, int )
     * 
     * @param bitCount
     *            Tamanho em bits do array de bits
     * @param bitArray
     *            Array de bytes contendo o array de bits
     * @return Quantidade de bits a concatenar com o array de bits
     */
    public static int getBitArrayPadLength( int bitCount,
                                            byte[ ] bitArray ) {
        return getBitArrayPadLength( bitCount,
                                     bitArray.length );
    }

    /**
     * Cria um array de bytes com o tamanho mínimo necessário para caber um array de bits.
     * 
     * @param bitCount
     *            Tamanho em bits do array de bits
     * @return Array de bytes com o tamanho mínimo para caber o array de bits
     */
    public static byte[ ] createBitArrayBuffer( int bitCount ) {
        return new byte[ BitHelper.getBitArrayBufferLength( bitCount ) ];
    }

    /**
     * Cria uma cópia de um array de bits que esteja alinhado com a direita de um array de bytes
     * (<i>padding</i> no início do array).<br>
     * <br>
     * Dentro dos bytes o array de bits de origem deve estar ordenado em <i>BigEndian</i>, como
     * ilustrado a seguir:<br>
     * <br>
     * <code>
     * &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp;posição "12" ---------------------------------------+<br><br>
     * &nbsp;Array de 2 bytes: [ 00010010, 10011100 ] (em binário)<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ------+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp;posição "12" --------------------+<br><br>
     * &nbsp;Array de 2 bytes: [ 12, 8C ] (em hexadecimal)
     * </code>
     * 
     * @see #copyOfLeftAlignedBitArray( byte[], int, int )
     * 
     * @param bitArray
     *            Array de bytes contendo o array de bits de origem (alinhado à direita)
     * @param bitCount
     *            Tamanho do array de bits de origem
     * @param newBitCount
     *            Tamanho do array de bits de destino
     * @return Array de bytes contendo a cópia do array de bits
     */
    public static byte[ ] copyOfRightAlignedBitArray( byte[ ] bitArray,
                                                      int bitCount,
                                                      int newBitCount ) {
        byte[ ] tmpResult = BitHelper.createBitArrayBuffer( newBitCount );
        BitHelper.bitArrayCopy( bitArray,
                                BitHelper.getBitArrayPadLength( bitCount,
                                                                bitArray ),
                                tmpResult,
                                BitHelper.getBitArrayPadLength( bitCount,
                                                                tmpResult ),
                                bitCount );
        return tmpResult;
    }

    /**
     * Cria uma cópia de um array de bits que esteja alinhado com a esquerda de um array de bytes
     * (<i>padding</i> no fim do array).<br>
     * <br>
     * Dentro dos bytes o array de bits deve estar ordenado em <i>BigEndian</i>, como ilustrado a
     * seguir:<br>
     * <br>
     * <code>
     * &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp;posição "12" ---------------------------------------+<br><br>
     * &nbsp;Array de 2 bytes: [ 10010100, 11100000 ] (em binário)<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp;posição "12" -----------------+<br><br>
     * &nbsp;Array de 2 bytes: [ 94 E0 ] (em hexadecimal)
     * </code>
     * 
     * @see #copyOfRightAlignedBitArray( byte[], int, int )
     * 
     * @param bitArray
     *            Array de bytes contendo o array de bits de origem (alinhado à esquerda)
     * @param bitCount
     *            Tamanho do array de bits de origem
     * @param newBitCount
     *            Tamanho do array de bits de destino
     * @return Array de bytes contendo a cópia do array de bits
     */
    public static byte[ ] copyOfLeftAlignedBitArray( byte[ ] bitArray,
                                                     int bitCount,
                                                     int newBitCount ) {
        byte[ ] tmpResult = BitHelper.createBitArrayBuffer( newBitCount );
        BitHelper.bitArrayCopy( bitArray,
                                0,
                                tmpResult,
                                0,
                                bitCount );
        return tmpResult;
    }

    /**
     * Conta a quantidade de bits com valor "1" em uma região de um array de bits, alinhados com a
     * esquerda do array de bytes (<i>padding</i> no fim do array), e que podem ou não estar
     * ordenados em "<i>LittleEndian</i>".
     * 
     * @see #countOneBits( int, int, byte[] )
     * 
     * @param isLittleEndian
     *            Indica que dentro dos bytes os bits do array estão ordenados em
     *            "<i>LittleEndian</i>" (do bit "0" para o bit "7"), ao contrário do convencional
     *            que é em "<i>BigEndian</i>" (do bit "7" para o bit "0"), como ilustrado a
     *            seguir:<br>
     *            <br>
     *            <code>
     *            &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp;posição "12" ---------------------------------------+<br><br>
     *            &nbsp;BigEndian:&nbsp; &nbsp; &nbsp; &nbsp; [ 10010100, 11100000 ] = [ 0x94, 0xE0 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp;| &nbsp;| &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "7" ----------+ &nbsp;| &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "8" -------------+ &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "12" ----------------+<br><br>
     *            &nbsp;LittleEndian:&nbsp; &nbsp; &nbsp;[ 00101001, 00000111 ] = [ 0x29, 0x07 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "7" ---+ &nbsp; &nbsp; &nbsp;| &nbsp; &nbsp; | &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ----------+ &nbsp; &nbsp; | &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "12" ---------------+ &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "8" --------------------+<br><br>
     *            </code>
     * @param bitArray
     *            Array de bytes contendo o array de bits
     * @param regionOffset
     *            Índice para o elemento inicial do array de bits
     * @param regionLength
     *            Tamanho da região dentro do array de bits
     * @return Quantidade de bits com valor "1" na região especificada do array de bits
     */
    public static int countOneBits( boolean isLittleEndian,
                                    byte[ ] bitArray,
                                    int regionOffset,
                                    int regionLength ) {
        int tmpResult = 0;
        int tmpCurrentOffset = regionOffset;
        int tmpLastOffset = ( regionOffset + regionLength );
        while ( tmpCurrentOffset < tmpLastOffset ) {
            int tmpByteIndex = ( tmpCurrentOffset / Byte.SIZE );
            if ( tmpByteIndex >= bitArray.length ) {
                break;
            }
            int tmpBitPosition = ( tmpCurrentOffset % Byte.SIZE );
            byte tmpAndMask = (byte) ( isLittleEndian ? ( 0x01 << tmpBitPosition )
                                                      : ( 0x80 >> tmpBitPosition ) );
            int tmpBitCount = ( Byte.SIZE - tmpBitPosition );
            int tmpRemainingBitCount = ( tmpLastOffset - tmpCurrentOffset );
            if ( tmpBitCount > tmpRemainingBitCount ) {
                tmpBitCount = tmpRemainingBitCount;
            }
            while ( tmpBitCount > 0 ) {
                if ( ( bitArray[ tmpByteIndex ] & tmpAndMask ) != 0 ) {
                    ++tmpResult;
                }
                tmpAndMask = (byte) ( isLittleEndian ? ( tmpAndMask << 1 )
                                                     : ( tmpAndMask >> 1 ) );
                ++tmpCurrentOffset;
                --tmpBitCount;
            }
        }
        return tmpResult;
    }

    /**
     * Conta a quantidade de bits com valor "1" em uma região de um array de bits.<br>
     * <br>
     * Os arrays de bits devem estar alinhados à esquerda dos arrays de bytes (<i>padding</i> no fim
     * do array), e dentro dos bytes os bits devem estar ordenados em <i>BigEndian</i>, como
     * ilustrado a seguir:<br>
     * <br>
     * <code>
     * &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp;posição "12" ---------------------------------------+<br><br>
     * &nbsp;BigEndian:&nbsp; &nbsp; &nbsp; &nbsp; [ 10010100, 11100000 ] = [ 0x94, 0xE0 ]<br>
     * &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp;| &nbsp;| &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp; posição "7" ----------+ &nbsp;| &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp; posição "8" -------------+ &nbsp; |<br>
     * &nbsp; &nbsp; &nbsp; posição "12" ----------------+<br><br>
     * </code>
     * 
     * @see #countOneBits( boolean, int, int, byte[] )
     * 
     * @param bitArray
     *            Array de bytes contendo o array de bits
     * @param regionOffset
     *            Índice para o elemento inicial do array de bits
     * @param regionLength
     *            Tamanho da região dentro do array de bits
     * @return Quantidade de bits com valor "1" na região especificada do array de bits
     */
    public static int countOneBits( byte[ ] bitArray,
                                    int regionOffset,
                                    int regionLength ) {
        return BitHelper.countOneBits( false,
                                       bitArray,
                                       regionOffset,
                                       regionLength );
    }

    /**
     * Concatena um array de bits à direita (no final) de outro array de bits.<br>
     * <br>
     * Os arrays de bits devem estar alinhados à esquerda dos arrays de bytes (<i>padding</i> no fim
     * do array), e dentro dos bytes os bits devem estar ordenados em <i>BigEndian</i>, como
     * ilustrado a seguir:<br>
     * <br>
     * <code>
     * &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     * &nbsp;Array de 2 bytes: [ 10010100, 11100000 ] (em binário)<br>
     * &nbsp;Array de 2 bytes: [ 94 E0 ] (em hexadecimal)
     * </code>
     * 
     * @param targetArray
     *            Array de bytes contendo o array de bits destino
     * @param targetIndex
     *            Índice a posição da origem onde o destino será concatenado
     * @param sourceArray
     *            Array de bytes contendo o array de bits origem
     * @param sourceLength
     *            Tamanho do array de bits de origem
     * @return
     */
    public static int appendRightBits( byte[ ] targetArray,
                                       int targetIndex,
                                       byte[ ] sourceArray,
                                       int sourceLength ) {
        BitHelper.bitArrayCopy( sourceArray,
                                BitHelper.getBitArrayPadLength( sourceLength,
                                                                sourceArray.length ),
                                targetArray,
                                targetIndex,
                                sourceLength );
        return ( targetIndex + sourceLength );
    }

    /**
     * Copia uma região de um array de bits sobre outra região de outro array de bits.<br>
     * <br>
     * Os arrays de bits devem estar alinhados à esquerda dos arrays de bytes (<i>padding</i> no fim
     * do array).
     * 
     * @param isLittleEndian
     *            Indica que dentro dos bytes os bits do array estão ordenados em
     *            "<i>LittleEndian</i>" (do bit "0" para o bit "7"), ao contrário do convencional
     *            que é em "<i>BigEndian</i>" (do bit "7" para o bit "0"), como ilustrado a
     *            seguir:<br>
     *            <br>
     *            <code>
     *            &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp;posição "12" ---------------------------------------+<br><br>
     *            &nbsp;BigEndian:&nbsp; &nbsp; &nbsp; &nbsp; [ 10010100, 11100000 ] = [ 0x94, 0xE0 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ---+ &nbsp; &nbsp; &nbsp;| &nbsp;| &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "7" ----------+ &nbsp;| &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "8" -------------+ &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "12" ----------------+<br><br>
     *            &nbsp;LittleEndian:&nbsp; &nbsp; &nbsp;[ 00101001, 00000111 ] = [ 0x29, 0x07 ]<br>
     *            &nbsp; &nbsp; &nbsp; posição "7" ---+ &nbsp; &nbsp; &nbsp;| &nbsp; &nbsp; | &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "0" ----------+ &nbsp; &nbsp; | &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "12" ---------------+ &nbsp; |<br>
     *            &nbsp; &nbsp; &nbsp; posição "8" --------------------+<br><br>
     *            </code>
     * @param sourceBitArray
     *            Array de bytes contendo o array de bits de origem
     * @param sourceBitsOffset
     *            Índice do bit inicial dentro do array de bits de origem
     * @param targetBitArray
     *            Array de bytes contendo o array de bits de destino
     * @param targetBitOffset
     *            Índice do bit inicial dentro do array de bits de destino
     * @param bitArrayLength
     *            Quantidade de bits a copiar da origem para o destino
     */
    public static void bitArrayCopy( boolean isLittleEndian,
                                     byte[ ] sourceBitArray,
                                     int sourceBitsOffset,
                                     byte[ ] targetBitArray,
                                     int targetBitOffset,
                                     int bitArrayLength ) {
        int tmpCurrentTargetBitIndex = targetBitOffset;
        int tmpCurrentSourceBitIndex = sourceBitsOffset;
        int tmpLastSourceBitIndex = ( tmpCurrentSourceBitIndex + bitArrayLength );
        while ( tmpCurrentSourceBitIndex < tmpLastSourceBitIndex ) {
            //
            int tmpSourceByteIndex = ( tmpCurrentSourceBitIndex / Byte.SIZE );
            int tmpTargetByteIndex = ( tmpCurrentTargetBitIndex / Byte.SIZE );
            if ( ( tmpSourceByteIndex >= sourceBitArray.length ) || ( tmpTargetByteIndex >= targetBitArray.length ) ) {
                break;
            }
            //
            int tmpSourceBitPosition = ( tmpCurrentSourceBitIndex % Byte.SIZE );
            int tmpSourceCopyLimit = ( Byte.SIZE - tmpSourceBitPosition );
            int tmpTargetBitPosition = ( tmpCurrentTargetBitIndex % Byte.SIZE );
            int tmpTargetCopyLimit = ( Byte.SIZE - tmpTargetBitPosition );
            int tmpCopyingBitsCount = ( tmpSourceCopyLimit < tmpTargetCopyLimit ) ? tmpSourceCopyLimit
                                                                                  : tmpTargetCopyLimit;
            int tmpRemainingBitsCount = ( tmpLastSourceBitIndex - tmpCurrentSourceBitIndex );
            if ( tmpCopyingBitsCount > tmpRemainingBitsCount ) {
                tmpCopyingBitsCount = tmpRemainingBitsCount;
            }
            //
            byte tmpCopyingSourceBits;
            byte tmpTargetAndMask;
            byte tmpTargetOrMask;
            if ( isLittleEndian ) {
                tmpCopyingSourceBits = (byte) ( sourceBitArray[ tmpSourceByteIndex ] >> tmpSourceBitPosition );
                tmpTargetAndMask = (byte) ~( ( 0x00ff >> ( Byte.SIZE
                                                           - ( tmpTargetBitPosition + tmpCopyingBitsCount ) ) )
                                             & ( 0x00ff << tmpTargetBitPosition ) );
                tmpTargetOrMask = (byte) ( ( tmpCopyingSourceBits << tmpTargetBitPosition ) & ( ~tmpTargetAndMask ) );
            }
            else {
                tmpCopyingSourceBits = (byte) ( sourceBitArray[ tmpSourceByteIndex ] << tmpSourceBitPosition );
                tmpTargetAndMask = (byte) ~( ( 0x00ff >> tmpTargetBitPosition )
                                             & ( 0x00ff << ( Byte.SIZE
                                                             - ( tmpTargetBitPosition + tmpCopyingBitsCount ) ) ) );
                tmpTargetOrMask = (byte) ( ( tmpCopyingSourceBits >> tmpTargetBitPosition ) & ( ~tmpTargetAndMask ) );
            }
            //
            targetBitArray[ tmpTargetByteIndex ] &= tmpTargetAndMask;
            targetBitArray[ tmpTargetByteIndex ] |= tmpTargetOrMask;
            //
            tmpCurrentSourceBitIndex += tmpCopyingBitsCount;
            tmpCurrentTargetBitIndex += tmpCopyingBitsCount;
        }
    }

    /**
     * 
     * Copia uma região de um array de bits sobre outra região de outro array de bits. <br>
     * <br>
     * Os arrays de bits devem estar alinhados à esquerda dos arrays de bytes (<i>padding</i> no fim
     * do array), e dentro dos bytes os bits devem estar ordenados em <i>BigEndian</i>, como
     * ilustrado a seguir:<br>
     * <br>
     * <code>
     * &nbsp;Array de 13 bits: [ 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0, 0 ]<br>
     * &nbsp;Array de 2 bytes: [ 10010100, 11100000 ] (em binário)<br>
     * &nbsp;Array de 2 bytes: [ 94 E0 ] (em hexadecimal)
     * </code>
     * 
     * @see #bitArrayCopy( boolean, byte[], int, byte[], int, int )
     * 
     * @param sourceBitArray
     *            Array de bytes contendo o array de bits de origem
     * @param sourceBitsOffset
     *            Índice do bit inicial dentro do array de bits de origem
     * @param targetBitArray
     *            Array de bytes contendo o array de bits de destino
     * @param targetBitOffset
     *            Índice do bit inicial dentro do array de bits de destino
     * @param bitArrayLength
     *            Quantidade de bits a copiar da origem para o destino
     */
    public static void bitArrayCopy( byte[ ] sourceBitArray,
                                     int sourceBitsOffset,
                                     byte[ ] targetBitArray,
                                     int targetBitOffset,
                                     int bitArrayLength ) {
        BitHelper.bitArrayCopy( false,
                                sourceBitArray,
                                sourceBitsOffset,
                                targetBitArray,
                                targetBitOffset,
                                bitArrayLength );
    }

    public static boolean bitArrayGet( byte[ ] bitArray,
                                       int offset ) {
        return bitArrayGet( false,
                            bitArray,
                            offset );
    }

    public static boolean bitArrayGet( boolean isLittleEndian,
                                       byte[ ] bitArray,
                                       int offset ) {
        int tmpByteIndex = ( offset / Byte.SIZE );
        if ( tmpByteIndex < bitArray.length ) {
            int tmpBitPosition = ( offset % Byte.SIZE );
            int tmpAndMask = ( isLittleEndian ? ( 0x01 << tmpBitPosition )
                                              : ( 0x80 >> tmpBitPosition ) );
            return ( ( bitArray[ tmpByteIndex ] & tmpAndMask ) != 0 );
        }
        return false;
    }

    public static boolean bitArrayTestAnd( boolean isLittleEndian,
                                           byte[ ] bitArray,
                                           int testOffset,
                                           int testLength ) {
        byte[ ] tmpBits = ByteHelper.fill( getBitArrayBufferLength( testLength ),
                                           0xff );
        bitArrayCopy( isLittleEndian,
                      bitArray,
                      testOffset,
                      tmpBits,
                      0,
                      testLength );
        return ByteHelper.isAllZeroes( ByteHelper.negate( tmpBits ) );
    }

    public static boolean bitArrayTestOr( boolean isLittleEndian,
                                          byte[ ] bitArray,
                                          int testOffset,
                                          int testLength ) {
        byte[ ] tmpBits = createBitArrayBuffer( testLength );
        bitArrayCopy( isLittleEndian,
                      bitArray,
                      testOffset,
                      tmpBits,
                      0,
                      testLength );
        return !ByteHelper.isAllZeroes( tmpBits );
    }

    public static void bitArrayReset( boolean isLittleEndian,
                                      byte[ ] bitArray,
                                      int resetOffset,
                                      int resetLength ) {
        int tmpCurrentOffset = resetOffset;
        int tmpLastOffset = ( resetOffset + resetLength );
        while ( tmpCurrentOffset < tmpLastOffset ) {
            int tmpByteIndex = ( tmpCurrentOffset / Byte.SIZE );
            if ( tmpByteIndex >= bitArray.length ) {
                break;
            }
            int tmpBitPosition = ( tmpCurrentOffset % Byte.SIZE );
            int tmpBitCount = ( Byte.SIZE - tmpBitPosition );
            int tmpRemainingBitCount = ( tmpLastOffset - tmpCurrentOffset );
            if ( tmpBitCount > tmpRemainingBitCount ) {
                tmpBitCount = tmpRemainingBitCount;
            }
            byte tmpAndMask;
            if ( isLittleEndian ) {
                tmpAndMask = (byte) ~( ( 0x00ff >> ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) )
                                       & ( 0x00ff << tmpBitPosition ) );
            }
            else {
                tmpAndMask = (byte) ~( ( 0x00ff >> tmpBitPosition )
                                       & ( 0x00ff << ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) ) );
            }
            bitArray[ tmpByteIndex ] &= tmpAndMask;
            tmpCurrentOffset += tmpBitCount;
        }
    }

    public static void bitArrayReset( byte[ ] bitArray,
                                      int resetOffset,
                                      int resetLength ) {
        BitHelper.bitArrayReset( false,
                                 bitArray,
                                 resetOffset,
                                 resetLength );
    }

    public static void bitArraySet( boolean isLittleEndian,
                                    byte[ ] bitArray,
                                    int setOffset,
                                    int setLength ) {
        int tmpCurrentOffset = setOffset;
        int tmpLastOffset = ( setOffset + setLength );
        while ( tmpCurrentOffset < tmpLastOffset ) {
            int tmpByteIndex = ( tmpCurrentOffset / Byte.SIZE );
            if ( tmpByteIndex >= bitArray.length ) {
                break;
            }
            int tmpBitPosition = ( tmpCurrentOffset % Byte.SIZE );
            int tmpBitCount = ( Byte.SIZE - tmpBitPosition );
            int tmpRemainingBitCount = ( tmpLastOffset - tmpCurrentOffset );
            if ( tmpBitCount > tmpRemainingBitCount ) {
                tmpBitCount = tmpRemainingBitCount;
            }
            byte tmpOrMask;
            if ( isLittleEndian ) {
                tmpOrMask = (byte) ( ( 0x00ff >> ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) )
                                     & ( 0x00ff << tmpBitPosition ) );
            }
            else {
                tmpOrMask = (byte) ( ( 0x00ff >> tmpBitPosition )
                                     & ( 0x00ff << ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) ) );
            }
            bitArray[ tmpByteIndex ] |= tmpOrMask;
            tmpCurrentOffset += tmpBitCount;
        }
    }

    public static void bitArraySet( byte[ ] bitArray,
                                    int setOffset ) {
        BitHelper.bitArraySet( false,
                               bitArray,
                               setOffset,
                               1 );
    }

    public static void bitArraySet( byte[ ] bitArray,
                                    int setOffset,
                                    int setLength ) {
        BitHelper.bitArraySet( false,
                               bitArray,
                               setOffset,
                               setLength );
    }

    public static void bitArrayToggle( boolean isLittleEndian,
                                       byte[ ] bitArray,
                                       int toggleOffset,
                                       int toggleLength ) {
        int tmpCurrentOffset = toggleOffset;
        int tmpLastOffset = ( toggleOffset + toggleLength );
        while ( tmpCurrentOffset < tmpLastOffset ) {
            int tmpByteIndex = ( tmpCurrentOffset / Byte.SIZE );
            if ( tmpByteIndex >= bitArray.length ) {
                break;
            }
            int tmpBitPosition = ( tmpCurrentOffset % Byte.SIZE );
            int tmpBitCount = ( Byte.SIZE - tmpBitPosition );
            int tmpRemainingBitCount = ( tmpLastOffset - tmpCurrentOffset );
            if ( tmpBitCount > tmpRemainingBitCount ) {
                tmpBitCount = tmpRemainingBitCount;
            }
            byte tmpXorMask;
            if ( isLittleEndian ) {
                tmpXorMask = (byte) ( ( 0x00ff >> ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) )
                                      & ( 0x00ff << tmpBitPosition ) );
            }
            else {
                tmpXorMask = (byte) ( ( 0x00ff >> tmpBitPosition )
                                      & ( 0x00ff << ( Byte.SIZE - ( tmpBitPosition + tmpBitCount ) ) ) );
            }
            bitArray[ tmpByteIndex ] ^= tmpXorMask;
            tmpCurrentOffset += tmpBitCount;
        }
    }

    public static void bitArrayToggle( byte[ ] bitArray,
                                       int toggleOffset,
                                       int toggleLength ) {
        BitHelper.bitArrayToggle( false,
                                  bitArray,
                                  toggleOffset,
                                  toggleLength );
    }

    public static void bitArrayInvert( boolean isLittleEndian,
                                       byte[ ] bitArray,
                                       int invertOffset,
                                       int invertLength ) {
        int tmpFirstOffset = invertOffset;
        int tmpLastOffset = ( invertOffset + invertLength - 1 );
        while ( tmpFirstOffset < tmpLastOffset ) {
            int tmpFirstByteIndex = ( tmpFirstOffset / Byte.SIZE );
            int tmpFirstBitPosition = ( tmpFirstOffset % Byte.SIZE );
            byte tmpFirstMask = (byte) ( isLittleEndian ? ( 0x0001 << tmpFirstBitPosition )
                                                        : ( 0x0080 >> tmpFirstBitPosition ) );
            boolean tmpFirstStatus = ( ( bitArray[ tmpFirstByteIndex ] & tmpFirstMask ) != 0 );
            //
            int tmpLastByteIndex = ( tmpLastOffset / Byte.SIZE );
            int tmpLastBitPosition = ( tmpLastOffset % Byte.SIZE );
            byte tmpLastMask = (byte) ( isLittleEndian ? ( 0x0001 << tmpLastBitPosition )
                                                       : ( 0x0080 >> tmpLastBitPosition ) );
            //
            if ( ( bitArray[ tmpLastByteIndex ] & tmpLastMask ) != 0 ) {
                bitArray[ tmpFirstByteIndex ] |= tmpFirstMask;
            }
            else {
                bitArray[ tmpFirstByteIndex ] &= ~tmpFirstMask;
            }
            if ( tmpFirstStatus ) {
                bitArray[ tmpLastByteIndex ] |= tmpLastMask;
            }
            else {
                bitArray[ tmpLastByteIndex ] &= ~tmpLastMask;
            }
            ++tmpFirstOffset;
            --tmpLastOffset;
        }
    }

    public static void bitArrayInvert( byte[ ] bitArray,
                                       int invertOffset,
                                       int invertLength ) {
        bitArrayInvert( false,
                        bitArray,
                        invertOffset,
                        invertLength );
    }

    public static byte[ ] subBitArray( boolean isLittleEndian,
                                       byte[ ] dataBitArray,
                                       int dataBitsOffset,
                                       int dataBitsLength ) {
        byte[ ] tmpResult = BitHelper.createBitArrayBuffer( dataBitsLength );
        BitHelper.bitArrayCopy( isLittleEndian,
                                dataBitArray,
                                dataBitsOffset,
                                tmpResult,
                                BitHelper.getBitArrayPadLength( dataBitsLength ),
                                dataBitsLength );
        return tmpResult;
    }

    public static byte[ ] subBitArray( byte[ ] dataBitArray,
                                       int dataBitsOffset,
                                       int dataBitsLength ) {
        return subBitArray( false,
                            dataBitArray,
                            dataBitsOffset,
                            dataBitsLength );
    }

    public static String bitArrayToString( byte[ ] bitArray ) {
        return bitArrayToString( bitArray,
                                 0 );
    }

    public static String bitArrayToString( byte[ ] bitArray,
                                           int bitsOffset ) {
        return bitArrayToString( bitArray,
                                 bitsOffset,
                                 bitArray.length * Byte.SIZE );
    }

    public static String bitArrayToString( byte[ ] bitArray,
                                           int bitsOffset,
                                           int bitsLength ) {
        return bitArrayToString( bitArray,
                                 bitsOffset,
                                 bitsLength,
                                 false );
    }

    public static String bitArrayToString( byte[ ] bitArray,
                                           int bitsOffset,
                                           int bitsLength,
                                           String byteSeparator ) {
        return bitArrayToString( bitArray,
                                 bitsOffset,
                                 bitsLength,
                                 false,
                                 byteSeparator );
    }

    public static String bitArrayToString( byte[ ] bitArray,
                                           int bitsOffset,
                                           int bitsLength,
                                           boolean leftAlign ) {
        return bitArrayToString( bitArray,
                                 bitsOffset,
                                 bitsLength,
                                 leftAlign,
                                 " " );
    }

    public static String bitArrayToString( byte[ ] bitArray,
                                           int bitsOffset,
                                           int bitsLength,
                                           boolean leftAlign,
                                           String byteSeparator ) {
        StringBuilder tmpResult = new StringBuilder( );
        int tmpAlignShift = ( leftAlign ? ( bitsOffset % Byte.SIZE )
                                        : 0 );
        for ( int tmpBitIndex = 0; tmpBitIndex < bitsLength; tmpBitIndex++ ) {
            int tmpBitOffset = ( bitsOffset + tmpBitIndex );
            int tmpByteIndex = ( tmpBitOffset / Byte.SIZE );
            int tmpBitPosition = ( tmpBitOffset % Byte.SIZE );
            byte tmpAndMask = (byte) ( 0x0080 >> tmpBitPosition );
            if ( ( ( tmpBitPosition - tmpAlignShift ) == 0 ) && ( tmpBitIndex != 0 ) ) {
                tmpResult.append( byteSeparator );
            }
            tmpResult.append( ( ( bitArray[ tmpByteIndex ] & tmpAndMask ) == 0 ) ? "0"
                                                                                 : "1" );
        }
        return tmpResult.toString( );
    }
}
