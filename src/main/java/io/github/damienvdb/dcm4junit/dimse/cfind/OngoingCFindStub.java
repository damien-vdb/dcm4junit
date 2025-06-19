package io.github.damienvdb.dcm4junit.dimse.cfind;

import io.github.damienvdb.dcm4junit.dimse.cfind.Stub.StubBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.isEqual;

public class OngoingCFindStub {

    private final StubBuilder builder;
    private final StubRegistry registry;

    public OngoingCFindStub(Predicate<Attributes> keysPredicate, StubRegistry registry) {
        this.builder = Stub.builder().expectedKeys(keysPredicate);
        this.registry = registry;
    }

    public OngoingCFindStub withAffectedSopClassUid(String affectedSopClassUid) {
        this.builder.affectedSOPClassUID(isEqual(affectedSopClassUid));
        return this;
    }

    public OngoingCFindStub withDelay(Duration delay) {
        this.builder.delay(delay);
        return this;
    }

    public void willReturn(Attributes... datasets) {
        List<Attributes> responses = Optional.ofNullable(datasets)
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        this.registry.register(builder.responses(responses).build());
    }

    public void willThrow(DicomServiceException e) {
        this.registry.register(builder.exception(e).build());
    }
}
