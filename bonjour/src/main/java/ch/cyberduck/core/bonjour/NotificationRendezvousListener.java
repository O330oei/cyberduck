package ch.cyberduck.core.bonjour;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;

public class NotificationRendezvousListener implements RendezvousListener {

    private final Rendezvous bonjour;

    private final NotificationService notification = NotificationServiceFactory.get();

    public NotificationRendezvousListener(final Rendezvous bonjour) {
        this.bonjour = bonjour;
    }

    @Override
    public void serviceResolved(final String identifier, final Host host) {
        notification.notify(BookmarkNameProvider.toString(host), host.getUuid(), "Bonjour", bonjour.getDisplayedName(identifier));
    }

    @Override
    public void serviceLost(final Host servicename) {
        //
    }
}
