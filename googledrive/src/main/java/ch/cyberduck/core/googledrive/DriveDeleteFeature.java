package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Map;

import com.google.api.services.drive.model.File;

public class DriveDeleteFeature implements Delete {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveDeleteFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            if(file.getType().contains(Path.Type.placeholder)) {
                continue;
            }
            callback.delete(file);
            try {
                if(DriveHomeFinderService.TEAM_DRIVES_NAME.equals(file.getParent())) {
                    session.getClient().teamdrives().delete(fileid.getFileid(file, new DisabledListProgressListener())).execute();
                }
                else {
                    if(PreferencesFactory.get().getBoolean("googledrive.delete.trash")) {
                        final File properties = new File();
                        properties.setTrashed(true);
                        session.getClient().files().update(fileid.getFileid(file, new DisabledListProgressListener()), properties)
                            .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
                    }
                    else {
                        session.getClient().files().delete(fileid.getFileid(file, new DisabledListProgressListener()))
                            .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
                    }
                }
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
