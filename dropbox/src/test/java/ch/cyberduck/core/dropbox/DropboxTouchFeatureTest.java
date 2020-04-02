package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class DropboxTouchFeatureTest extends AbstractDropboxTest {

    @Test(expected = AccessDeniedException.class)
    public void testDisallowedName() throws Exception {
        final DropboxTouchFeature touch = new DropboxTouchFeature(session);
        final Path file = new Path(new DefaultHomeFinderService(session).find(), String.format("~%s.tmp", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        assertFalse(touch.isSupported(new DefaultHomeFinderService(session).find(), file.getName()));
        touch.touch(file, new TransferStatus());
    }
}
