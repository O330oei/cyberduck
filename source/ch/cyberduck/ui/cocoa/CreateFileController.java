package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @version $Id$
 */
public class CreateFileController extends FileController {

    public CreateFileController(final WindowController parent) {
        super(parent);
    }

    @Override
    public void setIconView(NSImageView iconView) {
        iconView.setImage(IconCache.documentIcon(null, 128));
        super.setIconView(iconView);
    }

    @Override
    protected String getBundleName() {
        return "File";
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), false);
        }
        if(returncode == OTHER_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), true);
        }
    }

    protected void createFile(final Path workdir, final String filename, final boolean edit) {
        final BrowserController c = (BrowserController) parent;
        c.background(new BrowserBackgroundAction(c) {
            final Path file = PathFactory.createPath(this.getSession(), workdir,
                    LocalFactory.createLocal(Preferences.instance().getProperty("tmp.dir"), filename));

            public void run() {
                file.touch();
                if(file.exists()) {
                    if(edit) {
                        Editor editor = EditorFactory.createEditor(c, file);
                        editor.open();
                    }
                }
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        file.getName());
            }

            @Override
            public void cleanup() {
                if(filename.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reloadData(Collections.singletonList(file));
            }
        });
    }
}