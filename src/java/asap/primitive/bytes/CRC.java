package asap.primitive.bytes;

public class CRC {

    @SuppressWarnings( "serial" )
    public static class CRCException extends Exception {

        public CRCException( String message ) {
            super( message );
        }
    }

    public final int     order;

    public final long    polynom;

    public final boolean refin;

    public final boolean refout;

    public final long    crcxor;

    public final long    crcmask;

    public final long    crchighbit;

    public final long    crctab[];

    public final long    crcinit_direct;

    public final long    crcinit_nondirect;

    // subroutines
    protected long reflect( long data,
                            int bitCount ) {
        // reflects the lower 'bitnum' bits of 'crc'
        long tmpAndMask;
        long tmpOrMask = 1;
        long tmpResult = 0;
        for ( tmpAndMask = ( 1L << ( bitCount - 1 ) ); tmpAndMask != 0; tmpAndMask >>= 1 ) {
            if ( ( data & tmpAndMask ) != 0 ) {
                tmpResult |= tmpOrMask;
            }
            tmpOrMask <<= 1;
        }
        return ( tmpResult );
    }

    public CRC( // 'order' [1..32] is the CRC polynom order, counted without the leading '1' bit
               int order,
               // 'refin' [t,t] specifies if a data byte is reflected before processing (UART) or not
               boolean refin,
               // 'refout' [f,t] specifies if the CRC will be reflected before XOR
               boolean refout,
               // 'polynom' is the CRC polynom without leading '1' bit
               long polynom,
               // 'crcxor' is the final XOR value
               long crcxor,
               // 'direct' [f,t] specifies the kind of algorithm: 1=direct, no augmented zero bits
               boolean direct,
               // 'crcinit' is the initial CRC value belonging to that algorithm
               long crcinit )
        throws CRCException {
        // at first, compute constant bit masks for whole CRC and CRC high bit
        this.crcmask = ( ( 1L << order ) - 1 );
        this.crchighbit = ( 1L << ( order - 1 ) );
        // check parameters
        if ( order < 1 || order > 32 ) {
            throw new CRCException( "Invalid 'order'" );
        }
        if ( polynom != ( polynom & this.crcmask ) ) {
            throw new CRCException( "Invalid 'polynom'" );
        }
        if ( crcinit != ( crcinit & this.crcmask ) ) {
            throw new CRCException( "Invalid 'init'" );
        }
        if ( crcxor != ( crcxor & this.crcmask ) ) {
            throw new CRCException( "Invalid 'xor'" );
        }
        this.refin = refin;
        this.refout = refout;
        this.order = order;
        this.polynom = polynom;
        this.crcxor = crcxor;
        // compute missing initial CRC value
        if ( !direct ) {
            this.crcinit_nondirect = crcinit;
            for ( byte tmpBitIndex = 0; tmpBitIndex < this.order; tmpBitIndex++ ) {
                long tmpBitValue = ( crcinit & this.crchighbit );
                crcinit <<= 1;
                if ( tmpBitValue != 0 ) {
                    crcinit ^= this.polynom;
                }
            }
            this.crcinit_direct = ( crcinit & this.crcmask );
        }
        else {
            this.crcinit_direct = crcinit;
            for ( byte tmpBitIndex = 0; tmpBitIndex < this.order; tmpBitIndex++ ) {
                long tmpBitValue = ( crcinit & 1 );
                if ( tmpBitValue != 0 ) {
                    crcinit ^= this.polynom;
                }
                crcinit >>= 1;
                if ( tmpBitValue != 0 ) {
                    crcinit |= this.crchighbit;
                }
            }
            this.crcinit_nondirect = crcinit;
        }
        this.crctab = new long[ 256 ];
        // make CRC lookup table used by table algorithms
        for ( short tmpTableIndex = 0; tmpTableIndex < 256; tmpTableIndex++ ) {
            long tmpTableEntry = tmpTableIndex;
            if ( this.refin ) {
                tmpTableEntry = reflect( tmpTableEntry,
                                         8 );
            }
            tmpTableEntry <<= ( this.order - 8 );
            for ( byte tmpBitIndex = 0; tmpBitIndex < 8; tmpBitIndex++ ) {
                long tmpBitValue = ( tmpTableEntry & this.crchighbit );
                tmpTableEntry <<= 1;
                if ( tmpBitValue != 0 ) {
                    tmpTableEntry ^= this.polynom;
                }
            }
            if ( this.refin ) {
                tmpTableEntry = reflect( tmpTableEntry,
                                         order );
            }
            tmpTableEntry &= crcmask;
            this.crctab[ tmpTableIndex ] = tmpTableEntry;
        }
    }

    public long crc_bitbybit( byte message[] ) {
        // bit by bit algorithm with augmented zero bytes.
        // does not use lookup table, suited for polynom orders between 1...32.
        long tmpCrcResult = this.crcinit_nondirect;
        for ( int tmpMessageIndex = 0; tmpMessageIndex < message.length; tmpMessageIndex++ ) {
            long tmpMessageByte = message[ tmpMessageIndex ];
            if ( this.refin ) {
                tmpMessageByte = reflect( tmpMessageByte,
                                          8 );
            }
            for ( long tmpBitMask = 0x80; tmpBitMask != 0; tmpBitMask >>= 1 ) {
                long tmpBitValue = ( tmpCrcResult & this.crchighbit );
                tmpCrcResult <<= 1;
                if ( ( tmpMessageByte & tmpBitMask ) != 0 ) {
                    tmpCrcResult |= 1;
                }
                if ( tmpBitValue != 0 ) {
                    tmpCrcResult ^= this.polynom;
                }
            }
        }
        for ( int tmpBitIndex = 0; tmpBitIndex < this.order; tmpBitIndex++ ) {
            long tmpBitValue = ( tmpCrcResult & this.crchighbit );
            tmpCrcResult <<= 1;
            if ( tmpBitValue != 0 ) {
                tmpCrcResult ^= this.polynom;
            }
        }
        if ( this.refout ) {
            tmpCrcResult = reflect( tmpCrcResult,
                                    this.order );
        }
        tmpCrcResult ^= this.crcxor;
        tmpCrcResult &= this.crcmask;
        return ( tmpCrcResult );
    }

    public long crc_bitbybit_fast( byte message[] ) {
        // fast bit by bit algorithm without augmented zero bytes.
        // does not use lookup table, suited for polynom orders between 1...32.
        long tmpCrcResult = this.crcinit_direct;
        for ( int tmpMessageIndex = 0; tmpMessageIndex < message.length; tmpMessageIndex++ ) {
            long tmpMessageByte = message[ tmpMessageIndex ];
            if ( this.refin ) {
                tmpMessageByte = reflect( tmpMessageByte,
                                          8 );
            }
            for ( long tmpBitMask = 0x80; tmpBitMask != 0; tmpBitMask >>= 1 ) {
                long tmpBitValue = ( tmpCrcResult & this.crchighbit );
                tmpCrcResult <<= 1;
                if ( ( tmpMessageByte & tmpBitMask ) != 0 ) {
                    tmpBitValue ^= this.crchighbit;
                }
                if ( tmpBitValue != 0 ) {
                    tmpCrcResult ^= this.polynom;
                }
            }
        }
        if ( this.refout )
            tmpCrcResult = reflect( tmpCrcResult,
                                    this.order );
        tmpCrcResult ^= this.crcxor;
        tmpCrcResult &= this.crcmask;
        return ( tmpCrcResult );
    }

    public long crc_table( byte message[] ) {
        // normal lookup table algorithm with augmented zero bytes.
        // only usable with polynom orders of 8, 16, 24 or 32.
        int tmpMessageCursor = 0;
        int tmpMessageLength = message.length;
        long tmpCrcResult = this.crcinit_nondirect;
        if ( this.refin ) {
            tmpCrcResult = this.reflect( tmpCrcResult,
                                         this.order );
        }
        if ( !this.refin ) {
            while ( tmpMessageLength-- != 0 ) {
                tmpCrcResult = ( ( ( tmpCrcResult << 8 ) | message[ tmpMessageCursor++ ] )
                                 ^ this.crctab[ (int) ( ( tmpCrcResult >> ( this.order - 8 ) ) & 0xff ) ] );
            }
        }
        else {
            while ( tmpMessageLength-- != 0 ) {
                tmpCrcResult = ( ( ( tmpCrcResult >> 8 ) | ( message[ tmpMessageCursor++ ] << ( this.order - 8 ) ) )
                                 ^ this.crctab[ (int) ( tmpCrcResult & 0xff ) ] );
            }
        }
        if ( !this.refin ) {
            while ( ++tmpMessageLength < ( this.order / 8 ) ) {
                tmpCrcResult = ( ( tmpCrcResult << 8 )
                                 ^ this.crctab[ (int) ( ( tmpCrcResult >> ( this.order - 8 ) ) & 0xff ) ] );
            }
        }
        else {
            while ( ++tmpMessageLength < ( this.order / 8 ) ) {
                tmpCrcResult = ( ( tmpCrcResult >> 8 ) ^ this.crctab[ (int) ( tmpCrcResult & 0xff ) ] );
            }
        }
        if ( this.refout ^ this.refin ) {
            tmpCrcResult = reflect( tmpCrcResult,
                                    order );
        }
        tmpCrcResult ^= this.crcxor;
        tmpCrcResult &= this.crcmask;
        return ( tmpCrcResult );
    }

    public long crc_table_fast( byte message[] ) {
        // fast lookup table algorithm without augmented zero bytes, e.g. used in pkzip.
        // only usable with polynom orders of 8, 16, 24 or 32.
        int tmpMessageIndex = 0;
        int tmpMessageLength = message.length;
        long tmpCrcResult = this.crcinit_direct;
        if ( this.refin ) {
            tmpCrcResult = reflect( tmpCrcResult,
                                    this.order );
        }
        if ( !this.refin ) {
            while ( tmpMessageLength-- > 0 ) {
                tmpCrcResult = ( ( tmpCrcResult << 8 )
                                 ^ this.crctab[ (int) ( ( ( tmpCrcResult >> ( this.order - 8 ) ) & 0xff )
                                                        ^ message[ tmpMessageIndex++ ] )
                                                & 0xff ] );
            }
        }
        else {
            while ( tmpMessageLength-- > 0 ) {
                tmpCrcResult = ( ( tmpCrcResult >> 8 )
                                 ^ this.crctab[ (int) ( ( tmpCrcResult & 0xff ) ^ message[ tmpMessageIndex++ ] )
                                                & 0xff ] );
            }
        }
        if ( this.refout ^ this.refin ) {
            tmpCrcResult = reflect( tmpCrcResult,
                                    this.order );
        }
        tmpCrcResult ^= this.crcxor;
        tmpCrcResult &= this.crcmask;
        return ( tmpCrcResult );
    }
}
