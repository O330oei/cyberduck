package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.model.container.ObjectKeyAndVersion;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3MultipleDeleteFeatureTest extends AbstractS3Test {

    @Test
    public void testDeleteFile() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(test, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3MultipleDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
    }

    @Test
    public void testDeleteVersionedPlaceholder() throws Exception {
        final Path container = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        {
            final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
                new Path(container, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
            assertTrue(new S3FindFeature(session).find(test));
            assertTrue(new DefaultFindFeature(session).find(test));
            new S3MultipleDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
            assertFalse(new S3FindFeature(session).find(test));
        }
        {
            final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
                new Path(container, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
            assertTrue(new S3FindFeature(session).find(test));
            assertTrue(new DefaultFindFeature(session).find(test));
            new S3MultipleDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
            assertFalse(new S3FindFeature(session).find(test));
        }
        assertFalse(new S3VersionedObjectListService(session).list(container, new DisabledListProgressListener()).contains(
            new Path(container, name, EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testDeleteContainer() throws Exception {
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(container, null, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(container));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(container,
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final List<ObjectKeyAndVersion> keys = new ArrayList<ObjectKeyAndVersion>();
        for(int i = 0; i < 1010; i++) {
            keys.add(new ObjectKeyAndVersion(UUID.randomUUID().toString()));
        }
        new S3MultipleDeleteFeature(session).delete(container, keys, new DisabledLoginCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3MultipleDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
