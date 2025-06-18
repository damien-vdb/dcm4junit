package com.github.damienvdb.dcm4junit.dimse.cfind;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PACKAGE)
class Stub {
    @Builder.Default
    private final Predicate<String> affectedSOPClassUID = Predicate.isEqual(StubRegistry.DEFAULT_SOPCLASS);
    private final Predicate<Attributes> expectedKeys;
    @Getter(AccessLevel.PACKAGE)
    private final List<Attributes> responses;

    public boolean test(Attributes rq, Attributes keys) {
        return affectedSOPClassUID.test(rq.getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(keys);
    }

    // Working around lombok compilation issue with Gradle (referenced in a field)
    public static class StubBuilder {
    }
}
