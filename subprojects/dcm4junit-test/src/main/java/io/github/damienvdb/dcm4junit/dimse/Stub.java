package io.github.damienvdb.dcm4junit.dimse;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.DicomServiceException;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@SuperBuilder
@AllArgsConstructor
public class Stub<T> {
    protected final Predicate<String> affectedSOPClassUID;
    protected final Predicate<Attributes> expectedKeys;
    @Singular
    protected final List<T> responses;
    protected final DicomServiceException exception;
    @Builder.Default
    protected Duration delay = Duration.ZERO;

    public boolean test(IncomingRequest request) {
        return affectedSOPClassUID.test(request.getRq().getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(request.getKeys());
    }

    public List<T> apply() throws DicomServiceException {
        applyDelay();
        if (exception != null) {
            throw exception;
        }
        return responses;
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

    @RequiredArgsConstructor
    @Getter
    public static final class IncomingRequest {
        private final Attributes rq;
        private final Attributes keys;
    }
}
