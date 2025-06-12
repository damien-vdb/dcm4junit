package com.github.damienvdb.dcm4junit.dimse;

import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DimseMock implements Closeable {

    public static final String DEFAULT_AET = "storescp";
    private static final int DYNAMIC_START_PORT = 4200;
    private static final int DYNAMIC_PORT_RANGE = 1000;
    private static final String DEFAULT_HOSTNAME = "localhost";
    private final Connection connection;
    private final Device device;
    private final ApplicationEntity ae;
    @Delegate(excludes = {AssociationMonitor.class})
    private final FakeAssociationMonitor associationMonitor;
    private final MockStoreScp mockStoreScp;
    @Getter
    private int port;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    public DimseMock(Optional<DimseMockSettings> settings) {
        device = new Device("mockscp");
        mockStoreScp = MockStoreScp.builder().build();
        device.setDimseRQHandler(createServiceRegistry(Collections.singletonList(mockStoreScp)));
        port = settings.map(DimseMockSettings::port).orElse(0);

        connection = new Connection();
        connection.setHostname(settings.map(DimseMockSettings::hostname).orElse(DEFAULT_HOSTNAME));
        device.addConnection(connection);
        associationMonitor = new FakeAssociationMonitor();
        device.setAssociationMonitor(associationMonitor);
        ae = new ApplicationEntity(settings.map(DimseMockSettings::aet).orElse(DEFAULT_AET));
        configureTransferCapability(ae);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(connection);
    }

    /**
     * TODO: Add transfer capability settings
     */
    private static void configureTransferCapability(ApplicationEntity ae) {
        ae.addTransferCapability(
                new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));
    }

    private DicomServiceRegistry createServiceRegistry(List<DicomService> services) {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        services.forEach(serviceRegistry::addDicomService);
        return serviceRegistry;
    }

    @SneakyThrows
    public void start() {
        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);
        bindConnection();
    }

    private void bindConnection() throws IOException, GeneralSecurityException {
        if (port > 0) {
            bindConnection(port);
        } else {
            for (int i = DYNAMIC_START_PORT; i < DYNAMIC_START_PORT + DYNAMIC_PORT_RANGE; i++) {
                try {
                    bindConnection(i);
                    this.port = i;
                    return;
                } catch (IOException e) {
                    // Nothing
                }
            }
        }
    }

    private void bindConnection(int givenPort) throws IOException, GeneralSecurityException {
        connection.setPort(givenPort);
        connection.bind();
    }

    @Override
    public void close() {
        this.stop();
    }

    public void stop() {
        device.unbindConnections();
        executorService.shutdown();
        scheduledExecutorService.shutdown();
    }

    public void reset() {
        associationMonitor.clear();
        mockStoreScp.clear();
    }

    public String getAeTitle() {
        return ae.getAETitle();
    }

    /**
     * TODO: Configure
     */
    public String getHost() {
        return DEFAULT_HOSTNAME;
    }

    public List<File> getStoredFiles() {
        return new ArrayList<>(mockStoreScp.getStoredFiles().values());
    }

    public List<Attributes> getFmis() {
        return new ArrayList<>(mockStoreScp.getStoredFiles().keySet());
    }

}
