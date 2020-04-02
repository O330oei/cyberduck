package ch.cyberduck.core.s3;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3FindFeatureTest extends AbstractS3Test {

    @Test
    public void testFindNotFound() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3FindFeature f = new S3FindFeature(session);
        assertFalse(f.find(test));
    }

    @Test
    public void testFindUnknownBucket() throws Exception {
        final Path test = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertFalse(new S3FindFeature(session).find(test));
    }

    @Test
    public void testFindBucket() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session).find(container));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new S3FindFeature(new S3Session(new Host(new S3Protocol()))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
