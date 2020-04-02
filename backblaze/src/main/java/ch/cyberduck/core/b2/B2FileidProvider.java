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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2BucketResponse;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2ListFilesResponse;

public class B2FileidProvider implements IdProvider {

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;

    private Cache<Path> cache = PathCache.empty();

    public B2FileidProvider(final B2Session session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        try {
            if(cache.isCached(file.getParent())) {
                final AttributedList<Path> list = cache.get(file.getParent());
                final Path found = list.find(new SimplePathPredicate(file));
                if(null != found) {
                    if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                        // Cache in file attributes
                        return set(file, found.attributes().getVersionId());
                    }
                }
            }
            if(containerService.isContainer(file)) {
                final B2BucketResponse info = session.getClient().listBucket(file.getName());
                // Cache in file attributes
                return this.set(file, info.getBucketId());
            }
            if(cache.isCached(file.getParent())) {
                final AttributedList<Path> list = cache.get(file.getParent());
                final Path found = list.find(new SimplePathPredicate(file));
                if(null != found) {
                    if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                        // Cache in file attributes
                        return this.set(file, found.attributes().getVersionId());
                    }
                }
            }
            final B2ListFilesResponse response = session.getClient().listFileNames(
                this.getFileid(containerService.getContainer(file), listener),
                containerService.getKey(file), 2);
            for(B2FileInfoResponse info : response.getFiles()) {
                if(StringUtils.equals(containerService.getKey(file), info.getFileName())) {
                    // Cache in file attributes
                    return this.set(file, info.getFileId());
                }
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    protected String set(final Path file, final String id) {
        file.attributes().setVersionId(id);
        return id;
    }

    @Override
    public B2FileidProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
