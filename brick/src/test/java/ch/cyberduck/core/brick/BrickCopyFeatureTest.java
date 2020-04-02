package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVCopyFeature;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class BrickCopyFeatureTest extends AbstractBrickTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<String>(new DAVUploadFeature(new DAVWriteFeature(session)),
            new DAVAttributesFinderFeature(session)).touch(test, new TransferStatus());
        final Path copy = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVCopyFeature(session).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new DAVFindFeature(session).find(test));
        assertTrue(new DAVFindFeature(session).find(copy));
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path folder = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DAVDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<String>(new DAVUploadFeature(new DAVWriteFeature(session)), new DAVAttributesFinderFeature(session)).touch(test, new TransferStatus());
        final Path copy = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<String>(new DAVUploadFeature(new DAVWriteFeature(session)), new DAVAttributesFinderFeature(session)).touch(copy, new TransferStatus());
        new DAVCopyFeature(session).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new DAVDeleteFeature(session).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(directory, name, EnumSet.of(Path.Type.file));
        new DAVDirectoryFeature(session).mkdir(directory, null, new TransferStatus());
        new DefaultTouchFeature<String>(new DAVUploadFeature(new DAVWriteFeature(session)), new DAVAttributesFinderFeature(session)).touch(file, new TransferStatus());
        final Path copy = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DAVCopyFeature(session).copy(directory, copy, new TransferStatus(), new DisabledConnectionCallback());
        assertTrue(new DAVFindFeature(session).find(file));
        assertTrue(new DAVFindFeature(session).find(copy));
        assertTrue(new DAVFindFeature(session).find(new Path(copy, name, EnumSet.of(Path.Type.file))));
        new DAVDeleteFeature(session).delete(Arrays.asList(copy, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
