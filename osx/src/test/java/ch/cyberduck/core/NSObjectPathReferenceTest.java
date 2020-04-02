package ch.cyberduck.core;

import ch.cyberduck.binding.foundation.NSString;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class NSObjectPathReferenceTest {

    @Test
    public void testUnique() {
        NSObjectPathReference r = new NSObjectPathReference(NSString.stringWithString("a"));
        assertEquals(r, new NSObjectPathReference(NSString.stringWithString("a")));
        assertEquals(r.toString(), new NSObjectPathReference(NSString.stringWithString("a")).toString());
        assertNotSame(r, new NSObjectPathReference(NSString.stringWithString("b")));
        assertNotSame(r.toString(), new NSObjectPathReference(NSString.stringWithString("b")).toString());
    }

    @Test
    public void testEqualConstructors() {
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[file]-/b")).hashCode(),
            NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file))).hashCode()
        );
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[symboliclink]-/d")).hashCode(),
            NSObjectPathReference.get(new Path("/d", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))).hashCode()
        );
    }

    @Test
    public void testInterchange() {
        assertEquals(
            new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file))),
            new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file))))
        );
        assertEquals(
            new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file)))),
            new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file)))
        );
        assertEquals(new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file))).hashCode(),
            new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file)))).hashCode()
        );
    }

    @Test
    public void testUniquePath() {
        Path one = new Path("a", EnumSet.of(Path.Type.file));
        Path second = new Path("a", EnumSet.of(Path.Type.file));
        assertEquals(NSObjectPathReference.get(one), NSObjectPathReference.get(second));
    }

    @Test
    public void testHashcodeCollision() {
        assertNotEquals(
            NSObjectPathReference.get(
                new Path("19", EnumSet.of(Path.Type.file))
            ),
            NSObjectPathReference.get(
                new Path("0X", EnumSet.of(Path.Type.file))
            )
        );
    }
}
