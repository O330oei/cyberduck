package ch.cyberduck.core.s3;

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3WriteFeatureTest extends AbstractS3Test {

    @Test
    public void testAppendBelowLimit() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) {
                return true;
            }
        }, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file) {
                return new PathAttributes();
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
    }

    @Test
    public void testSize() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) {
                return true;
            }
        }, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file) {
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(3L);
                return attributes;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertEquals(3L, append.size, 0L);
    }

    @Test
    public void testAppendNoMultipartFound() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()).append);
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()));
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()));
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS2SignatureFailure() throws Exception {
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        final S3WriteFeature feature = new S3WriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        feature.write(file, status, new DisabledConnectionCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS4Signature() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = new RandomStringGenerator.Builder().build().generate(5 * 1024 * 1024).getBytes(StandardCharsets.UTF_8);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        try {
            feature.write(file, status, new DisabledConnectionCallback());
        }
        catch(InteroperabilityException e) {
            assertEquals("A header you provided implies functionality that is not implemented. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testWriteAWS4Signature() throws Exception {
        final S3WriteFeature feature = new S3WriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = new RandomStringGenerator.Builder().build().generate(5 * 1024 * 1024).getBytes(StandardCharsets.UTF_8);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(file).getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
