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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.TeamDrive;

public class DriveDirectoryFeature implements Directory<VersionId> {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveDirectoryFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(DriveHomeFinderService.TEAM_DRIVES_NAME.equals(folder.getParent())) {
                final TeamDrive execute = session.getClient().teamdrives().create(
                    new UUIDRandomStringService().random(), new TeamDrive().setName(folder.getName())
                ).execute();
                return new Path(folder.getParent(), folder.getName(), folder.getType(),
                    new PathAttributes(folder.attributes()).withVersionId(execute.getId()));
            }
            else {
                // Identified by the special folder MIME type application/vnd.google-apps.folder
                final Drive.Files.Create insert = session.getClient().files().create(new File()
                    .setName(folder.getName())
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(Collections.singletonList(fileid.getFileid(folder.getParent(), new DisabledListProgressListener()))));
                final File execute = insert
                    .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
                return new Path(folder.getParent(), folder.getName(), folder.getType(),
                    new DriveAttributesFinderFeature(session, fileid).toAttributes(execute));
            }
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public DriveDirectoryFeature withWriter(final Write<VersionId> writer) {
        return this;
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return new DriveTouchFeature(session, fileid).isSupported(workdir, name);
    }
}
