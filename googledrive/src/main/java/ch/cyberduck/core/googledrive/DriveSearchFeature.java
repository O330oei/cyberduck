package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Search;

public class DriveSearchFeature implements Search {
    private DriveSession session;
    private DriveFileidProvider fileid;

    public DriveSearchFeature(final DriveSession session) {
        this(session, new DriveFileidProvider(session));
    }

    public DriveSearchFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        try {
            return new DriveSearchListService(session, regex.toPattern().pattern()).list(workdir, listener);
        }
        catch(NotfoundException e) {
            return AttributedList.emptyList();
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

    @Override
    public Search withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}