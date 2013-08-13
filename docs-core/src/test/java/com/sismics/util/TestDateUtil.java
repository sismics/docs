package com.sismics.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Test of the date utilities.
 * 
 * @author jtremeaux
 */
public class TestDateUtil {

    @Test
    public void guessTimezoneCodeTest() throws Exception {
        Assert.assertEquals("Thu, 04 APR 2013 20:37:27 +10", DateUtil.guessTimezoneOffset("Thu, 04 APR 2013 20:37:27 AEST"));
    }
}
