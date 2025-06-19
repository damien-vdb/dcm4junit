package com.github.damienvdb.dcm4junit.dimse.cfind;

import lombok.AccessLevel;
import lombok.Builder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.DicomServiceException;

import java.util.List;
import java.util.function.Predicate;

@Builder(access = AccessLevel.PACKAGE)
class Stub {
    @Builder.Default
    private final Predicate<String> affectedSOPClassUID = Predicate.isEqual(StubRegistry.DEFAULT_SOPCLASS);
    private final Predicate<Attributes> expectedKeys;
    private final List<Attributes> responses;
    private final DicomServiceException exception;


    private Stub(Predicate<String> affectedSOPClassUID, Predicate<Attributes> expectedKeys,
                 List<Attributes> responses, DicomServiceException exception) {
        if (exception != null && responses != null) {
            throw new IllegalArgumentException("Cannot specify both responses and exception");
        }
        this.affectedSOPClassUID = affectedSOPClassUID;
        this.expectedKeys = expectedKeys;
        this.responses = responses;
        this.exception = exception;
    }

    public boolean test(Attributes rq, Attributes keys) {
        return affectedSOPClassUID.test(rq.getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(keys);
    }

    public List<Attributes> apply() throws DicomServiceException {
        if (responses != null) {
            return responses;
        }
        throw exception;
    }

    // Working around lombok compilation issue with Gradle (referenced in a field)
    public static class StubBuilder {
    }
}
