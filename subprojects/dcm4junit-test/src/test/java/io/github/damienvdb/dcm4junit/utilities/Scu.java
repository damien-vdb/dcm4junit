package io.github.damienvdb.dcm4junit.utilities;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.dcm4che3.data.UID.ImplicitVRLittleEndian;

/**
 * This class is not thread-safe.
 */
public class Scu implements Closeable {

    private final String transferSyntax;
    private final String calledAet;
    private final Connection remote;
    private final Device device;
    private final ApplicationEntity ae;
    private final Connection connection;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    public Scu(Options options) {
        transferSyntax = options.getTransferSyntax();
        calledAet = options.calledAetTitle;
        device = new Device(options.getDeviceName());
        ae = new ApplicationEntity(options.getCallingAetTitle());
        connection = new Connection();
        remote = new Connection();
        remote.setPort(options.getPort());
        remote.setHostname(options.getHostname());
        device.addConnection(connection);
        device.addApplicationEntity(ae);
        executor = Executors.newSingleThreadExecutor();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);

    }

    private static Options configureOptions(DimseMock mock) {
        return Options.builder()
                .calledAetTitle(mock.getAeTitle())
                .hostname(mock.getHostname())
                .port(mock.getPort())
                .build();
    }

    public static List<Attributes> cfind(DimseMock mock, String abstractSyntax, Attributes query) {
        try (var scu = new Scu(configureOptions(mock))) {
            return scu.cfind(abstractSyntax, query);
        }
    }

    public static Optional<Attributes> cmove(DimseMock mock, String abstractSyntax, Attributes query, String aem) {
        try (var scu = new Scu(configureOptions(mock))) {
            return scu.cmove(abstractSyntax, query, aem);
        }
    }

    /**
     * Simple C-FIND scu.
     *
     * @param abstractSyntax Affected SOP Class UID
     * @param keys           Dataset
     * @return List of datasets
     * @throws DicomServiceException in case of error
     */
    public List<Attributes> cfind(String abstractSyntax, Attributes keys) {

        return execute(abstractSyntax, association -> {
            DimseRSP rsp = association.cfind(abstractSyntax,
                    0,
                    keys,
                    transferSyntax,
                    Integer.MAX_VALUE);
            List<Attributes> results = new ArrayList<>();
            while (rsp.next()) {
                var status = rsp.getCommand().getInt(Tag.Status, 0);
                if (!Status.isPending(status) && status != Status.Success) {
                    throw new DicomServiceException(status, rsp.getCommand().getString(Tag.ErrorComment));
                }
                Optional.ofNullable(rsp.getDataset()).ifPresent(results::add);
            }
            return results;
        });
    }

    public Optional<Attributes> cmove(String abstractSyntax, Attributes keys, String aem) {
        return execute(abstractSyntax, association -> {
            DimseRSP rsp = association.cmove(abstractSyntax,
                    0,
                    keys,
                    transferSyntax, aem);
            Optional<Attributes> lastResult = Optional.empty();
            while (rsp.next()) {
                var status = rsp.getCommand().getInt(Tag.Status, 0);
                if (!Status.isPending(status) && status != Status.Success) {
                    throw new DicomServiceException(status, rsp.getCommand().getString(Tag.ErrorComment));
                }
                lastResult = Optional.ofNullable(rsp.getCommand());
            }
            return lastResult;
        });
    }

    @SneakyThrows
    private <T> T execute(String abstractSyntax, AssociationCallable<T> callable) {
        AAssociateRQ associateRQ = new AAssociateRQ();
        associateRQ.setCalledAET(calledAet);
        associateRQ.addPresentationContext(new PresentationContext(1, abstractSyntax, transferSyntax));
        var association = ae.connect(connection, remote, associateRQ);
        try {
            return callable.call(association);
        } finally {
            release(association);
        }
    }

    @SneakyThrows
    private void release(Association association) {
        if (association != null && association.isReadyForDataTransfer()) {
            association.waitForOutstandingRSP();
            association.release();
        }
    }

    public void close() {
        executor.shutdown();
        scheduledExecutor.shutdown();
    }

    @Builder
    @Getter
    public static class Options {

        @Builder.Default
        private final String hostname = "localhost";
        private final int port;
        @Builder.Default
        private final String deviceName = "findscu";
        @Builder.Default
        private final String callingAetTitle = "findscu";
        @Builder.Default
        private final String calledAetTitle = "findscp";
        @Builder.Default
        private final String transferSyntax = ImplicitVRLittleEndian;
    }

    @FunctionalInterface
    private interface AssociationCallable<T> {

        T call(Association association) throws Exception;
    }
}
