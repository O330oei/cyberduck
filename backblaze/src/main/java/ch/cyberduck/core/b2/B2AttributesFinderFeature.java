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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_LARGE_FILE_SHA1;
import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2AttributesFinderFeature implements AttributesFinder {

    private final B2Session session;
    private final B2FileidProvider fileid;

    public B2AttributesFinderFeature(final B2Session session, final B2FileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(file.getType().contains(Path.Type.upload)) {
            // Pending large file upload
            return PathAttributes.EMPTY;
        }
        try {
            final B2FileResponse info = session.getClient().getFileInfo(fileid.getFileid(file, new DisabledListProgressListener()));
            return this.toAttributes(info);
        }
        catch(B2ApiException e) {
            if(StringUtils.equals("file_state_none", e.getMessage())) {
                // Pending large file upload
                return PathAttributes.EMPTY;
            }
            throw new B2ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    protected PathAttributes toAttributes(final B2FileResponse response) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(response.getContentLength());
        if(response.getFileInfo().containsKey(X_BZ_INFO_LARGE_FILE_SHA1)) {
            attributes.setChecksum(Checksum.parse(response.getFileInfo().get(X_BZ_INFO_LARGE_FILE_SHA1)));
        }
        else {
            attributes.setChecksum(Checksum.parse(StringUtils.removeStart(StringUtils.lowerCase(response.getContentSha1(), Locale.ROOT), "unverified:")));
        }
        if(!response.getFileInfo().isEmpty()) {
            final Map<String, String> metadata = new HashMap<>();
            for(Map.Entry<String, String> entry : response.getFileInfo().entrySet()) {
                metadata.put(entry.getKey(), entry.getValue());
            }
            attributes.setMetadata(metadata);
        }
        attributes.setVersionId(response.getFileId());
        if(response.getFileInfo().containsKey(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)) {
            attributes.setModificationDate(Long.parseLong(response.getFileInfo().get(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS)));
        }
        return attributes;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}

