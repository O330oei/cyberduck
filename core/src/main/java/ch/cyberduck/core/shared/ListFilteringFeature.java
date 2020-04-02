package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CacheReference;
import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

public abstract class ListFilteringFeature {

    private final Session<?> session;

    private Cache<Path> cache
        = PathCache.empty();

    public ListFilteringFeature(final Session<?> session) {
        this.session = session;
    }

    protected Path search(final Path file) throws BackgroundException {
        final AttributedList<Path> list;
        if(!cache.isCached(file.getParent())) {
            // Do not decrypt filenames to match with input
            list = session._getFeature(ListService.class).list(file.getParent(), new DisabledListProgressListener());
            // Cache directory listing
            cache.put(file.getParent(), list);
        }
        else {
            list = cache.get(file.getParent());
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            // Search with specific version and region
            final Path path = list.find(new DefaultPathPredicate(file));
            if(path != null) {
                return path;
            }
        }
        // Try to match path only as the version might have changed in the meantime
        return list.find(new IgnoreDuplicateFilter(
            session.getCaseSensitivity() == Protocol.Case.insensitive ? new CaseInsensitivePathPredicate(file) : new SimplePathPredicate(file))
        );
    }

    public ListFilteringFeature withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Filter previous versions and delete markers
     */
    private static final class IgnoreDuplicateFilter implements CacheReference<Path> {
        private final CacheReference<Path> proxy;

        public IgnoreDuplicateFilter(final CacheReference<Path> proxy) {
            this.proxy = proxy;
        }

        @Override
        public boolean test(final Path file) {
            if(file.attributes().isDuplicate()) {
                return false;
            }
            return proxy.test(file);
        }
    }
}
