package com.github.damienvdb.dcm4junit.dimse;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class MockFindScpRegistry {

    public static final String DEFAULT_SOPCLASS = UID.StudyRootQueryRetrieveInformationModelFind;

    private final List<Stub> stubs = new ArrayList<>();

    public void put(Predicate<String> affectedSopClassUidPredicate, Predicate<Attributes> expectedKeys, List<Attributes> responses) {
        register(Stub.builder()
                .affectedSOPClassUID(affectedSopClassUidPredicate)
                .expectedKeys(expectedKeys)
                .responses(responses)
                .build());
    }

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
            return responsesMatched.get(0).responses;
        }

        throw new IllegalStateException("More than one stub matched for " + keys);
    }

    public void clear() {
        this.stubs.clear();
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PACKAGE)
    static class Stub {
        @Builder.Default
        private final Predicate<String> affectedSOPClassUID = Predicate.isEqual(DEFAULT_SOPCLASS);
        private final Predicate<Attributes> expectedKeys;
        private final List<Attributes> responses;

        public boolean test(Attributes rq, Attributes keys) {
            return affectedSOPClassUID.test(rq.getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(keys);
        }
    }
}
