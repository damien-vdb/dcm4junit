package com.github.damienvdb.dcm4junit.dimse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.isEqual;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class OngoingCFindStub {

    private final MockFindScpRegistry.Stub.StubBuilder builder;
    private final MockFindScpRegistry registry;

    public OngoingCFindStub withAffectedSopClassUid(String affectedSopClassUid) {
        this.builder.affectedSOPClassUID(isEqual(affectedSopClassUid));
        return this;
    }

    public void willReturn(Attributes... datasets) {
        List<Attributes> responses = Optional.ofNullable(datasets)
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        this.registry.register(builder.responses(responses).build());
    }
}
