package asap.primitive.crypto;

import asap.primitive.exception.ExceptionTemplate;

public interface CryptoAPI {

    @SuppressWarnings( "serial" )
    public abstract class CryptoAPIException extends ExceptionTemplate {

        public CryptoAPIException( String messageFormat,
                                   Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public CryptoAPIException( Throwable e,
                                   String messageFormat,
                                   Object... messageArgs ) {
            super( e,
                   messageFormat,
                   messageArgs );
        }
    }

    public interface Util {

        int getPadLen( int effectiveLen,
                       int padModulo );

        byte[ ] getPad( int effectiveLen,
                        int padModulo,
                        byte padFill );

        public String getBase64Alphabet( );

        public void setBase64Alphabet( String alphabet )
            throws CryptoAPIException;

        String encodeBase64( byte[ ] binary );

        byte[ ] decodeBase64( String text )
            throws CryptoAPIException;
    }

    public interface Random {

        byte[ ] generate( int len )
            throws CryptoAPIException;
    }

    public interface SHA1 {

        static final int SHA1_DIGEST_BYTE_LEN = 20;

        byte[ ] run( byte[ ] message )
            throws CryptoAPIException;
    }

    public interface DES {

        static final int KEY_BIT_LEN  = 56;

        static final int KEY_BYTE_LEN = ( ( KEY_BIT_LEN + 15 ) / 8 );

        byte[ ] encrypt( byte[ ] plaintext )
            throws CryptoAPIException;

        byte[ ] decrypt( byte[ ] crypttext )
            throws CryptoAPIException;
    }

    public interface AES {

        static final int KEY_BIT_LEN  = 128;

        static final int KEY_BYTE_LEN = ( ( KEY_BIT_LEN + 7 ) / 8 );

        byte[ ] encrypt( byte[ ] plaintext )
            throws CryptoAPIException;

        byte[ ] decrypt( byte[ ] crypttext )
            throws CryptoAPIException;
    }

    public interface RSA {

        static final int KEY_BIT_LEN  = 1024;

        static final int KEY_BYTE_LEN = ( ( KEY_BIT_LEN + 7 ) / 8 );

        public interface Key {

            byte[ ] getExponent( )
                throws CryptoAPIException;

            byte[ ] getModulus( )
                throws CryptoAPIException;

            byte[ ] cipher( byte[ ] message )
                throws CryptoAPIException;

            byte[ ] encryptPKCS1( byte[ ] plaintext )
                throws CryptoAPIException;

            byte[ ] decryptPKCS1( byte[ ] crypttext )
                throws CryptoAPIException;
        }

        Key getPublicKey( )
            throws CryptoAPIException;

        Key getPrivateKey( )
            throws CryptoAPIException;
    }

    public Util getUtil( )
        throws CryptoAPIException;

    public SHA1 getSHA1( )
        throws CryptoAPIException;

    public Random getRandom( )
        throws CryptoAPIException;

    public DES getDES( byte[ ] key )
        throws CryptoAPIException;

    public DES getDES( byte[ ] key1,
                       byte[ ] key2 )
        throws CryptoAPIException;

    public DES getDES( byte[ ] key1,
                       byte[ ] key2,
                       byte[ ] key3 )
        throws CryptoAPIException;

    public AES getAES( int keyBitLength,
                       boolean cbcMode,
                       byte[ ] key,
                       byte[ ] initVector )
        throws CryptoAPIException;

    public AES getAES128CBC( byte[ ] key,
                             byte[ ] initVector )
        throws CryptoAPIException;

    public RSA getRSA( )
        throws CryptoAPIException;

    public RSA getRSA( byte[ ] publicExponent,
                       byte[ ] modulus )
        throws CryptoAPIException;

    public RSA getRSA( byte[ ] publicExponent,
                       byte[ ] modulus,
                       byte[ ] privateExponent )
        throws CryptoAPIException;
}
