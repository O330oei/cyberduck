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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2FileidProviderTest extends AbstractB2Test {

    @Test
    public void getFileIdFile() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path file = new B2TouchFeature(session, fileid).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(fileid.getFileid(file, new DisabledListProgressListener()));
        try {
            assertNull(fileid.getFileid(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdDirectory() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertNotNull(fileid.getFileid(folder, new DisabledListProgressListener()));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(folder, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFileIdCollision() throws Exception {
        final B2FileidProvider idProvider = new B2FileidProvider(session);
        final Path bucket = new B2DirectoryFeature(session, idProvider).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path path2R = new Path(bucket, "2R", EnumSet.of(Path.Type.directory));
        final Path path33 = new Path(bucket, "33", EnumSet.of(Path.Type.directory));

        final Directory directoryFeature = new B2DirectoryFeature(session, idProvider);
        final Path path2RWithId = directoryFeature.mkdir(path2R, null, new TransferStatus());
        assertNotNull(path2RWithId.attributes().getVersionId());
        final Path path33WithId = directoryFeature.mkdir(path33, null, new TransferStatus());
        assertNotNull(path33WithId.attributes().getVersionId());
        assertNotEquals(path2RWithId.attributes().getVersionId(), path33WithId.attributes().getVersionId());

        final String fileId = idProvider.getFileid(path33, new DisabledListProgressListener());

        assertEquals(fileId, path33WithId.attributes().getVersionId());
        assertNotEquals(fileId, path2RWithId.attributes().getVersionId());

        new B2DeleteFeature(session, idProvider).delete(Arrays.asList(path2RWithId, path33WithId, bucket), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
