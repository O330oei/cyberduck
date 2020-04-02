package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;

public abstract class AppendWriteFeature<Reply> implements Write<Reply> {

    private final Find finder;
    private final AttributesFinder attribute;

    protected AppendWriteFeature(final Session<?> session) {
        this.finder = session.getFeature(Find.class, new DefaultFindFeature(session));
        this.attribute = session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session));
    }

    protected AppendWriteFeature(final Find finder, final AttributesFinder attributes) {
        this.finder = finder;
        this.attribute = attributes;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attribute.withCache(cache).find(file);
            return new Append(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }
}
