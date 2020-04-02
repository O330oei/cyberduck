package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;

public abstract class DefaultHostPasswordStore implements HostPasswordStore {
    private static final Logger log = Logger.getLogger(DefaultHostPasswordStore.class);

    private final Preferences preferences = PreferencesFactory.get();

    /**
     * Find password for login
     *
     * @param bookmark Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    @Override
    public String findLoginPassword(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching login password from keychain for %s", bookmark));
        }
        final String password = this.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
            bookmark.getHostname(), credentials.getUsername());
        if(null == password) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", bookmark));
            }
        }
        return password;
    }

    @Override
    public String findLoginToken(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching login token from keychain for %s", bookmark));
        }
        // Find token named like "Shared Access Signature (SAS) Token"
        final String token = this.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
            bookmark.getHostname(), bookmark.getProtocol().getTokenPlaceholder());
        if(null == token) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Token not found in keychain for %s", bookmark));
            }
        }
        return token;
    }

    /**
     * Find passphrase for private key
     *
     * @param bookmark Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    @Override
    public String findPrivateKeyPassphrase(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching private key passphrase from keychain for %s", bookmark));
        }
        if(credentials.isPublicKeyAuthentication()) {
            final Local key = credentials.getIdentity();
            String passphrase = this.getPassword(bookmark.getHostname(), key.getAbbreviatedPath());
            if(null == passphrase) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                passphrase = this.getPassword("SSH", key.getAbsolute());
            }
            if(null == passphrase) {
                // Backward compatibility
                passphrase = this.getPassword("SSHKeychain", key.getAbbreviatedPath());
            }
            if(null == passphrase) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Passphrase not found in keychain for %s", key));
                }
            }
            return passphrase;
        }
        else {
            return null;
        }
    }

    @Override
    public OAuthTokens findOAuthTokens(final Host bookmark) {
        final long expiry = preferences.getLong(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()));
        final String prefix = this.getOAuthPrefix(bookmark);
        final String hostname = getOAuthHostname(bookmark);
        return new OAuthTokens(this.getPassword(bookmark.getProtocol().getScheme(),
            bookmark.getPort(), hostname,
            String.format("%s OAuth2 Access Token", prefix)),
            this.getPassword(bookmark.getProtocol().getScheme(),
                bookmark.getPort(), hostname,
                String.format("%s OAuth2 Refresh Token", prefix)),
            expiry);
    }

    protected String getOAuthHostname(final Host bookmark) {
        if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
            return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost();
        }
        return bookmark.getHostname();
    }

    private String getOAuthPrefix(final Host bookmark) {
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            return String.format("%s (%s)", bookmark.getProtocol().getDescription(), bookmark.getCredentials().getUsername());
        }
        return bookmark.getProtocol().getDescription();
    }

    /**
     * Adds the password to the login keychain
     *
     * @param bookmark Hostname
     * @see ch.cyberduck.core.Host#getCredentials()
     */
    @Override
    public void save(final Host bookmark) throws LocalAccessDeniedException {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(log.isInfoEnabled()) {
            log.info(String.format("Add password for bookmark %s", bookmark));
        }
        if(credentials.isPublicKeyAuthentication()) {
            this.addPassword(bookmark.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                credentials.getIdentityPassphrase());
        }
        if(credentials.isPasswordAuthentication()) {
            if(StringUtils.isEmpty(credentials.getUsername())) {
                log.warn(String.format("No username in credentials for bookmark %s", bookmark.getHostname()));
                return;
            }
            if(StringUtils.isEmpty(credentials.getPassword())) {
                log.warn(String.format("No password in credentials for bookmark %s", bookmark.getHostname()));
                return;
            }
            this.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                bookmark.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
        if(credentials.isTokenAuthentication()) {
            this.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                bookmark.getHostname(), bookmark.getProtocol().getTokenPlaceholder(), credentials.getToken());
        }
        if(credentials.isOAuthAuthentication()) {
            final String prefix = this.getOAuthPrefix(bookmark);
            if(StringUtils.isNotBlank(credentials.getOauth().getAccessToken())) {
                this.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), this.getOAuthHostname(bookmark),
                    String.format("%s OAuth2 Access Token", prefix), credentials.getOauth().getAccessToken());
            }
            if(StringUtils.isNotBlank(credentials.getOauth().getRefreshToken())) {
                this.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), this.getOAuthHostname(bookmark),
                    String.format("%s OAuth2 Refresh Token", prefix), credentials.getOauth().getRefreshToken());
            }
            // Save expiry
            if(credentials.getOauth().getExpiryInMilliseconds() != null) {
                preferences.setProperty(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()), credentials.getOauth().getExpiryInMilliseconds());
            }
        }
    }
}
