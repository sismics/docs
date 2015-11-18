package com.sismics.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Test of the image utilities.
 * 
 * @author bgamard
 */
public class TestImageUtil {

    @Test
    public void computeGravatarTest() throws Exception {
        Assert.assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", ImageUtil.computeGravatar("MyEmailAddress@example.com "));
    }
}
