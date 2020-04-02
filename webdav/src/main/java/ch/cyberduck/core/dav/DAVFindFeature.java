package ch.cyberduck.core.dav;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.shared.DefaultFindFeature;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.ExistsResponseHandler;

public class DAVFindFeature implements Find {

    private final DAVSession session;

    private Cache<Path> cache = PathCache.empty();

    public DAVFindFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            try {
                try {
                    final HttpHead request = new HttpHead(new DAVPathEncoder().encode(file));
                    for(Header header : this.headers()) {
                        request.addHeader(header);
                    }
                    return session.getClient().execute(request, new ExistsResponseHandler());
                }
                catch(SardineException e) {
                    throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                }
                catch(IOException e) {
                    throw new HttpExceptionMappingService().map(e, file);
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                // 400 Multiple choices
                return new DefaultFindFeature(session).withCache(cache).find(file);
            }
        }
        catch(AccessDeniedException e) {
            // Parent directory may not be accessible. Issue #5662
            return true;
        }
        catch(LoginFailureException | NotfoundException e) {
            return false;
        }
    }

    public Set<Header> headers() {
        return Collections.emptySet();
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
