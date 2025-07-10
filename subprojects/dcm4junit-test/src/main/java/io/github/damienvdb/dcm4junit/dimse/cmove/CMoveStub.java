package io.github.damienvdb.dcm4junit.dimse.cmove;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.DicomServiceException;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

import static io.github.damienvdb.dcm4junit.dimse.cmove.CMoveStubRegistry.DEFAULT_SOPCLASS;
import static io.github.damienvdb.dcm4junit.dimse.cmove.CMoveStubRegistry.IncomingRequest;

@Builder(access = AccessLevel.PACKAGE)
public class CMoveStub {
    @Builder.Default
    private final Predicate<String> affectedSOPClassUID = Predicate.isEqual(DEFAULT_SOPCLASS);
    private final Predicate<Attributes> expectedKeys;
    @Builder.Default
    private final Duration delay = Duration.ZERO;
    @Singular
    private final List<Path> files;
    private final DicomServiceException exception;

    @Getter
    private final String aem;
    @Getter
    private final String hostname;
    @Getter
    private final int port;


    public boolean test(IncomingRequest request) {
        return affectedSOPClassUID.test(request.getRq().getString(Tag.AffectedSOPClassUID)) && expectedKeys.test(request.getKeys());
    }

    public List<Path> apply() throws DicomServiceException {
        applyDelay();
        if (files != null) {
            return files;
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
    public static class CMoveStubBuilder {

        public CMoveStubBuilder to(String aet, String ip, int port) {
            return this.aem(aet)
                    .hostname(ip)
                    .port(port);
        }
    }
}
