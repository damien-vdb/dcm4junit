package io.github.damienvdb.dcm4junit.dimse.cmove;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CMoveStubRegistry {
    public static final String DEFAULT_SOPCLASS = UID.StudyRootQueryRetrieveInformationModelMove;

    private final List<CMoveStub> stubs = new ArrayList<>();
    private final List<IncomingRequest> requests = new ArrayList<>();

    private static String formatRequests(List<IncomingRequest> matches) {
        return matches.stream()
                .map(request -> request.getKeys().toString())
                .collect(Collectors.joining("\n\n"));
    }

    protected void register(CMoveStub stub) {
        this.stubs.add(stub);
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

    public boolean isEmpty() {
        return stubs.isEmpty();
    }

    public void clear() {
        this.stubs.clear();
    }

    public int size() {
        return this.stubs.size();
    }

    public Optional<CMoveStub> findStub(Attributes rq, Attributes keys) {
        IncomingRequest incomingRequest = new IncomingRequest(rq, keys);
        requests.add(incomingRequest);
        List<CMoveStub> responsesMatched = stubs.stream()
                .filter(s -> s.test(incomingRequest))
                .collect(Collectors.toList());
        if (responsesMatched.isEmpty()) {
            return Optional.empty();
        }
        if (responsesMatched.size() == 1) {
            return Optional.ofNullable(responsesMatched.get(0));
        }

        throw new IllegalStateException("More than one stub matched for " + keys);
    }

    @RequiredArgsConstructor
    @Getter
    static final class IncomingRequest {
        private final Attributes rq;
        private final Attributes keys;
    }
}
