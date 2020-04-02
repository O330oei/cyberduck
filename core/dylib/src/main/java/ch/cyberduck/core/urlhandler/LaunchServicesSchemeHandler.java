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

import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for the handler functions in ApplicationServices.h
 */
public final class LaunchServicesSchemeHandler extends AbstractSchemeHandler {

    static {
        Native.load("core");
    }

    private ApplicationFinder applicationFinder;

    public LaunchServicesSchemeHandler() {
        this(ApplicationFinderFactory.get());
    }

    public LaunchServicesSchemeHandler(final ApplicationFinder applicationFinder) {
        this.applicationFinder = applicationFinder;
    }

    @Override
    public void setDefaultHandler(final Application application, final List<String> schemes) {
        for(String scheme : schemes) {
            this.setDefaultHandlerForURLScheme(scheme, application.getIdentifier());
        }
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSSetDefaultHandlerForURLScheme
     * Register this bundle identifier as the default application for all schemes
     *
     * @param bundleIdentifier The bundle identifier of the application
     * @param scheme           The protocol identifier
     */
    private native void setDefaultHandlerForURLScheme(String scheme, String bundleIdentifier);

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyDefaultHandlerForURLScheme
     *
     * @param scheme The protocol identifier
     * @return The bundle identifier for the application registered as the default handler for this scheme
     */
    @Override
    public Application getDefaultHandler(final String scheme) {
        final Application application = applicationFinder.getDescription(this.getDefaultHandlerForURLScheme(scheme));
        if(applicationFinder.isInstalled(application)) {
            return application;
        }
        return Application.notfound;
    }

    private native String getDefaultHandlerForURLScheme(String scheme);

    @Override
    public List<Application> getAllHandlers(final String scheme) {
        final List<Application> handlers = new ArrayList<Application>();
        for(String bundleIdentifier : this.getAllHandlersForURLScheme(scheme)) {
            final Application application = applicationFinder.getDescription(bundleIdentifier);
            if(applicationFinder.isInstalled(application)) {
                handlers.add(application);
            }
        }
        return handlers;
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyAllHandlersForURLScheme
     *
     * @param scheme The protocol identifier
     * @return The bundle identifiers for all applications that promise to be capable of handling this scheme
     */
    private native String[] getAllHandlersForURLScheme(String scheme);
}
