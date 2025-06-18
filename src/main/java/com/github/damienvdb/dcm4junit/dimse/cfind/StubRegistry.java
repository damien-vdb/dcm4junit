package com.github.damienvdb.dcm4junit.dimse.cfind;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class StubRegistry {

    public static final String DEFAULT_SOPCLASS = UID.StudyRootQueryRetrieveInformationModelFind;

    private final List<Stub> stubs = new ArrayList<>();

    public void register(Stub stub) {
        this.stubs.add(stub);
    }

    public boolean isEmpty() {
        return stubs.isEmpty();
    }

    public List<Attributes> get(Attributes rq, Attributes keys) {
        List<Stub> responsesMatched = stubs.stream()
                .filter(s -> s.test(rq, keys))
                .collect(Collectors.toList());
        if (responsesMatched.isEmpty()) {
            return emptyList();
        }
        if (responsesMatched.size() == 1) {
            return responsesMatched.get(0).getResponses();
        }

        throw new IllegalStateException("More than one stub matched for " + keys);
    }

    public void clear() {
        this.stubs.clear();
    }

}
