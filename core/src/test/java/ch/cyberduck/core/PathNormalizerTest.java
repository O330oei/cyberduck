package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathNormalizerTest {

    @Test
    public void testNormalize() {
        assertEquals(PathNormalizer.normalize("relative/path", false), "relative/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", true), "/absolute/path");
        assertEquals(PathNormalizer.normalize("/absolute/path", false), "/absolute/path");
    }

    @Test
    public void test972() {
        assertEquals("//home/path", PathNormalizer.normalize("//home/path"));
    }

    @Test
    public void testName() {
        assertEquals("p", PathNormalizer.name("/p"));
        assertEquals("n", PathNormalizer.name("/p/n"));
        assertEquals("p", PathNormalizer.name("p"));
        assertEquals("n", PathNormalizer.name("p/n"));
    }

    @Test
    public void testParent() {
        assertEquals("/", PathNormalizer.parent("/p", '/'));
        assertEquals("/p", PathNormalizer.parent("/p/n", '/'));
        assertNull(PathNormalizer.parent("/", '/'));
    }

    @Test
    public void testDoubleDot() {
        assertEquals("/", PathNormalizer.normalize("/.."));
        assertEquals("/p", PathNormalizer.normalize("/p/n/.."));
        assertEquals("/n", PathNormalizer.normalize("/p/../n"));
        assertEquals("/", PathNormalizer.normalize(".."));
        assertEquals("/", PathNormalizer.normalize("."));
    }

    @Test
    public void testDot() {
        assertEquals("/p", PathNormalizer.normalize("/p/."));
        assertEquals("/", PathNormalizer.normalize("/."));
    }

    @Test
    public void testPathNormalize() {
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/.."), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/.././"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/remove/../to/remove/.././"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/to/remove/remove/../../"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/././././to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "./.path/to"), EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                ".path/to"), EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path/.to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/.to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path//to"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(PathNormalizer.normalize(
                "/path///to////"), EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
    }

    @Test
    public void testPathName() {
        {
            Path path = new Path(PathNormalizer.normalize(
                "/path/to/file/"), EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = new Path(PathNormalizer.normalize(
                "/path/to/file"), EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    @Test
    public void testNormalizeNameWithBackslash() {
        assertEquals("file\\name", PathNormalizer.name("/path/to/file\\name"));
    }
}
