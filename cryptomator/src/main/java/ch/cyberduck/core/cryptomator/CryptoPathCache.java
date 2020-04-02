package ch.cyberduck.core.cryptomator;

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
import ch.cyberduck.core.CacheReference;
import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

import java.util.Map;

public final class CryptoPathCache implements Cache<Path> {
    private static final Logger log = Logger.getLogger(CryptoPathCache.class);

    private final Cache<Path> delegate;

    public CryptoPathCache(final Cache<Path> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isValid(final Path file) {
        return delegate.isValid(this.toDecrypted(file));
    }

    @Override
    public CacheReference<?> reference(final Path object) {
        return delegate.reference(object);
    }

    @Override
    public boolean isCached(final Path folder) {
        return delegate.isCached(this.toDecrypted(folder));
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public AttributedList<Path> put(final Path folder, final AttributedList<Path> encrypted) {
        final AttributedList<Path> list = new AttributedList<>();
        // Swap with decrypted paths
        for(int i = 0; i < encrypted.size(); i++) {
            final Path f = encrypted.get(i);
            list.add(i, this.toDecrypted(f));
        }
        return delegate.put(this.toDecrypted(folder), list);
    }

    @Override
    public AttributedList<Path> get(final Path folder) {
        final AttributedList<Path> decrypted = delegate.get(this.toDecrypted(folder));
        final AttributedList<Path> list = new AttributedList<>();
        // Swap with encrypted paths
        for(int i = 0; i < decrypted.size(); i++) {
            final Path f = decrypted.get(i);
            list.add(i, this.toEncrypted(f));
        }
        return list;
    }

    @Override
    public AttributedList<Path> remove(final Path folder) {
        return delegate.remove(this.toDecrypted(folder));
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public Map<Path, AttributedList<Path>> asMap() {
        return delegate.asMap();
    }

    @Override
    public void invalidate(final Path folder) {
        delegate.invalidate(this.toDecrypted(folder));
    }

    private Path toDecrypted(final Path file) {
        if(file.getType().contains(Path.Type.encrypted)) {
            if(null == file.attributes().getDecrypted()) {
                log.error(String.format("Missing decrypted reference for %s", file));
            }
            return file.attributes().getDecrypted();
        }
        return file;
    }

    private Path toEncrypted(final Path file) {
        if(file.getType().contains(Path.Type.decrypted)) {
            if(null == file.attributes().getEncrypted()) {
                log.error(String.format("Missing encrypted reference for %s", file));
            }
            return file.attributes().getEncrypted();
        }
        return file;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Path lookup(final CacheReference<Path> reference) {
        return delegate.lookup(reference);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoPathCache{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
