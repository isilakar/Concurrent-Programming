package org.cp.jamaica.server;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.apache.log4j.Logger;
import org.cp.jamaica.client.Client;
import org.cp.monitor.resources.Resource;
import org.cp.monitor.resources.ResourceA;
import org.cp.monitor.resources.ResourceB;

import java.util.Map;
import java.util.Queue;
import java.util.Random;

/**
 * @author kornicameister
 * @version 0.0.1
 * @since 0.0.1
 */
public class RServer
        implements Runnable {

    public static final  String SERVICE_A  = "a-service";
    public static final  String SERVICE_B  = "b-service";
    public static final  String SERVICE_AB = "ab-service";
    private static final Logger LOGGER     = Logger.getLogger(RServer.class);
    private Map<String, Queue<Client>>   queueMap;
    private Map<String, Queue<Resource>> resources;
    private String                       currentlyInvestigates;

    public RServer() {
        this.queueMap = Maps.newConcurrentMap();

        this.queueMap.put(SERVICE_A, Queues.<Client>newConcurrentLinkedQueue());
        this.queueMap.put(SERVICE_B, Queues.<Client>newConcurrentLinkedQueue());
        this.queueMap.put(SERVICE_AB, Queues.<Client>newConcurrentLinkedQueue());

        this.resources = Maps.newConcurrentMap();
        this.resources.put(SERVICE_A, Queues.<Resource>newArrayDeque());
        this.resources.put(SERVICE_B, Queues.<Resource>newArrayDeque());

        for (int i = 0 ; i < 20 ; i++) {
            this.resources.get(SERVICE_A).add(new ResourceA(i));
            this.resources.get(SERVICE_B).add(new ResourceB(i + 20));
        }
    }

    private void getNextToInvestigate() {
        final int nextInt = new Random().nextInt(3);
        this.currentlyInvestigates = nextInt == 0 ? SERVICE_A : nextInt == 1 ? SERVICE_B : SERVICE_AB;
    }

    @Override
    public void run() {
        while (true) {
            if (this.currentlyInvestigates == null) {
                this.getNextToInvestigate();
            }
            synchronized (this) {
                try {
                    final Queue<Client> clients = this.queueMap.get(this.currentlyInvestigates);
                    if (clients.size() == 0) {
                        this.getNextToInvestigate();
                        Thread.sleep(100);
                    } else {
                        final Client targetOfRendezvous = clients.poll();
                        if (targetOfRendezvous != null) {
                            switch (this.currentlyInvestigates) {
                                case SERVICE_A:
                                case SERVICE_B:
                                    final Resource resource = this.resources.get(this.currentlyInvestigates).poll();
                                    if (resource != null) {
                                        targetOfRendezvous.msg(resource);
                                    }
                                    break;
                                case SERVICE_AB: {
                                    final Resource resourceA = this.resources.get(SERVICE_A).poll();
                                    final Resource resourceB = this.resources.get(SERVICE_B).poll();
                                    if (resourceA != null && resourceB != null) {
                                        targetOfRendezvous.msg(resourceA, resourceB);
                                    }
                                }
                                break;
                            }
                            this.getNextToInvestigate();
                            Thread.sleep(100);
                        } else {
                            this.getNextToInvestigate();
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception exception) {
                    LOGGER.warn(exception.getMessage());
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .addValue(queueMap)
                      .toString();
    }

    public synchronized boolean call(final String receiverCode, final Client c) {
        if (receiverCode == null) {
            return false;
        }
        final Queue<Client> clients = this.queueMap.get(receiverCode);
        if (clients.contains(c)) {
            LOGGER.warn(String.format("Client %s not accepted already waiting", c));
            return false;
        } else {
            final Resource[] cResources = c.getResources();
            final String cReceiverCode = c.getReceiverCode();
            if (cResources != null) {
                for (final Resource resource : cResources) {
                    switch (cReceiverCode) {
                        case SERVICE_A:
                            this.resources.get(SERVICE_A).add(resource);
                            break;
                        case SERVICE_B:
                            this.resources.get(SERVICE_B).add(resource);
                            break;
                        case SERVICE_AB:
                            if (resource instanceof ResourceA) {
                                this.resources.get(SERVICE_A).add(resource);
                            } else {
                                this.resources.get(SERVICE_B).add(resource);
                            }
                            break;
                    }
                }
            }
        }
        LOGGER.debug(String.format("Client %s accepted and waits...", c));
        return clients != null && clients.add(c);
    }
}
