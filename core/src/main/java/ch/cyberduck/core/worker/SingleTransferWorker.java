package ch.cyberduck.core.worker;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import java.util.concurrent.Future;

public class SingleTransferWorker extends AbstractTransferWorker {

    private final Session source;
    private final Session destination;

    public SingleTransferWorker(final Session source, final Session destination, final Transfer transfer, final TransferOptions options,
                                final TransferSpeedometer meter, final TransferPrompt prompt,
                                final TransferErrorCallback error,
                                final ProgressListener listener, final StreamListener streamListener,
                                final ConnectionCallback connect, final NotificationService notification) {
        super(transfer, options, prompt, meter, error, listener, streamListener, connect, notification);
        this.source = source;
        this.destination = destination;
    }

    @Override
    public Session borrow(final Connection type) {
        switch(type) {
            case source:
                return source;
            case destination:
                return destination;
        }
        return null;
    }

    @Override
    protected void release(final Session session, final Connection type, final BackgroundException failure) {
        //
    }

    public Future<TransferStatus> submit(final TransferCallable runnable) throws BackgroundException {
        return ConcurrentUtils.constantFuture(runnable.call());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SingleTransferWorker{");
        sb.append("source=").append(source);
        sb.append(", destination=").append(destination);
        sb.append('}');
        return sb.toString();
    }
}
