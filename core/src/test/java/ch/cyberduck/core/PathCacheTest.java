package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class PathCacheTest {

    @Test
    public void testLookup() {
        final Cache<Path> cache = new ReverseLookupCache<Path>(new PathCache(1), 1);
        assertNull(cache.lookup(new DefaultPathPredicate(new Path("/", EnumSet.of(Path.Type.directory)))));
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path directory = new Path("p", EnumSet.of(Path.Type.directory));
        final Path file1 = new Path(directory, "name1", EnumSet.of(Path.Type.file));
        list.add(file1);
        final Path file2 = new Path(directory, "name2", EnumSet.of(Path.Type.file));
        list.add(file2);
        cache.put(directory, list);
        assertNotNull(cache.lookup(new DefaultPathPredicate(file1)));
        assertNotNull(cache.lookup(new DefaultPathPredicate(file2)));
    }

    @Test
    public void testIsEmpty() {
        final PathCache cache = new PathCache(1);
        assertTrue(cache.isEmpty());
        cache.put(new Path("/", EnumSet.of(Path.Type.directory)), new AttributedList<Path>());
        assertFalse(cache.isEmpty());
    }

    @Test
    public void testContainsKey() {
        final PathCache cache = new PathCache(1);
        final Path f = new Path("/", EnumSet.of(Path.Type.directory));
        assertFalse(cache.containsKey(f));
        cache.put(f, new AttributedList<Path>());
        final CacheReference reference = new DefaultPathPredicate(new Path("/", EnumSet.of(Path.Type.directory)));
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
    }

    @Test
    public void testInvalidate() {
        final PathCache cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<Path>();
        final Path f = new Path("/t", EnumSet.of(Path.Type.directory));
        cache.put(f, list);
        assertFalse(cache.get(f).attributes().isInvalid());
        cache.invalidate(f);
        assertTrue(cache.get(f).attributes().isInvalid());
        assertTrue(cache.containsKey(f));
        assertTrue(cache.isCached(f));
        assertFalse(cache.isValid(f));
    }

    @Test
    public void testGet() {
        final PathCache cache = new PathCache(1);
        final Path file = new Path("name", EnumSet.of(Path.Type.file));
        assertEquals(AttributedList.<Path>emptyList(), cache.get(file));
    }

    @Test
    public void testDisabledCache() {
        PathCache cache = PathCache.empty();
        final Path file = new Path("name", EnumSet.of(Path.Type.file));
        cache.put(file, AttributedList.<Path>emptyList());
        assertFalse(cache.containsKey(file));
        assertEquals(0, cache.size());
    }
}
