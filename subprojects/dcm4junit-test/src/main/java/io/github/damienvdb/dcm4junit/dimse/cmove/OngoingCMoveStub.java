package io.github.damienvdb.dcm4junit.dimse.cmove;

import io.github.damienvdb.dcm4junit.dimse.cmove.CMoveStub.CMoveStubBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;

import java.nio.file.Path;
import java.util.function.Predicate;

public class OngoingCMoveStub extends CMoveStubRegistry {
    private final CMoveStubRegistry registry;
    private final CMoveStubBuilder builder;

    public OngoingCMoveStub(Predicate<Attributes> keysPredicate, CMoveStubRegistry registry) {
        this.builder = CMoveStub.builder().expectedKeys(keysPredicate);
        this.registry = registry;
    }

    public OngoingCMoveStub willStore(Path path) {
        this.builder.file(path);
        return this;
    }

    public void to(String aet, String ip, int port) {
        this.builder.to(aet, ip, port);
        this.registry.register(builder.build());
    }

    public void willThrow(DicomServiceException e) {
        this.registry.register(builder.exception(e).build());
    }
}
