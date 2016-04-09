package com.sismics.docs.rest;

import org.junit.Test;

/**
 * Test the theme resource.
 * 
 * @author bgamard
 */
public class TestThemeResource extends BaseJerseyTest {
    /**
     * Test the theme resource.
     */
    @Test
    public void testThemeResource() {
        // Get the stylesheet anonymously
        String stylesheet = target().path("/theme/stylesheet").request()
                .get(String.class);
        System.out.println(stylesheet);
    }
}