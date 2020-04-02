package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.notification.NotificationService;

public class TerminalNotification implements NotificationService {

    private final TerminalProgressListener console
        = new TerminalProgressListener();

    @Override
    public NotificationService setup() {
        return this;
    }

    @Override
    public void unregister() {
        //
    }

    @Override
    public void addListener(final Listener listener) {
        //
    }

    @Override
    public void notify(final String identifier, final String group, final String title, final String description) {
        console.message(String.format("%s. %s", title, description));
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description, final String action) {
        console.message(String.format("%s. %s", title, description));
    }
}
