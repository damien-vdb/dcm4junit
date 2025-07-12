package io.github.damienvdb.dcm4junit.dimse.cmove;

import io.github.damienvdb.dcm4junit.dimse.cmove.CMoveStub.CMoveStubBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.service.DicomServiceException;

import java.nio.file.Path;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;

public class OngoingCMoveStub extends CMoveStubRegistry {

    public static final String DEFAULT_SOPCLASS = UID.StudyRootQueryRetrieveInformationModelMove;

    private final CMoveStubRegistry registry;
    private final CMoveStubBuilder<?, ?> builder;

    public OngoingCMoveStub(Predicate<Attributes> keysPredicate, CMoveStubRegistry registry) {
        this.builder = CMoveStub.builder()
                .affectedSOPClassUID(Predicate.isEqual(DEFAULT_SOPCLASS))
                .expectedKeys(keysPredicate);
        this.registry = registry;
    }

    public OngoingCMoveStub withAffectedSopClassUid(String affectedSopClassUid) {
        this.builder.affectedSOPClassUID(isEqual(affectedSopClassUid));
        return this;
    }

    public OngoingCMoveStub willStore(Path path) {
        this.builder.response(path);
        return this;
    }

    public void to(String aet, String ip, int port) {
        this.builder.aem(aet)
                .hostname(ip)
                .port(port)
                .build();
        this.registry.register(this.builder.aem(aet)
                .hostname(ip)
                .port(port)
                .build());
    }

    public void willThrow(DicomServiceException e) {
        this.registry.register(builder.exception(e).build());
    }
}
