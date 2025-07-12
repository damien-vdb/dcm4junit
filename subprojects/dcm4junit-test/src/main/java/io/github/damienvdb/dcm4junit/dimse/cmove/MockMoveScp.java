package io.github.damienvdb.dcm4junit.dimse.cmove;

import io.github.damienvdb.dcm4junit.dimse.jupiter.CMoveScp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.*;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;

@Slf4j
public class MockMoveScp extends BasicCMoveSCP {

    private final CMoveStubRegistry registry = new CMoveStubRegistry();

    public MockMoveScp(CMoveScp cmoveScp) {
        super(cmoveScp.sopClasses());
    }

    @SneakyThrows
    private static InstanceLocator toLocator(Path file) {

        try (DicomInputStream stream = new DicomInputStream(file.toFile())) {
            var fmi = stream.readFileMetaInformation();
            return new InstanceLocator(fmi.getString(Tag.MediaStorageSOPClassUID),
                    fmi.getString(Tag.MediaStorageSOPInstanceUID),
                    fmi.getString(Tag.TransferSyntaxUID),
                    file.toUri().toString());
        }
    }

    public OngoingCMoveStub stubFor(Attributes query) {
        return new OngoingCMoveStub(isEqual(query), registry);
    }

    public OngoingCMoveStub stubFor(Predicate<Attributes> predicate) {
        return new OngoingCMoveStub(predicate, registry);
    }

    public void verify(Predicate<Attributes> predicate) {
        this.registry.verifyRequests(predicate);
    }

    @Override
    protected RetrieveTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException {
        if (this.registry.isEmpty()) {
            throw new DicomServiceException(Status.UnableToProcess, "No ongoing stub");
        }
        var stubOpt = this.registry.findStub(rq, keys);
        if (stubOpt.isEmpty()) {
            return null;
        }
        var stub = stubOpt.get();
        var paths = stub.apply();

        var locators = paths
                .stream()
                .map(MockMoveScp::toLocator)
                .toList();

        Connection destination = new Connection();
        destination.setHostname(stub.getHostname());
        destination.setPort(stub.getPort());

        AAssociateRQ aarq = makeAAssociateRQ(as.getLocalAET(), stub.getAem(), locators);
        Association storeas = openStoreAssociation(as, destination, aarq);

        return new BasicRetrieveTask<>(Dimse.C_MOVE_RQ, as, pc, rq, locators, storeas);

    }

    private Association openStoreAssociation(Association as,
                                             Connection remote, AAssociateRQ aarq) throws DicomServiceException {
        try {
            return as.getApplicationEntity().connect(
                    as.getConnection(), remote, aarq);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
        }
    }

    private AAssociateRQ makeAAssociateRQ(String callingAET,
                                          String calledAET, List<InstanceLocator> matches) {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCalledAET(calledAET);
        aarq.setCallingAET(callingAET);

        for (InstanceLocator match : matches) {
            if (aarq.addPresentationContextFor(match.cuid, match.tsuid)) {
                if (!UID.ExplicitVRLittleEndian.equals(match.tsuid))
                    aarq.addPresentationContextFor(match.cuid, UID.ExplicitVRLittleEndian);
                if (!UID.ImplicitVRLittleEndian.equals(match.tsuid))
                    aarq.addPresentationContextFor(match.cuid, UID.ImplicitVRLittleEndian);
            }
        }
        return aarq;
    }
}
