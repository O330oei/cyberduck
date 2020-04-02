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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftTouchFeature implements Touch<StorageObject> {

    final PathContainerService containerService
        = new PathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    private Write<StorageObject> writer;

    public SwiftTouchFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
        this.writer = new SwiftWriteFeature(session, regionService);
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setLength(0L);
        final StatusOutputStream<StorageObject> out = writer.write(file, status, new DisabledConnectionCallback());
        new DefaultStreamCloser().close(out);
        final StorageObject metadata = out.getStatus();
        return new Path(file.getParent(), file.getName(), file.getType(),
            new SwiftAttributesFinderFeature(session, regionService).toAttributes(metadata));
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }

    @Override
    public Touch<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
