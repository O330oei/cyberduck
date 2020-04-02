package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.util.EnumSet;

public class S3DirectoryFeature implements Directory<StorageObject> {

    private static final String MIMETYPE = "application/x-directory";

    private final S3Session session;

    private final PathContainerService containerService
        = new S3PathContainerService();

    private Write<StorageObject> writer;

    public S3DirectoryFeature(final S3Session session, final Write<StorageObject> writer) {
        this.session = session;
        this.writer = writer;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(folder)) {
            final S3BucketCreateService service = new S3BucketCreateService(session);
            service.create(folder, StringUtils.isBlank(region) ? PreferencesFactory.get().getProperty("s3.location") : region);
            return folder;
        }
        else {
            status.setChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status));
            // Add placeholder object
            status.setMime(MIMETYPE);
            final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
            type.add(Path.Type.placeholder);
            final StatusOutputStream<StorageObject> out = writer.write(new Path(folder.getParent(), folder.getName(), type,
                new PathAttributes(folder.attributes())), status, new DisabledConnectionCallback());
            new DefaultStreamCloser().close(out);
            final StorageObject metadata = out.getStatus();
            return new Path(folder.getParent(), folder.getName(), type,
                new S3AttributesFinderFeature(session).toAttributes(metadata));
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        if(workdir.isRoot()) {
            if(StringUtils.isNotBlank(name)) {
                return ServiceUtils.isBucketNameValidDNSName(name);
            }
        }
        return true;
    }

    @Override
    public S3DirectoryFeature withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
