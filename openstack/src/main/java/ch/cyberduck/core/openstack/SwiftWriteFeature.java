package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftWriteFeature extends AbstractHttpWriteFeature<StorageObject> implements Write<StorageObject> {
    private static final Logger log = Logger.getLogger(SwiftSession.class);

    private final PathContainerService containerService
        = new PathContainerService();

    private final Preferences preferences
        = PreferencesFactory.get();

    private final SwiftSession session;
    private final SwiftSegmentService segmentService;
    private final SwiftObjectListService listService;
    private final Find finder;
    private final AttributesFinder attributes;
    private final SwiftRegionService regionService;

    public SwiftWriteFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService,
            new SwiftObjectListService(session, regionService),
            new SwiftSegmentService(session, regionService),
            new DefaultFindFeature(session));
    }

    public SwiftWriteFeature(final SwiftSession session, final SwiftRegionService regionService,
                             final SwiftObjectListService listService,
                             final SwiftSegmentService segmentService) {
        this(session, regionService, listService, segmentService, new DefaultFindFeature(session));
    }

    public SwiftWriteFeature(final SwiftSession session, final SwiftRegionService regionService,
                             final SwiftObjectListService listService,
                             final SwiftSegmentService segmentService, final Find finder) {
        this(session, regionService, listService, segmentService, finder, new DefaultAttributesFinderFeature(session));
    }

    public SwiftWriteFeature(final SwiftSession session, final SwiftRegionService regionService,
                             final SwiftObjectListService listService,
                             final SwiftSegmentService segmentService,
                             final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.listService = listService;
        this.segmentService = segmentService;
        this.regionService = regionService;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // Submit store run to background thread
        final DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    // Previous
                    final HashMap<String, String> headers = new HashMap<>(status.getMetadata());
                    final Checksum checksum = status.getChecksum();
                    final String etag = session.getClient().storeObject(
                        regionService.lookup(file),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        entity, headers, checksum.algorithm == HashAlgorithm.md5 ? checksum.hash : null);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Saved object %s with checksum %s", file, etag));
                    }
                    final StorageObject stored = new StorageObject(containerService.getKey(file));
                    stored.setMd5sum(etag);
                    stored.setSize(status.getLength());
                    return stored;
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(length >= preferences.getLong("openstack.upload.largeobject.threshold")) {
            if(preferences.getBoolean("openstack.upload.largeobject")) {
                final List<Path> segments;
                long size = 0L;
                try {
                    segments = listService.list(segmentService.getSegmentsDirectory(file, length), new DisabledListProgressListener()).toList();
                    if(segments.isEmpty()) {
                        return Write.notfound;
                    }
                }
                catch(NotfoundException e) {
                    return Write.notfound;
                }
                for(Path segment : segments) {
                    size += segment.attributes().getSize();
                }
                return new Append(size);
            }
        }
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }
}
