package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

public class MonitorFolderHostCollectionTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.get().register(new TestProtocol());
    }

    @Test
    public void testLoad() throws Exception {
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final MonitorFolderHostCollection c = new MonitorFolderHostCollection(source);
        c.load();
        final Host bookmark = new Host(new TestProtocol());
        c.add(bookmark);
        assertEquals(1, c.size());
        bookmark.setLabels(Collections.singleton("l"));
        c.collectionItemChanged(bookmark);
        assertEquals(1, c.size());
    }
}
