package com.sismics.docs.core.util;

import java.io.InputStream;

import junit.framework.Assert;

import org.bouncycastle.util.io.Streams;
import org.junit.Test;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

/**
 * Test of the encryption utilities.
 * 
 * @author bgamard
 */
public class TestEncryptUtil {

    /**
     * Test private key.
     */
    String pk = "OnceUponATime";
    
    @Test
    public void generatePrivateKeyTest() throws Exception {
        String key = EncryptionUtil.generatePrivateKey();
        System.out.println(key);
        Assert.assertFalse(Strings.isNullOrEmpty(key));
    }
    
    @Test
    public void encryptStreamTest() throws Exception {
        InputStream inputStream = EncryptionUtil.encryptStream(this.getClass().getResourceAsStream("/file/udhr.pdf"), pk);
        byte[] encryptedData = Streams.readAll(inputStream);
        byte[] assertData = Streams.readAll(this.getClass().getResourceAsStream("/file/udhr_encrypted.pdf"));
        Assert.assertTrue(ByteStreams.equal(
                ByteStreams.newInputStreamSupplier(encryptedData),
                ByteStreams.newInputStreamSupplier(assertData)));
    }
    
    @Test
    public void decryptStreamTest() throws Exception {
        InputStream inputStream = EncryptionUtil.decryptStream(this.getClass().getResourceAsStream("/file/udhr_encrypted.pdf"), pk);
        byte[] encryptedData = Streams.readAll(inputStream);
        byte[] assertData = Streams.readAll(this.getClass().getResourceAsStream("/file/udhr.pdf"));
        Assert.assertTrue(ByteStreams.equal(
                ByteStreams.newInputStreamSupplier(encryptedData),
                ByteStreams.newInputStreamSupplier(assertData)));
    }
}
