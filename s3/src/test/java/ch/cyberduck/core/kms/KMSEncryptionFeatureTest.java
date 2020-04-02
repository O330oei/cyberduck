package ch.cyberduck.core.kms;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class KMSEncryptionFeatureTest extends AbstractS3Test {

    @Test
    public void testSetEncryptionKMSDefaultKeySignatureVersionV4() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session);
        feature.setEncryption(test, KMSEncryptionFeature.SSE_KMS_DEFAULT);
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("aws:kms", value.algorithm);
        assertNotNull(value.key);
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSetEncryptionKMSCustomKeySignatureVersionV4() throws Exception {
        final Path container = new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session);
        feature.setEncryption(test, new Encryption.Algorithm("aws:kms", "arn:aws:kms:eu-west-1:930717317329:key/015fa0af-f95e-483e-8fb6-abffb46fb783"));
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("aws:kms", value.algorithm);
        assertEquals("arn:aws:kms:eu-west-1:930717317329:key/015fa0af-f95e-483e-8fb6-abffb46fb783", value.key);
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testGetKeys_eu_west_1() throws Exception {
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(session, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertFalse(kms.getKeys(new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume)), new DisabledLoginCallback()).isEmpty());
        session.close();
    }

    @Test
    public void testGetKeys_ap_southeast_2() throws Exception {
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(session, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Set<Encryption.Algorithm> keys = kms.getKeys(new Path("test-ap-southeast-2-cyberduck", EnumSet.of(Path.Type.volume)), new DisabledLoginCallback());
        assertTrue(keys.contains(Encryption.Algorithm.NONE));
        assertTrue(keys.contains(S3EncryptionFeature.SSE_AES256));
        assertEquals(2, keys.size());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testCreateUserAuthenticationFailure() throws Exception {
        final S3Session session = new S3Session(
            new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                    "key", "secret"
                )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        new KMSEncryptionFeature(session, new DisabledX509TrustManager(), new DefaultX509KeyManager()).getKeys(new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume)), new DisabledLoginCallback());
        session.close();
    }
}
