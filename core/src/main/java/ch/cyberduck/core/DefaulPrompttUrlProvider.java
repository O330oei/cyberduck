package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.PromptUrlProvider;

public class DefaulPrompttUrlProvider implements PromptUrlProvider {

    private final UrlProvider proxy;

    public DefaulPrompttUrlProvider(final UrlProvider proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(Type.download == type) {
            return !DescriptiveUrl.EMPTY.equals(proxy.toUrl(file).find(DescriptiveUrl.Type.signed));
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Object options, final PasswordCallback callback) {
        return proxy.toUrl(file).find(DescriptiveUrl.Type.signed);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }
}
