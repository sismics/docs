package com.sismics.docs.core.util;

import com.google.common.base.Strings;
import com.sismics.docs.core.model.context.AppContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.stream.Stream;

/**
 * Encryption utilities.
 * 
 * @author bgamard
 */
public class EncryptionUtil {
    /**
     * Salt.
     */
    private static final String SALT = "LEpxZmm2SMu2PeKzPNrar2rhVAS6LrrgvXKeL9uyXC4vgKHg";

    private static final String LEGACY_CIPHER_SPEC = "AES/CTR/NOPADDING";
    public static final String DEFAULT_CIPHER_SPEC = LEGACY_CIPHER_SPEC;
    
    static {
        // Initialize Bouncy Castle provider
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Security.removeProvider("SunRsaSign");
    }
    
    /**
     * Generate a private key.
     * 
     * @return New random private key
     */
    public static String generatePrivateKey() {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            return new BigInteger(176, random).toString(32);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Decrypt an InputStream using the specified private key.
     * 
     * @param is InputStream to encrypt
     * @param privateKey Private key
     * @return Encrypted stream
     * @throws Exception  e
     */
    public static InputStream decryptInputStream(InputStream is, String privateKey, String cipherSpec) throws Exception {
        return new CipherInputStream(is, getCipher(privateKey, Cipher.DECRYPT_MODE, cipherSpec));
    }

    /**
     * Decrypt a file to a temporary file using the specified private key.
     *
     * @param file Encrypted file
     * @param privateKey Private key
     * @return Decrypted temporary file
     * @throws Exception e
     */
    public static Path decryptFile(Path file, String privateKey, String cipherSpec) throws Exception {
        if (privateKey == null) {
            // For unit testing
            return file;
        }

        Path tmpFile = AppContext.getInstance().getFileService().createTemporaryFile();
        try (InputStream is = Files.newInputStream(file)) {
            Files.copy(new CipherInputStream(is, getCipher(privateKey, Cipher.DECRYPT_MODE, cipherSpec)), tmpFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tmpFile;
    }

    /**
     * Return an encryption cipher.
     * 
     * @param privateKey Private key
     * @return Encryption cipher
     * @throws Exception e
     */
    public static Cipher getEncryptionCipher(String privateKey, String cipherSpec) throws Exception {
        if (Strings.isNullOrEmpty(privateKey)) {
            throw new IllegalArgumentException("The private key is null or empty");
        }
        return getCipher(privateKey, Cipher.ENCRYPT_MODE, cipherSpec);
    }

    public static String getEncryptionCipherSpec() {
        return System.getProperty("teedy.cipher", DEFAULT_CIPHER_SPEC);
    }

    public static String getDecryptionCipherSpec(Path specFile) throws Exception {
	String cipherSpec = LEGACY_CIPHER_SPEC;
	if (Files.exists(specFile)) {
            try (Stream<String> lines = Files.lines(specFile)) {
                cipherSpec = lines.findFirst().orElse(cipherSpec);
            }
        }
        return cipherSpec;
    }
    
    /**
     * Initialize a Cipher.
     * 
     * @param privateKey Private key
     * @param mode Mode (encrypt or decrypt)
     * @return Cipher
     * @throws Exception e
     */
    private static Cipher getCipher(String privateKey, int mode, String cipherSpec) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(privateKey.toCharArray(), SALT.getBytes(), 2000, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWITHSHA256AND256BITAES-CBC-BC");
        SecretKey desKey = skf.generateSecret(keySpec);
        Cipher cipher = "NULL".equals(cipherSpec) // the NULL cipher may be disabled, but we want it to be usable for Teedy
		? new javax.crypto.NullCipher()
		: Cipher.getInstance(cipherSpec);
        cipher.init(mode, desKey);
        return cipher;
    }
}
