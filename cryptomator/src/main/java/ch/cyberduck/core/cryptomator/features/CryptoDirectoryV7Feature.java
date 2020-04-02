package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.ContentWriter;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV7Provider;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.cryptomator.cryptolib.api.FileHeader;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class CryptoDirectoryV7Feature<Reply> implements Directory<Reply> {
    private static final Logger log = Logger.getLogger(CryptoDirectoryV7Feature.class);

    private final Session<?> session;
    private final Directory<Reply> proxy;
    private final CryptoVault vault;
    private final RandomStringService random
        = new UUIDRandomStringService();

    public CryptoDirectoryV7Feature(final Session<?> session, final Directory<Reply> delegate, final Write<Reply> writer, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate.withWriter(new CryptoWriteFeature<Reply>(session, writer, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        final String directoryId = random.random();
        final Path encrypt = vault.encrypt(session, folder, directoryId, false);
        // Create metadata file for directory
        final Path metadataFolder = vault.encrypt(session, folder, true);
        final Path metadataPath = new Path(session._getFeature(Directory.class).mkdir(metadataFolder, region, new TransferStatus()),
            CryptoDirectoryV7Provider.DIRECTORY_METADATAFILE,
            EnumSet.of(Path.Type.file));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write metadata %s for folder %s", metadataPath, folder));
        }
        new ContentWriter(session).write(metadataPath, directoryId.getBytes(StandardCharsets.UTF_8));
        final Path intermediate = encrypt.getParent();
        if(!session._getFeature(Find.class).find(intermediate)) {
            session._getFeature(Directory.class).mkdir(intermediate, region, new TransferStatus());
        }
        // Write header
        final FileHeader header = vault.getFileHeaderCryptor().create();
        status.setHeader(vault.getFileHeaderCryptor().encryptHeader(header));
        status.setNonces(new RandomNonceGenerator());
        final Path target = proxy.mkdir(encrypt, region, status);
        // Implementation may return new copy of attributes without encryption attributes
        target.attributes().setDirectoryId(directoryId);
        target.attributes().setDecrypted(encrypt.attributes().getDecrypted());
        // Make reference of encrypted path in attributes of decrypted file point to metadata file
        final Path decrypt = vault.decrypt(session, vault.encrypt(session, target, true));
        decrypt.attributes().setVersionId(target.attributes().getVersionId());
        return decrypt;
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return proxy.isSupported(workdir, name);
    }

    @Override
    public CryptoDirectoryV7Feature<Reply> withWriter(final Write<Reply> writer) {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDirectoryFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
