package ch.cyberduck.core.urlhandler;

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

import ch.cyberduck.core.local.Application;

import java.util.List;

public abstract class AbstractSchemeHandler implements SchemeHandler {

    /**
     * @param schemes The protocol identifier
     * @return True if this application is the default handler for all schemes
     */
    @Override
    public boolean isDefaultHandler(final List<String> schemes, final Application application) {
        boolean isDefault = true;
        for(String s : schemes) {
            if(!application.equals(this.getDefaultHandler(s))) {
                isDefault = false;
                break;
            }
        }
        return isDefault;
    }
}
