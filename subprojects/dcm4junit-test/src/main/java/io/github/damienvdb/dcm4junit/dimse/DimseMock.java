package io.github.damienvdb.dcm4junit.dimse;

import io.github.damienvdb.dcm4junit.dimse.cfind.MockFindScp;
import io.github.damienvdb.dcm4junit.dimse.jupiter.CFindScp;
import io.github.damienvdb.dcm4junit.dimse.jupiter.CStoreScp;
import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomServiceRegistry;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
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
    @Getter
    private final String hostname;
    private final FakeAssociationMonitor associationMonitor;
    private final Optional<MockStoreScp> mockStoreScp;
    private final Optional<MockFindScp> mockFindScp;
    @Getter
    private int port;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    public DimseMock(Optional<DimseMockSettings> settings) {
        port = settings.map(DimseMockSettings::port).orElse(0);
        hostname = settings.map(DimseMockSettings::hostname).orElse(DEFAULT_HOSTNAME);
        String aet = settings.map(DimseMockSettings::aet).orElse(DEFAULT_AET);

        mockStoreScp = settings.map(DimseMockSettings::cstoreScp)
                .filter(CStoreScp::enabled)
                .map(cstoreScp -> MockStoreScp.builder().build());

        mockFindScp = settings.map(DimseMockSettings::cfindScp)
                .filter(CFindScp::enabled)
                .map(MockFindScp::new);

        device = new Device("mockscp");
        connection = new Connection();
        connection.setHostname(hostname);
        device.addConnection(connection);
        associationMonitor = new FakeAssociationMonitor();
        device.setDimseRQHandler(createServiceRegistry());
        device.setAssociationMonitor(associationMonitor);
        ae = new ApplicationEntity(aet);
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

    private DicomServiceRegistry createServiceRegistry() {
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        mockStoreScp.ifPresent(serviceRegistry::addDicomService);
        mockFindScp.ifPresent(serviceRegistry::addDicomService);
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

    public boolean isStarted() {
        return connection.isListening();
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
        mockStoreScp.ifPresent(MockStoreScp::clear);
        mockFindScp.ifPresent(MockFindScp::clear);
    }

    public List<Association> getAssociationsEstablished() {
        return this.associationMonitor.getAssociationsEstablished();
    }


    public List<Association> getAssociationsFailed() {
        return this.associationMonitor.getAssociationsFailed();
    }


    public List<Association> getAssociationsRejected() {
        return this.associationMonitor.getAssociationsRejected();
    }

    public List<Association> getAssociationsAccepted() {
        return this.associationMonitor.getAssociationsAccepted();
    }

    public String getAeTitle() {
        return ae.getAETitle();
    }

    public MockStoreScp getCStoreScp() {
        return mockStoreScp.orElseThrow(() -> new IllegalStateException("Mock C-STORE SCP is not enabled"));
    }

    public MockFindScp getCFindScp() {
        return mockFindScp.orElseThrow(() -> new IllegalStateException("Mock C-FIND SCP is not enabled"));
    }
}
