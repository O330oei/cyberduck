package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URIEncoderTest {

    @Test
    public void testEncode() {
        assertEquals("/p", URIEncoder.encode("/p"));
        assertEquals("/p%20d", URIEncoder.encode("/p d"));
    }

    @Test
    public void testEncodeHash() {
        assertEquals("file%23", URIEncoder.encode("file#"));
    }

    @Test
    public void testEncodeTrailingDelimiter() {
        assertEquals("/a/p/", URIEncoder.encode("/a/p/"));
        assertEquals("/p%20d/", URIEncoder.encode("/p d/"));
    }

    @Test
    public void testEncodeRelativeUri() {
        assertEquals("a/p", URIEncoder.encode("a/p"));
        assertEquals("a/p/", URIEncoder.encode("a/p/"));
    }
}
