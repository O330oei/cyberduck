package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.junit.Test;

import static org.junit.Assert.fail;

public class BackgroundActionPauserTest {

    @Test
    public void testAwait() {
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
                throw new BackgroundException(new RuntimeException("f"));
            }
        };
        try {
            action.call();
            fail();
        }
        catch(BackgroundException e) {
            //
        }
        new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public void validate() throws ConnectionCanceledException {
                if(action.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
            }

            @Override
            public void progress(final Integer delay) {
                //
            }
        }).await();
    }
}
