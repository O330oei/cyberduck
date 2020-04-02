package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.webloc.UrlFileWriter;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePackageItem;
import org.nuxeo.onedrive.client.OneDriveRemoteItem;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.util.EnumSet;
import java.util.Iterator;

public class GraphItemListService implements ListService {
    private static final Logger log = Logger.getLogger(GraphItemListService.class);

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;
    private final UrlFileWriter urlFileWriter = UrlFileWriterFactory.get();

    public GraphItemListService(final GraphSession session) {
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final OneDriveFolder folder = session.toFolder(directory);
        try {
            final Iterator<OneDriveItem.Metadata> iterator = folder.iterator(PreferencesFactory.get().getInteger("onedrive.listing.chunksize"));
            while(iterator.hasNext()) {
                final OneDriveItem.Metadata metadata;
                try {
                    metadata = iterator.next();
                }
                catch(OneDriveRuntimeException e) {
                    log.warn(e.getMessage());
                    continue;
                }
                final PathAttributes attr = attributes.toAttributes(metadata);

                final String fileName;
                if(metadata instanceof OneDrivePackageItem.Metadata) {
                    fileName = String.format("%s.%s", PathNormalizer.name(metadata.getName()), urlFileWriter.getExtension());
                }
                else {
                    fileName = metadata.getName();
                }

                children.add(new Path(directory, fileName, this.resolveType(metadata), attr));
                listener.chunk(directory, children);
            }
        }
        catch(OneDriveRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
            throw new GraphExceptionMappingService().map("Listing directory {0} failed", e.getCause(), directory);
        }
        return children;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        attributes.withCache(cache);
        return this;
    }

    private EnumSet<Path.Type> resolveType(final OneDriveItem.Metadata metadata) {
        if(metadata instanceof OneDrivePackageItem.Metadata) {
            return EnumSet.of(Path.Type.file, Path.Type.placeholder);
        }
        else if(metadata instanceof OneDriveRemoteItem.Metadata) {
            final EnumSet<Path.Type> types = this.resolveType(((OneDriveRemoteItem.Metadata) metadata).getRemoteItem());
            types.add(Path.Type.shared);
            return types;
        }
        else {
            return EnumSet.of(metadata.isFolder() ? Path.Type.directory : Path.Type.file);
        }
    }
}
