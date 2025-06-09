package com.github.damienvdb.dcm4junit.dimse;

import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DimseMock {

    public static final int PORT = 4100;
    private final Device device;
    private final ApplicationEntity ae;
    @Delegate(excludes = {AssociationMonitor.class})
    private final FakeAssociationMonitor associationMonitor;
    private final MockStoreScp mockStoreScp;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    public DimseMock() {
        device = new Device("mockscp");
        mockStoreScp = MockStoreScp.builder().build();
        device.setDimseRQHandler(createServiceRegistry(Collections.singletonList(mockStoreScp)));
        Connection conn = new Connection();
        conn.setPort(PORT);
        device.addConnection(conn);
        associationMonitor = new FakeAssociationMonitor();
        device.setAssociationMonitor(associationMonitor);
        ae = new ApplicationEntity("storescp");
        configureTransferCapability(ae);
        device.addApplicationEntity(ae);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
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
        device.bindConnections();
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
        return "localhost";
    }

    /**
     * TODO: Configure
     */
    public int getPort() {
        return PORT;
    }

    public List<File> getStoredFiles() {
        return new ArrayList<>(mockStoreScp.getStoredFiles().values());
    }

    public List<Attributes> getFmis() {
        return new ArrayList<>(mockStoreScp.getStoredFiles().keySet());
    }
}
