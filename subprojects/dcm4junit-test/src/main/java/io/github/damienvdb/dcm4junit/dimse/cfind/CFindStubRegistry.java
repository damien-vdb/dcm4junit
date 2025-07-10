package io.github.damienvdb.dcm4junit.dimse.cfind;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.service.DicomServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CFindStubRegistry {

    public static final String DEFAULT_SOPCLASS = UID.StudyRootQueryRetrieveInformationModelFind;

    private final List<CFindStub> stubs = new ArrayList<>();
    private final List<IncomingRequest> requests = new ArrayList<>();

    private static String formatRequests(List<IncomingRequest> matches) {
        return matches.stream()
                .map(request -> request.getKeys().toString())
                .collect(Collectors.joining("\n\n"));
    }

    public void register(CFindStub stub) {
        this.stubs.add(stub);
    }

    public boolean isEmpty() {
        return stubs.isEmpty();
    }

    List<Attributes> findResponses(Attributes rq, Attributes keys) throws DicomServiceException {
        IncomingRequest incomingRequest = new IncomingRequest(rq, keys);
        requests.add(incomingRequest);
        List<CFindStub> responsesMatched = stubs.stream()
                .filter(s -> s.test(incomingRequest))
                .collect(Collectors.toList());
        if (responsesMatched.isEmpty()) {
            return emptyList();
        }
        if (responsesMatched.size() == 1) {
            return responsesMatched.get(0).apply();
        }

        throw new IllegalStateException("More than one stub matched for " + keys);
    }

    void verifyRequests(Predicate<Attributes> predicate) {
        List<IncomingRequest> matches = requests.stream()
                .filter(r -> predicate.test(r.getKeys()))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            throw new AssertionError("No requests matched predicate. Actual requests:\n" + formatRequests(matches));
        }
        if (matches.size() == 1) {
            return;
        }
        throw new IllegalStateException("More than one request matched predicate. Actual requests:\n" + formatRequests(matches));

    }

    public void clear() {
        this.stubs.clear();
    }

    public int size() {
        return this.stubs.size();
    }

    @RequiredArgsConstructor
    @Getter
    static final class IncomingRequest {
        private final Attributes rq;
        private final Attributes keys;
    }
}
