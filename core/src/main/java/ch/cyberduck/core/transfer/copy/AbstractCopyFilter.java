package ch.cyberduck.core.transfer.copy;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Map;

public abstract class AbstractCopyFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractCopyFilter.class);

    protected final Session<?> sourceSession;

    protected final Session<?> destinationSession;

    protected Cache<Path> sourceCache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    protected final Cache<Path> destinationCache
            = new PathCache(PreferencesFactory.get().getInteger("transfer.cache.size"));

    protected final Map<Path, Path> files;

    private final UploadFilterOptions options;

    public AbstractCopyFilter(final Session<?> source, final Session<?> destination, final Map<Path, Path> files) {
        this(source, destination, files, new UploadFilterOptions());
    }

    public AbstractCopyFilter(final Session<?> source, final Session<?> destination,
                              final Map<Path, Path> files, final UploadFilterOptions options) {
        this.sourceSession = source;
        this.destinationSession = destination;
        this.files = files;
        this.options = options;
    }

    @Override
    public TransferPathFilter withCache(final Cache<Path> cache) {
        // With cache from source host
        this.sourceCache = cache;
        return this;
    }

    @Override
    public TransferStatus prepare(final Path source, final Local n, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        // Read remote attributes from source
        final PathAttributes attributes = sourceSession.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(sourceSession)).withCache(sourceCache).find(source);
        if(source.isFile()) {
            // Content length
            status.setLength(attributes.getSize());
        }
        status.setRemote(attributes);
        if(options.permissions) {
            status.setPermission(attributes.getPermission());
        }
        if(options.timestamp) {
            status.setTimestamp(attributes.getModificationDate());
        }
        if(options.acl) {
            final AclPermission feature = sourceSession.getFeature(AclPermission.class);
            if(feature != null) {
                try {
                    status.setAcl(feature.getPermission(source));
                }
                catch(AccessDeniedException | InteroperabilityException e) {
                    // Ignore
                }
            }
        }
        // Save checksum and pass to transfer status when copying from file
        status.setChecksum(source.attributes().getChecksum());
        if(parent.isExists()) {
            // Do not attempt to create a directory that already exists
            final Path target = files.get(source);
            // Look for file in target host
            if(destinationSession.getFeature(Find.class, new DefaultFindFeature(destinationSession)).withCache(destinationCache).find(target)) {
                status.setExists(true);
            }
        }
        return status;
    }

    @Override
    public void apply(final Path source, final Local n, final TransferStatus status, final ProgressListener listener) {
        //
    }

    @Override
    public void complete(final Path source, final Local n, final TransferOptions options, final TransferStatus status, final ProgressListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", source.getAbsolute(), status));
        }
        if(status.isComplete()) {
            final Path target = files.get(source);
            if(!Permission.EMPTY.equals(status.getPermission())) {
                final UnixPermission feature = destinationSession.getFeature(UnixPermission.class);
                if(feature != null) {
                    if(!Permission.EMPTY.equals(status.getPermission())) {
                        try {
                            listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                                target.getName(), status.getPermission()));
                            feature.setUnixPermission(target, status.getPermission());
                        }
                        catch(BackgroundException e) {
                            // Ignore
                            log.warn(e.getMessage());
                        }
                    }
                }
            }
            if(!Acl.EMPTY.equals(status.getAcl())) {
                final AclPermission feature = destinationSession.getFeature(AclPermission.class);
                if(feature != null) {
                    try {
                        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                            target.getName(), status.getAcl()));
                        feature.setPermission(target, status.getAcl());
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
            if(status.getTimestamp() != null) {
                final Timestamp timestamp = destinationSession.getFeature(Timestamp.class);
                if(timestamp != null) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing timestamp of {0} to {1}", "Status"),
                        target.getName(), UserDateFormatterFactory.get().getShortFormat(status.getTimestamp())));
                    try {
                        timestamp.setTimestamp(target, status);
                    }
                    catch(BackgroundException e) {
                        // Ignore
                        log.warn(e.getMessage());
                    }
                }
            }
        }
    }
}
