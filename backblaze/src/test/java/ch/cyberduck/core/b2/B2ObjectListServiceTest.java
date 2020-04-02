package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2ObjectListServiceTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2ObjectListService(session, fileid).list(new Path(bucket, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new B2ObjectListService(session, fileid).list(folder, new DisabledListProgressListener()).isEmpty());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfoundContainer() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new Path("notfound-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
    }

    @Test
    public void testList() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        status.setChecksum(Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        IOUtils.write(new byte[0], out);
        out.close();
        final B2FileResponse resopnse = (B2FileResponse) out.getStatus();
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(file)));
        assertEquals(Long.valueOf(1L), list.find(new SimplePathPredicate(file)).attributes().getRevision());
        assertEquals(0L, list.find(new SimplePathPredicate(file)).attributes().getSize());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).contains(file));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListChunking() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new B2TouchFeature(session, fileid).touch(new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid, 1).list(bucket, new DisabledListProgressListener());
        assertTrue(list.contains(file1));
        assertTrue(list.contains(file2));

        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListRevisions() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final String name = UUID.randomUUID().toString();
        final Path file1 = new Path(bucket, name, EnumSet.of(Path.Type.file));
        final Path file2 = new Path(bucket, name, EnumSet.of(Path.Type.file));
        {
            final byte[] content = RandomUtils.nextBytes(1);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file1, status, new DisabledConnectionCallback());
            IOUtils.write(content, out);
            out.close();
            final B2FileResponse resopnse = (B2FileResponse) out.getStatus();
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            file1.attributes().setVersionId(resopnse.getFileId());
            assertTrue(list.contains(file1));
            assertEquals(Long.valueOf(1L), list.find(new SimplePathPredicate(file1)).attributes().getRevision());
            assertEquals(content.length, list.find(new SimplePathPredicate(file1)).attributes().getSize());
            assertEquals(bucket, list.find(new SimplePathPredicate(file1)).getParent());
        }
        // Replace
        {
            final byte[] content = RandomUtils.nextBytes(1);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file2, status, new DisabledConnectionCallback());
            IOUtils.write(content, out);
            out.close();
            final B2FileResponse resopnse = (B2FileResponse) out.getStatus();
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            file2.attributes().setVersionId(resopnse.getFileId());
            assertTrue(list.contains(file2));
            assertEquals(Long.valueOf(1L), list.get(file2).attributes().getRevision());
            assertFalse(list.get(file2).attributes().isDuplicate());
            assertTrue(list.contains(file1));
            assertEquals(Long.valueOf(2L), list.get(file1).attributes().getRevision());
            assertTrue(list.get(file1).attributes().isDuplicate());
            assertEquals(bucket, list.get(file1).getParent());
        }
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        {
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            assertNull(list.find(new SimplePathPredicate(file1)));
            assertNull(list.find(new SimplePathPredicate(file2)));
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFolder() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path folder1 = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path folder2 = new B2DirectoryFeature(session, fileid).mkdir(new Path(folder1, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(folder1, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new B2TouchFeature(session, fileid).touch(new Path(folder2, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(folder1, new DisabledListProgressListener());
        // Including
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/.bzEmpty', type=[file]}
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/b136e277-3ee0-49f0-b19f-4a66eb7d8f38', type=[directory, placeholder]}
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/c2cbb949-2877-416d-9eb5-0855279adde3', type=[file]}
        assertEquals(2, list.size());
        assertNotNull(list.find(new SimplePathPredicate(file1)));
        assertNotNull(list.find(new SimplePathPredicate(folder2)));
        assertNull(list.find(new SimplePathPredicate(file2)));
        assertNull(list.find(new SimplePathPredicate(folder1)));
        assertEquals(folder1, list.find(new SimplePathPredicate(file1)).getParent());
        assertEquals(folder1, list.find(new SimplePathPredicate(folder2)).getParent());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, folder1, file1, folder2, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDisplayFolderInBucketMissingPlaceholder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, null, new TransferStatus());
        final Path folder1 = new Path(bucket, "1-d", EnumSet.of(Path.Type.directory));
        final Path file1 = new Path(folder1, "2-f", EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file1, new TransferStatus());

        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(folder1, list.iterator().next());

        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file1), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDisplayFolderInFolderMissingPlaceholder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, null, new TransferStatus());
        final Path folder1 = new Path(bucket, "1-d", EnumSet.of(Path.Type.directory));
        final Path folder2 = new Path(folder1, "2-d", EnumSet.of(Path.Type.directory));
        final Path file11 = new Path(folder2, "31-f", EnumSet.of(Path.Type.file));
        final Path file12 = new Path(folder2, "32-f", EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file11, new TransferStatus());
        new B2TouchFeature(session, fileid).touch(file12, new TransferStatus());

        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(folder1, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(folder2, list.iterator().next());

        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file11, file12), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testIdenticalNamingFileFolder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, null, new TransferStatus());
        final String name = UUID.randomUUID().toString();
        final Path folder1 = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertTrue(list.contains(file1));
        assertTrue(list.contains(folder1));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(file1, folder1, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
