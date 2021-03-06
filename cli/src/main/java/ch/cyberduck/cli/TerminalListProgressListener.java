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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.date.AbstractUserDateFormatter;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

public class TerminalListProgressListener extends IndexedListProgressListener {

    private final Console console = new Console();

    private final AbstractUserDateFormatter formatter
        = UserDateFormatterFactory.get();

    private int size = 0;

    private boolean l;

    private TerminalPromptReader prompt;

    public TerminalListProgressListener() {
        this.prompt = new InteractiveTerminalPromptReader();
    }

    /**
     * @param l Long format
     */
    public TerminalListProgressListener(final boolean l) {
        this.l = l;
    }

    public TerminalListProgressListener(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    public TerminalListProgressListener(final TerminalPromptReader prompt, final boolean l) {
        this.l = l;
        this.prompt = prompt;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) {
        if(l) {
            if(file.isSymbolicLink()) {
                console.printf("%n%sl%s\t%s\t%s -> %s%s",
                    Ansi.ansi().bold(),
                    file.attributes().getPermission().getSymbol(),
                    formatter.getMediumFormat(
                        file.attributes().getModificationDate()),
                    file.getName(), file.getSymlinkTarget().getAbsolute(),
                    Ansi.ansi().reset());
            }
            else {
                console.printf("%n%s%s%s\t%s\t%s\t%s%s",
                    Ansi.ansi().bold(),
                    file.isDirectory() ? "d" : "-",
                    file.attributes().getPermission().getSymbol(),
                    formatter.getMediumFormat(
                        file.attributes().getModificationDate()),
                    StringUtils.isNotBlank(file.attributes().getRegion())
                        ? file.attributes().getRegion() : StringUtils.EMPTY,
                    file.getName(),
                    Ansi.ansi().reset());
            }
        }
        else {
            console.printf("%n%s%s%s", Ansi.ansi().bold(), file.getName(), Ansi.ansi().reset());
        }
    }

    @Override
    public void message(final String message) {
        //
    }
}
