package asap.primitive.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import asap.primitive.bytes.ByteHelper;

public class CryptoAPI_JCE implements CryptoAPI {

    public static final String URL_SAFE_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    public static final String SIMPLE_BASE64_ALPHABET   = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    @SuppressWarnings( "serial" )
    protected class CryptoAPIException_JCE extends CryptoAPIException {

        public CryptoAPIException_JCE( String messageFormat,
                                       Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public CryptoAPIException_JCE( Throwable e,
                                       String messageFormat,
                                       Object... messageArgs ) {
            super( e,
                   messageFormat,
                   messageArgs );
        }
    }

    protected class Util_JCE implements Util {

        @Override
        public int getPadLen( int effectiveLen,
                              int padModulo ) {
            return ( ( ( ( effectiveLen + padModulo - 1 ) / padModulo ) * padModulo ) - effectiveLen );
        }

        @Override
        public byte[ ] getPad( int effectiveLen,
                               int padModulo,
                               byte padFill ) {
            byte[ ] tmpResult = new byte[ getPadLen( effectiveLen,
                                                     padModulo ) ];
            Arrays.fill( tmpResult,
                         padFill );
            return tmpResult;
        }

        protected String base64Alphabet = SIMPLE_BASE64_ALPHABET;

        @Override
        public String getBase64Alphabet( ) {
            return this.base64Alphabet;
        }

        @Override
        public void setBase64Alphabet( String alphabet )
            throws CryptoAPIException {
            if ( ( alphabet == null ) || ( alphabet.length( ) != this.base64Alphabet.length( ) ) ) {
                throw new CryptoAPIException_JCE( "Alfabeto nulo ou de tamanho inválido para Base64 (tamanho %d esperado, %s fornecido.",
                                                  this.base64Alphabet.length( ),
                                                  ( alphabet == null ) ? "null"
                                                                       : Integer.toString( alphabet.length( ) ) );
            }
            int tmpIndex = 0;
            List< Character > tmpCharList = new ArrayList< Character >( );
            for ( char tmpChar : alphabet.toCharArray( ) ) {
                if ( tmpCharList.contains( tmpChar ) ) {
                    throw new CryptoAPIException_JCE( "Caracter '%c' duplicado na posição %d do alfabeto Base64.",
                                                      tmpChar,
                                                      tmpIndex );
                }
                if ( SIMPLE_BASE64_ALPHABET.indexOf( tmpChar ) < 0 ) {
                    throw new CryptoAPIException_JCE( "Caracter '%c' inválido na posição %d do alfabeto Base64.",
                                                      tmpChar,
                                                      tmpIndex );
                }
            }
            this.base64Alphabet = alphabet;
        }

        @Override
        public String encodeBase64( byte[ ] binary ) {
            StringBuilder tmpResult = new StringBuilder( );
            char[ ] tmpBase64Alphabet = this.base64Alphabet.toCharArray( );
            int tmpBlock;
            int tmpIndex;
            int tmpIntegralLen = ( ( binary.length / 3 ) * 3 );
            int tmpParcialLen = ( binary.length % 3 );
            for ( tmpIndex = 0; tmpIndex < tmpIntegralLen; tmpIndex += 3 ) {
                tmpBlock = ( ( ( (int) binary[ tmpIndex + 0 ] & 0xff ) * 0x00010000 )
                             + ( ( (int) binary[ tmpIndex + 1 ] & 0xff ) * 0x00000100 )
                             + ( ( (int) binary[ tmpIndex + 2 ] & 0xff ) * 0x00000001 ) );
                tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00040000 ) & 0x0000003f ] );
                tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00001000 ) & 0x0000003f ] );
                tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00000040 ) & 0x0000003f ] );
                tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00000001 ) & 0x0000003f ] );
            }
            switch ( tmpParcialLen ) {
                case 1:
                    tmpBlock = ( ( ( (int) binary[ tmpIndex ] & 0xff ) * 0x00010000 ) );
                    tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00040000 ) & 0x0000003f ] );
                    tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00001000 ) & 0x0000003f ] );
                    tmpResult.append( "==" );
                    break;
                case 2:
                    tmpBlock = ( ( ( (int) binary[ tmpIndex + 0 ] & 0xff ) * 0x00010000 )
                                 + ( ( (int) binary[ tmpIndex + 1 ] & 0xff ) * 0x00000100 ) );
                    tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00040000 ) & 0x0000003f ] );
                    tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00001000 ) & 0x0000003f ] );
                    tmpResult.append( tmpBase64Alphabet[ ( tmpBlock / 0x00000040 ) & 0x0000003f ] );
                    tmpResult.append( "=" );
                    break;
                default:
                    break;
            }
            return tmpResult.toString( );
        }

        @Override
        public byte[ ] decodeBase64( String text )
            throws CryptoAPIException {
            if ( ( text.length( ) % 4 ) != 0 ) {
                throw new CryptoAPIException_JCE( "Tamanho inválido de código Base64" );
            }
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
                tmpAlphabetIndex = this.base64Alphabet.indexOf( tmpChar );
                if ( tmpAlphabetIndex < 0 ) {
                    throw new CryptoAPIException_JCE( "Alfabeto Base64 inválido" );
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
                    throw new CryptoAPIException_JCE( "Codificação Base64 inválida" );
            }
            return Arrays.copyOf( tmpDraft,
                                  tmpResultLen );
        }
    }

    protected class Random_JCE implements Random {

        private SecureRandom mRandom;

        protected Random_JCE( )
            throws CryptoAPIException {
            try {
                mRandom = SecureRandom.getInstance( "SHA1PRNG" );
            }
            catch ( NoSuchAlgorithmException e ) {
                throw new CryptoAPIException_JCE( "Falha iniciando objeto Random",
                                                  e );
            }
        }

        @Override
        public byte[ ] generate( int len )
            throws CryptoAPIException {
            byte[ ] tmpResult = new byte[ len ];
            mRandom.nextBytes( tmpResult );
            return tmpResult;
        }
    }

    protected class SHA1_JCE implements SHA1 {

        MessageDigest mMessageDigest;

        protected SHA1_JCE( )
            throws CryptoAPIException {
            try {
                mMessageDigest = MessageDigest.getInstance( "SHA-1" );
            }
            catch ( Throwable e ) {
                throw new CryptoAPIException_JCE( "Falha iniciando objeto SHA-1",
                                                  e );
            }
        }

        @Override
        public byte[ ] run( byte[ ] message ) {
            return mMessageDigest.digest( message );
        }
    }

    protected class DES_JCE implements DES {

        private class JCE_DES_Wrapper {

            protected Cipher mEncryptCipher;

            protected Cipher mDecryptCipher;

            protected JCE_DES_Wrapper( byte[ ] key )
                throws CryptoAPIException_JCE {
                if ( key.length != KEY_BYTE_LEN ) {
                    throw new CryptoAPIException_JCE( "Tamanho inválido de chave DES" );
                }
                try {
                    KeySpec tmpKeySpec = new DESKeySpec( key );
                    SecretKey tmpSecretKey = SecretKeyFactory.getInstance( "DES" ).generateSecret( tmpKeySpec );
                    mEncryptCipher = Cipher.getInstance( "DES/ECB/NoPadding" );
                    mEncryptCipher.init( Cipher.ENCRYPT_MODE,
                                         tmpSecretKey );
                    mDecryptCipher = Cipher.getInstance( "DES/ECB/NoPadding" );
                    mDecryptCipher.init( Cipher.DECRYPT_MODE,
                                         tmpSecretKey );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha iniciando objeto DES",
                                                      e );
                }
            }

            protected byte[ ] encrypt( byte[ ] plaintext )
                throws CryptoAPIException {
                if ( ( plaintext.length % KEY_BYTE_LEN ) != 0 ) {
                    throw new CryptoAPIException_JCE( "DES exige tamanho de mensagem múltiplo do tamanho da chave" );
                }
                byte[ ] tmpResult = null;
                try {
                    tmpResult = mEncryptCipher.doFinal( plaintext );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha cifrando DES",
                                                      e );
                }
                return tmpResult;
            }

            protected byte[ ] decrypt( byte[ ] crypttext )
                throws CryptoAPIException {
                if ( ( crypttext.length % KEY_BYTE_LEN ) != 0 ) {
                    throw new CryptoAPIException_JCE( "DES exige tamanho de mensagem múltiplo do tamanho da chave" );
                }
                byte[ ] tmpResult = null;
                try {
                    tmpResult = mDecryptCipher.doFinal( crypttext );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha decifrando DES",
                                                      e );
                }
                return tmpResult;
            }
        }

        private JCE_DES_Wrapper mJceWrapper1;

        private JCE_DES_Wrapper mJceWrapper2;

        private JCE_DES_Wrapper mJceWrapper3;

        protected DES_JCE( byte[ ] key )
            throws CryptoAPIException {
            mJceWrapper1 = new JCE_DES_Wrapper( key );
            mJceWrapper2 = null;
            mJceWrapper3 = null;
        }

        protected DES_JCE( byte[ ] key1,
                           byte[ ] key2 )
            throws CryptoAPIException {
            mJceWrapper1 = new JCE_DES_Wrapper( key1 );
            mJceWrapper2 = new JCE_DES_Wrapper( key2 );
            mJceWrapper3 = null;
        }

        protected DES_JCE( byte[ ] key1,
                           byte[ ] key2,
                           byte[ ] key3 )
            throws CryptoAPIException {
            mJceWrapper1 = new JCE_DES_Wrapper( key1 );
            mJceWrapper2 = new JCE_DES_Wrapper( key2 );
            mJceWrapper3 = new JCE_DES_Wrapper( key3 );
        }

        @Override
        public byte[ ] encrypt( byte[ ] plaintext )
            throws CryptoAPIException {
            byte[ ] tmpResult = null;
            if ( mJceWrapper3 != null ) {
                tmpResult = mJceWrapper3.encrypt( mJceWrapper2.decrypt( mJceWrapper1.encrypt( plaintext ) ) );
            }
            else if ( mJceWrapper2 != null ) {
                tmpResult = mJceWrapper1.encrypt( mJceWrapper2.decrypt( mJceWrapper1.encrypt( plaintext ) ) );
            }
            else {
                tmpResult = mJceWrapper1.encrypt( plaintext );
            }
            return tmpResult;
        }

        @Override
        public byte[ ] decrypt( byte[ ] ciphertext )
            throws CryptoAPIException {
            byte[ ] tmpResult = null;
            if ( mJceWrapper3 != null ) {
                tmpResult = mJceWrapper1.decrypt( mJceWrapper2.encrypt( mJceWrapper3.decrypt( ciphertext ) ) );
            }
            else if ( mJceWrapper2 != null ) {
                tmpResult = mJceWrapper1.decrypt( mJceWrapper2.encrypt( mJceWrapper1.decrypt( ciphertext ) ) );
            }
            else {
                tmpResult = mJceWrapper1.decrypt( ciphertext );
            }
            return tmpResult;
        }
    }

    protected class Symmetric_JCE implements AES {

        protected String mAlgorithmName;

        protected int    mKeyBitLength;

        protected int    mKeyByteLength;

        protected Cipher mEncryptCipher;

        protected Cipher mDecryptCipher;

        public Symmetric_JCE( String algorithmName,
                              int keyBitLength,
                              boolean cbcMode,
                              byte[ ] key,
                              byte[ ] initVector )
            throws CryptoAPIException {
            this.mAlgorithmName = algorithmName;
            this.mKeyBitLength = keyBitLength;
            this.mKeyByteLength = ( ( ( this.mKeyBitLength + Byte.SIZE - 1 ) ) / Byte.SIZE );
            if ( key.length != this.mKeyByteLength ) {
                throw new CryptoAPIException_JCE( "Chave inválida para %s de %d bits: %d bytes esperados",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength,
                                                  this.mKeyByteLength );
            }
            if ( cbcMode && ( ( initVector == null ) || ( initVector.length != this.mKeyByteLength ) ) ) {
                throw new CryptoAPIException_JCE( "InitVector nulo ou inválido para %s de %d bits: %d bytes esperados",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength,
                                                  this.mKeyByteLength );
            }
            try {
                SecretKeySpec tmpKeySpec = new SecretKeySpec( key,
                                                              this.mAlgorithmName );
                String tmpTransformation = String.format( "%s/%s/NoPadding",
                                                          this.mAlgorithmName,
                                                          cbcMode ? "CBC"
                                                                  : "ECB" );
                this.mEncryptCipher = Cipher.getInstance( tmpTransformation );
                this.mDecryptCipher = Cipher.getInstance( tmpTransformation );
                if ( cbcMode ) {
                    IvParameterSpec tmpInitVector = new IvParameterSpec( initVector );
                    this.mEncryptCipher.init( Cipher.ENCRYPT_MODE,
                                              tmpKeySpec,
                                              tmpInitVector );
                    this.mDecryptCipher.init( Cipher.DECRYPT_MODE,
                                              tmpKeySpec,
                                              tmpInitVector );
                }
                else {
                    this.mEncryptCipher.init( Cipher.ENCRYPT_MODE,
                                              tmpKeySpec );
                    this.mDecryptCipher.init( Cipher.DECRYPT_MODE,
                                              tmpKeySpec );
                }
            }
            catch ( Throwable e ) {
                throw new CryptoAPIException_JCE( e,
                                                  "Falha ao criar/iniciar %s de %d bits",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength );
            }
        }

        @Override
        public byte[ ] encrypt( byte[ ] plaintext )
            throws CryptoAPIException {
            if ( ( plaintext.length % this.mKeyByteLength ) != 0 ) {
                throw new CryptoAPIException_JCE( "PlainText inválido para %s de %d bits: %d bytes esperados",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength,
                                                  this.mKeyByteLength );
            }
            byte[ ] tmpResult = null;
            try {
                tmpResult = this.mEncryptCipher.doFinal( plaintext );
            }
            catch ( Throwable e ) {
                throw new CryptoAPIException_JCE( e,
                                                  "Falha ao cifrar com %s de %d bits",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength );
            }
            return tmpResult;
        }

        @Override
        public byte[ ] decrypt( byte[ ] crypttext )
            throws CryptoAPIException {
            if ( ( crypttext.length % this.mKeyByteLength ) != 0 ) {
                throw new CryptoAPIException_JCE( "CryptText inválido para %s de %d bits: %d bytes esperados",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength,
                                                  this.mKeyByteLength );
            }
            byte[ ] tmpResult = null;
            try {
                tmpResult = mDecryptCipher.doFinal( crypttext );
            }
            catch ( Throwable e ) {
                throw new CryptoAPIException_JCE( e,
                                                  "Falha ao decifrar com %s de %d bits",
                                                  this.mAlgorithmName,
                                                  this.mKeyBitLength );
            }
            return tmpResult;
        }
    }

    protected class RSA_JCE implements RSA {

        public class Key_JCE implements Key {

            protected byte[ ] mExponent;

            protected byte[ ] mModulus;

            protected Cipher  mNoPadCipher;

            protected Cipher  mPKCS1EncryptCipher;

            protected Cipher  mPKCS1DecryptCipher;

            protected Key_JCE( )
                throws CryptoAPIException {
            }

            protected Key_JCE( byte[ ] exponent,
                               byte[ ] modulus,
                               boolean privateKey )
                throws CryptoAPIException {
                if ( modulus.length != KEY_BYTE_LEN ) {
                    throw new CryptoAPIException_JCE( "Tamanho inválido de chave RSA" );
                }
                mExponent = ByteHelper.copyOf( exponent );
                mModulus = ByteHelper.copyOf( modulus );
                try {
                    mNoPadCipher = Cipher.getInstance( "RSA/ECB/NoPadding" );
                    mPKCS1EncryptCipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
                    mPKCS1DecryptCipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
                    java.security.Key tmpKey = null;
                    KeyFactory tmpKeyFactory = KeyFactory.getInstance( "RSA" );
                    byte[ ] tmpModulus = new byte[ mModulus.length + 1 ];
                    tmpModulus[ 0 ] = 0;
                    System.arraycopy( mModulus,
                                      0,
                                      tmpModulus,
                                      1,
                                      mModulus.length );
                    if ( privateKey ) {
                        tmpKey = tmpKeyFactory.generatePrivate( new RSAPrivateKeySpec( new BigInteger( tmpModulus ),
                                                                                       new BigInteger( mExponent ) ) );
                    }
                    else {
                        tmpKey = tmpKeyFactory.generatePublic( new RSAPublicKeySpec( new BigInteger( tmpModulus ),
                                                                                     new BigInteger( mExponent ) ) );
                    }
                    mNoPadCipher.init( Cipher.ENCRYPT_MODE,
                                       tmpKey );
                    mPKCS1EncryptCipher.init( Cipher.ENCRYPT_MODE,
                                              tmpKey );
                    mPKCS1DecryptCipher.init( Cipher.DECRYPT_MODE,
                                              tmpKey );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha iniciando objeto RSA",
                                                      e );
                }
            }

            @Override
            public byte[ ] cipher( byte[ ] message )
                throws CryptoAPIException {
                byte[ ] tmpResult = null;
                try {
                    tmpResult = mNoPadCipher.doFinal( message );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha cifrando RSA",
                                                      e );
                }
                return tmpResult;
            }

            @Override
            public byte[ ] encryptPKCS1( byte[ ] plaintext )
                throws CryptoAPIException {
                byte[ ] tmpResult = null;
                try {
                    tmpResult = mPKCS1EncryptCipher.doFinal( plaintext );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha encriptando RSA-PKCS1",
                                                      e );
                }
                return tmpResult;
            }

            @Override
            public byte[ ] decryptPKCS1( byte[ ] crypttext )
                throws CryptoAPIException {
                byte[ ] tmpResult = null;
                try {
                    tmpResult = mPKCS1DecryptCipher.doFinal( crypttext );
                }
                catch ( Throwable e ) {
                    throw new CryptoAPIException_JCE( "Falha decriptando RSA-PKCS1",
                                                      e );
                }
                return tmpResult;
            }

            @Override
            public byte[ ] getExponent( ) {
                return mExponent;
            }

            @Override
            public byte[ ] getModulus( ) {
                return mModulus;
            }
        }

        Key_JCE mPublicKey;

        Key_JCE mPrivateKey;

        protected RSA_JCE( )
            throws CryptoAPIException {
            try {
                KeyPairGenerator tmpKeyPairGenerator = KeyPairGenerator.getInstance( "RSA" );
                tmpKeyPairGenerator.initialize( new RSAKeyGenParameterSpec( KEY_BYTE_LEN,
                                                                            RSAKeyGenParameterSpec.F4 ) );
                KeyPair tmpKeyPair = tmpKeyPairGenerator.generateKeyPair( );
                RSAPublicKey tmpJCEPublicKey = (RSAPublicKey) tmpKeyPair.getPublic( );
                RSAPrivateKey tmpJCEPrivateKey = (RSAPrivateKey) tmpKeyPair.getPrivate( );
                mPrivateKey = new Key_JCE( tmpJCEPrivateKey.getPrivateExponent( ).toByteArray( ),
                                           tmpJCEPublicKey.getModulus( ).toByteArray( ),
                                           true );
                mPublicKey = new Key_JCE( tmpJCEPublicKey.getPublicExponent( ).toByteArray( ),
                                          tmpJCEPublicKey.getModulus( ).toByteArray( ),
                                          false );
            }
            catch ( Throwable e ) {
                throw new CryptoAPIException_JCE( "Falha criando chaves RSA",
                                                  e );
            }
        }

        protected RSA_JCE( byte[ ] publicExponent,
                           byte[ ] modulus )
            throws CryptoAPIException {
            mPrivateKey = null;
            mPublicKey = new Key_JCE( publicExponent,
                                      modulus,
                                      false );
        }

        protected RSA_JCE( byte[ ] publicExponent,
                           byte[ ] modulus,
                           byte[ ] privateExponent )
            throws CryptoAPIException {
            mPrivateKey = new Key_JCE( privateExponent,
                                       modulus,
                                       true );
            mPublicKey = new Key_JCE( publicExponent,
                                      modulus,
                                      false );
        }

        @Override
        public Key getPrivateKey( )
            throws CryptoAPIException {
            if ( mPrivateKey == null ) {
                throw new CryptoAPIException_JCE( "Nenhuma chave privada RSA para exportar" );
            }
            return mPrivateKey;
        }

        @Override
        public Key getPublicKey( ) {
            return mPublicKey;
        }
    }

    protected static String providerBrief( ) {
        String tmpReturn = "";
        for ( Provider tmpProvider : Security.getProviders( ) ) {
            tmpReturn += ( tmpProvider.toString( ) + "\n" );
            Iterator< Provider.Service > tmpProviderServices = tmpProvider.getServices( ).iterator( );
            while ( tmpProviderServices.hasNext( ) ) {
                tmpReturn += ( "\t" + tmpProviderServices.next( ).toString( ) + "\n" );
            }
            tmpReturn += "\n";
        }
        return tmpReturn;
    }

    @Override
    public Util getUtil( )
        throws CryptoAPIException {
        return new Util_JCE( );
    }

    @Override
    public Random getRandom( )
        throws CryptoAPIException {
        return new Random_JCE( );
    }

    @Override
    public SHA1 getSHA1( )
        throws CryptoAPIException {
        return new SHA1_JCE( );
    }

    @Override
    public DES getDES( byte[ ] key )
        throws CryptoAPIException {
        return new DES_JCE( key );
    }

    @Override
    public DES getDES( byte[ ] key1,
                       byte[ ] key2 )
        throws CryptoAPIException {
        return new DES_JCE( key1,
                            key2 );
    }

    @Override
    public DES getDES( byte[ ] key1,
                       byte[ ] key2,
                       byte[ ] key3 )
        throws CryptoAPIException {
        return new DES_JCE( key1,
                            key2,
                            key3 );
    }

    @Override
    public AES getAES( int keyBitLength,
                       boolean cbcMode,
                       byte[ ] key,
                       byte[ ] initVector )
        throws CryptoAPIException {
        return new Symmetric_JCE( "AES",
                                  keyBitLength,
                                  cbcMode,
                                  key,
                                  initVector );
    }

    @Override
    public AES getAES128CBC( byte[ ] key,
                             byte[ ] initVector )
        throws CryptoAPIException {
        return new Symmetric_JCE( "AES",
                                  128,
                                  true,
                                  key,
                                  initVector );
    }

    @Override
    public RSA getRSA( )
        throws CryptoAPIException {
        return new RSA_JCE( );
    }

    @Override
    public RSA getRSA( byte[ ] publicExponent,
                       byte[ ] modulus )
        throws CryptoAPIException {
        return new RSA_JCE( publicExponent,
                            modulus );
    }

    @Override
    public RSA getRSA( byte[ ] publicExponent,
                       byte[ ] modulus,
                       byte[ ] privateExponent )
        throws CryptoAPIException {
        return new RSA_JCE( publicExponent,
                            modulus,
                            privateExponent );
    }
}
