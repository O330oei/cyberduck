package ch.cyberduck.core.features;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

public interface Directory<Reply> {

    /**
     * @param folder Directory
     * @param region Location
     * @param status Transfer status
     */
    Path mkdir(Path folder, String region, TransferStatus status) throws BackgroundException;

    /**
     * @param workdir Working directory in browser
     * @param name    Folder name or null if unknown
     * @return True if creating directory will succeed
     */
    default boolean isSupported(final Path workdir, final String name) {
        return workdir.attributes().getPermission().isWritable();
    }

    Directory<Reply> withWriter(Write<Reply> writer);
}
