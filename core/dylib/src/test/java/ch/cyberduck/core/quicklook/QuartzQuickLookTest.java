package ch.cyberduck.core.quicklook;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.ui.quicklook.QuickLook;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class QuartzQuickLookTest {

    @Test
    public void testSelect() {
        QuickLook q = new QuartzQuickLook();
        final List<Local> files = new ArrayList<Local>();
        files.add(new NullLocal("f"));
        files.add(new NullLocal("b"));
        q.select(files);
    }
}
