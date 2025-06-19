package com.github.damienvdb.dcm4junit.dimse.cfind;

import lombok.AccessLevel;
import lombok.Builder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.DicomServiceException;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Builder(access = AccessLevel.PACKAGE)
class Stub {
    @Builder.Default
    private final Predicate<String> affectedSOPClassUID = Predicate.isEqual(StubRegistry.DEFAULT_SOPCLASS);
    private final Predicate<Attributes> expectedKeys;
    @Builder.Default
    private final Duration delay = Duration.ZERO;
    private final List<Attributes> responses;
    private final DicomServiceException exception;


    private Stub(Predicate<String> affectedSOPClassUID, Predicate<Attributes> expectedKeys, Duration delay,
                 List<Attributes> responses, DicomServiceException exception) {
        if (exception != null && responses != null) {
            throw new IllegalArgumentException("Cannot specify both responses and exception");
        }
        this.affectedSOPClassUID = affectedSOPClassUID;
        this.expectedKeys = expectedKeys;
        this.delay = delay;
        this.responses = responses;
        this.exception = exception;
    }

    public boolean test(StubRegistry.IncomingRequest request) {
        return affectedSOPClassUID.test(request.getRq().getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(request.getKeys());
    }

    public List<Attributes> apply() throws DicomServiceException {
        applyDelay();
        if (responses != null) {
            return responses;
        }
        throw exception;
    }

    private void applyDelay() {
        long millis = delay.toMillis();
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    // Working around lombok compilation issue with Gradle (referenced in a field)
    public static class StubBuilder {
    }
}
