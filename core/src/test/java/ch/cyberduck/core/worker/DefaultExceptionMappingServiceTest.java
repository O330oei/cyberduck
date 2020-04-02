package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultExceptionMappingServiceTest {

    @Test
    public void testMap() {
        assertEquals("Error", new DefaultExceptionMappingService().map(new NullPointerException()).getMessage());
        assertEquals("Unknown application error.", new DefaultExceptionMappingService().map(new NullPointerException()).getDetail());
        assertEquals("Unknown application error. R.", new DefaultExceptionMappingService().map(new NullPointerException("r")).getDetail());
    }
}
