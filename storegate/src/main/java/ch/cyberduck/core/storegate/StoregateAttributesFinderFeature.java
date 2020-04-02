package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;

public class StoregateAttributesFinderFeature implements AttributesFinder {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateAttributesFinderFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            final FilesApi files = new FilesApi(session.getClient());
            return this.toAttributes(files.filesGet_1(URIEncoder.encode(fileid.getPrefixedPath(file))));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributes(final File f) {
        final PathAttributes attrs = new PathAttributes();
        if(0 != f.getModified().getMillis()) {
            attrs.setModificationDate(f.getModified().getMillis());
        }
        else {
            attrs.setModificationDate(f.getUploaded().getMillis());
        }
        if(0 != f.getCreated().getMillis()) {
            attrs.setCreationDate(f.getCreated().getMillis());
        }
        else {
            attrs.setCreationDate(f.getUploaded().getMillis());
        }
        attrs.setSize(f.getSize());
        if((f.getFlags() & File.FlagsEnum.Locked.getValue()) == File.FlagsEnum.Locked.getValue()) {
            attrs.setLockId(Boolean.TRUE.toString());
        }
        if((f.getFlags() & File.FlagsEnum.Hidden.getValue()) == File.FlagsEnum.Hidden.getValue()) {
            attrs.setHidden(true);
        }
        return attrs;
    }
}
