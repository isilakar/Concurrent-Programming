package org.cp.jamaica.client.impl;

import org.cp.jamaica.client.Client;
import org.cp.jamaica.server.RServer;

/**
 * @author kornicameister
 * @version 0.0.1
 * @since 0.0.1
 */
public class ClientAB
        extends AbstractClient
        implements Client {

    public ClientAB(final RServer server) {
        super(server);
        this.setReceiverCode(RServer.SERVICE_AB);
    }

}
