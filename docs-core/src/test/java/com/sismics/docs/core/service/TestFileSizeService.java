package com.sismics.docs.core.service;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.util.mime.MimeType;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TestFileSizeService extends BaseTransactionalTest {

    @Test
    public void processFileTest() throws Exception {
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername("username");
        user.setPassword("12345678");
        user.setEmail("toto@docs.com");
        user.setRoleId("admin");
        user.setStorageQuota(1_000_000L);
        String userId = userDao.create(user, "me");

        FileDao fileDao = new FileDao();
        File file;

        String fileId;

        try(InputStream inputStream = getSystemResourceAsStream(FILE_JPG)) {
            file = new File();
            file.setId("apollo_portrait");
            file.setUserId(userId);
            file.setVersion(0);
            file.setMimeType(MimeType.IMAGE_JPEG);
            file.setSize(File.UNKNOWN_SIZE);
            fileId = fileDao.create(file, userId);
            Cipher cipher = EncryptionUtil.getEncryptionCipher(user.getPrivateKey());
            Files.copy(new CipherInputStream(inputStream, cipher), DirectoryUtil.getStorageDirectory().resolve(fileId), REPLACE_EXISTING);
        }

        FileSizeService fileSizeService = new FileSizeService();
        fileSizeService.processFile(file);
        Assert.assertEquals(fileDao.getFile(fileId).getSize(), Long.valueOf(7907));
    }
}
