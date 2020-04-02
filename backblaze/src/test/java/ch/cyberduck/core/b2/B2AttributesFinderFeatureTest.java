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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2AttributesFinderFeatureTest extends AbstractB2Test {

    @Test
    public void testFindLargeUpload() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2FileidProvider fileid = new B2FileidProvider(session);
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
            fileid.withCache(cache).getFileid(bucket, new DisabledListProgressListener()),
            file.getName(), null, Collections.emptyMap());
        assertSame(PathAttributes.EMPTY, new B2AttributesFinderFeature(session, fileid).find(file));
        final Path found = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).find(
            new SimplePathPredicate(file));
        assertTrue(found.getType().contains(Path.Type.upload));
        new B2ReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback()).close();
        new B2ReadFeature(session, fileid).read(found, new TransferStatus(), new DisabledConnectionCallback()).close();
        assertNotNull(fileid.getFileid(file, new DisabledListProgressListener()));
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
    }
}
