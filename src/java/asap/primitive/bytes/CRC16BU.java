package asap.primitive.bytes;

public class CRC16BU {

    private final static int table[] = { 0x0000,
                                         0xCC01,
                                         0xD801,
                                         0x1400,
                                         0xF001,
                                         0x3C00,
                                         0x2800,
                                         0xE401,
                                         0xA001,
                                         0x6C00,
                                         0x7800,
                                         0xB401,
                                         0x5000,
                                         0x9C01,
                                         0x8801,
                                         0x4400 };

    public static int compute( byte[ ] buffer ) {
        int tmpResult = 0;
        for ( int tmpInputCursor = 0; tmpInputCursor < buffer.length; tmpInputCursor++ ) {
            int tmpInputByte = buffer[ tmpInputCursor ] & 0xFF;
            tmpResult = ( ( tmpResult >>> 4 ) ^ table[ ( tmpInputByte ^ tmpResult ) & 0x0F ] );
            tmpResult = ( ( tmpResult >>> 4 ) ^ table[ ( ( tmpInputByte >> 4 ) ^ tmpResult ) & 0x0F ] );
        }
        tmpResult = ( tmpResult << 8 | tmpResult >>> 8 );
        tmpResult &= 0xFFFF;
        return tmpResult;
    }
}
