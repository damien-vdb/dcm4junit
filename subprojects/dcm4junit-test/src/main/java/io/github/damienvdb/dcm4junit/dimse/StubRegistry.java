package io.github.damienvdb.dcm4junit.dimse;

import org.dcm4che3.data.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.damienvdb.dcm4junit.dimse.Stub.IncomingRequest;

public class StubRegistry<S extends Stub<?>> {
    protected final List<S> stubs = new ArrayList<>();
    protected final List<IncomingRequest> requests = new ArrayList<>();

    private static String formatRequests(List<IncomingRequest> matches) {
        return matches.stream()
                .map(request -> request.getKeys().toString())
                .collect(Collectors.joining("\n\n"));
    }

    public void register(S stub) {
        this.stubs.add(stub);
    }

    public boolean isEmpty() {
        return stubs.isEmpty();
    }

    public Optional<S> findStub(Attributes rq, Attributes keys) {
        IncomingRequest incomingRequest = new IncomingRequest(rq, keys);
        requests.add(incomingRequest);
        List<S> responsesMatched = stubs.stream()
                .filter(s -> s.test(incomingRequest))
                .toList();
        if (responsesMatched.isEmpty()) {
            return Optional.empty();
        }
        if (responsesMatched.size() == 1) {
            return Optional.of(responsesMatched.get(0));
        }

        throw new IllegalStateException("More than one stub matched for " + keys);
    }

    public void verifyRequests(Predicate<Attributes> predicate) {
        List<IncomingRequest> matches = requests.stream()
                .filter(r -> predicate.test(r.getKeys()))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            throw new AssertionError("No requests matched predicate. Actual requests:\n" + StubRegistry.formatRequests(matches));
        }
        if (matches.size() == 1) {
            return;
        }
        throw new IllegalStateException("More than one request matched predicate. Actual requests:\n" + StubRegistry.formatRequests(matches));

    }

    public void clear() {
        this.stubs.clear();
    }

    public int size() {
        return this.stubs.size();
    }
}
