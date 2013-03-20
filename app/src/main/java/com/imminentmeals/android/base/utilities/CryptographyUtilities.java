package com.imminentmeals.android.base.utilities;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <p>Collection of utilities to encrypt and decipher strings. A SEED value is used as a shared secret ("Master-Password").
 * Only with the same SEED can the stored values can be decrypted.</p>
 *
 * Usage:
 * <pre>
 * String digest = cryptography.encrypt(masterpassword, message)
 * ...
 * String message = cryptography.decipher(masterpassword, digest)
 * </pre>
 * @author ferenc.hechler (http://www.androidsnippets.com/encryptdecrypt-strings)
 * @author Dandré Allison
 */
@Singleton
public class CryptographyUtilities {
    /** Name of the secret key stored in the {@link android.content.SharedPreferences} (Note: expected to be
     * a {@link String}) */
    public static final String KEY_SECRET_KEY = "com.imminentmeals.android.base.utilities.key."
                                                + "CryptographyUtilities.SECRET_KEY";
    /** Name of the algorithm used */
    public static final String AES = "AES";

    /**
     * <p>Constructs the {@link CryptographyUtilitiesTest}.</p>
     * @param secret_key The key used to encrypt and decipher messages
     */
    @Inject
    public CryptographyUtilities(@Nonnull SecretKey secret_key) {
        _SECRET_KEY = secret_key;
    }

    /**
     * <p>Encrypts the message, so that only with the current {@link SecretKey} can it be deciphered.</p>
     * @param message The message to encrypt
     * @return The encrypted message
     * @throws UnsupportedEncodingException Indicates the String encoding ({@link #_CHARSET}) not valid
     * @throws NoSuchPaddingException Indicates no installed provider can provide the padding scheme in the transformation
     * @throws NoSuchAlgorithmException Indicates an invalid encryption algorithm was requested
     * @throws BadPaddingException Indicates the padding of the data doesn't match the padding scheme
     * @throws IllegalBlockSizeException Indicates the size of the resulting bytes is not a multiple of the cipher block
     *                                   size
     * @throws InvalidKeyException Indicates an invalid key ({@link #_SECRET_KEY}) was used
     */
    @Nonnull public String encrypt(@Nonnull String message)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
                   NoSuchPaddingException, UnsupportedEncodingException  {
        final byte[] digest = encrypt(_SECRET_KEY.getEncoded(), message.getBytes(_CHARSET));
        return toHex(digest);
    }

    /**
     * <p>Deciphers the digest using the current {@link SecretKey}, which must be the same key used to encrypt
     * the message to work correctly.
     * @param digest The digest to decipher
     * @return The deciphered message
     * @throws NoSuchPaddingException Indicates no installed provider can provide the padding scheme in the transformation
     * @throws NoSuchAlgorithmException Indicates an invalid encryption algorithm was requested
     * @throws BadPaddingException Indicates the padding of the data doesn't match the padding scheme
     * @throws IllegalBlockSizeException Indicates the size of the resulting bytes is not a multiple of the cipher block
     *                                   size
     * @throws InvalidKeyException Indicates an invalid key ({@link #_SECRET_KEY}) was used
     * @throws UnsupportedEncodingException Indicates the String encoding ({@link #_CHARSET}) not valid
     */
    @Nonnull public String decipher(@Nonnull String digest)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
                   NoSuchPaddingException, UnsupportedEncodingException {
        final byte[] encrypted_message = toByte(digest);
        final byte[] message = decipher(_SECRET_KEY.getEncoded(), encrypted_message);
        return new String(message, _CHARSET);
    }

    /**
     * <p>Generates a {@link SecretKey} to be used to encrypt/decipher messages.</p>
     * @return A secret key
     * @throws NoSuchAlgorithmException Indicates an invalid encryption algorithm was requested
     */
    @Nonnull public static SecretKey generateKey() throws NoSuchAlgorithmException {
        final KeyGenerator key_generator = KeyGenerator.getInstance(AES);
        key_generator.init(_KEY_LENGTH, new SecureRandom());
        return key_generator.generateKey();
    }

    /**
     * <p>Generates a {@link SecretKey} to be used to encrypt/decipher messages.</p>
     * @param passphrase The passphrase used to generate the secret key
     * @param salt The salt added to obscure the secret key
     * @return A secret key
     * @throws NoSuchAlgorithmException Indicates an invalid encryption algorithm was requested
     * @throws InvalidKeySpecException Indicates an invalid key specification was requested
     */
    @Nonnull public static SecretKey generateKey(char[] passphrase, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 1000;

        final SecretKeyFactory key_factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final KeySpec key_spec = new PBEKeySpec(passphrase, salt, iterations, _KEY_LENGTH);
        return key_factory.generateSecret(key_spec);
    }

/* Helpers */
    @Nonnull private static byte[] encrypt(@Nonnull byte[] raw, @Nonnull byte[] clear)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
                   NoSuchPaddingException {
        final SecretKeySpec private_key_spec = new SecretKeySpec(raw, AES);
        final Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, private_key_spec);
        return cipher.doFinal(clear);
    }

    @Nonnull private static byte[] decipher(@Nonnull byte[] raw, @Nonnull byte[] digest)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException {
        final SecretKeySpec private_key_spec = new SecretKeySpec(raw, AES);
        final Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, private_key_spec);
        return cipher.doFinal(digest);
    }

    @Nonnull private static byte[] toByte(@Nonnull String hex_string) {
        final int length = hex_string.length()/2;
        final byte[] result = new byte[length];
        for (int i = 0; i < length; i++)
            result[i] = Integer.valueOf(hex_string.substring(2 * i, 2 * i + 2), _BASE_16).byteValue();
        return result;
    }

    @Nonnull private static String toHex(@Nonnull byte[] buffer) {
        if (buffer == null) return "";
        final StringBuilder result = new StringBuilder(2 * buffer.length);
        for (byte b : buffer)
            appendHex(result, b);

        return result.toString();
    }

    private static void appendHex(@Nonnull StringBuilder string_builder, byte b) {
        string_builder.append(_HEX.charAt((b >> _APPEND_HEX_OFFSET) & _HEX_BIT_MASK))
                      .append(_HEX.charAt(b & _HEX_BIT_MASK));
    }

    /** Hexadecimal characters, used to bytes into hexadecimal */
    private static final String _HEX = "0123456789ABCDEF";
    /** Defines the length of generated {@link SecretKey}s */
    private static final int _KEY_LENGTH = 256;
    /** Defines the character set encoding */
    private static final String _CHARSET = "UTF-8";
    /** Bit shift used in {@link #appendHex(StringBuilder, byte)} */
    private static final int _APPEND_HEX_OFFSET = 4;
    /** Bit mask used in {@link #appendHex(StringBuilder, byte)} */
    private static final int _HEX_BIT_MASK = 0x0f;
    /** Hexadecimal number radix */
    private static final int _BASE_16 = 16;
    /** The {@link SecretKey} */
    private final SecretKey _SECRET_KEY;
}
