package io.github.damienvdb.dcm4junit.dimse.cmove;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import io.github.damienvdb.dcm4junit.dimse.DimseMockTest;
import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import io.github.damienvdb.dcm4junit.utilities.Scu;
import org.assertj.core.api.Assertions;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static io.github.damienvdb.dcm4junit.assertions.Assertions.assertThat;
import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static java.util.function.Predicate.isEqual;

@DimseMockSettings(aet = "MockMoveScpTest")
public class MockMoveScpTest {

    public static final File FILE = new File(DimseMockTest.class.getClassLoader().getResource("MR01.dcm").getPath());

    public static final Attributes QUERY = builder()
            .setString(Tag.QueryRetrieveLevel, "STUDY")
            .setString(Tag.StudyInstanceUID, "1.2.3.4.5")
            .build();

    @Test
    void movescp_responds_on_expected_keys(DimseMock mock) {

        // Mock the C-MOVE to forward to its C-STORE SCP, so that I can check it
        mock.getCMoveScp().stubFor(QUERY)
                .willStore(FILE.toPath())
                .to(mock.getAeTitle(), mock.getHostname(), mock.getPort());

        Optional<Attributes> rsp = Scu.cmove(mock, UID.StudyRootQueryRetrieveInformationModelMove, QUERY, "TARGET_AET");
        assertThat(rsp.get())
                .hasInt(Tag.NumberOfCompletedSuboperations, 1)
                .hasInt(Tag.NumberOfFailedSuboperations, 0)
                .hasInt(Tag.NumberOfWarningSuboperations, 0);

        mock.getCMoveScp().verify(isEqual(QUERY));

        Assertions.assertThat(mock.getCStoreScp().getFmis())
                .hasSize(1)
                .first()
                .satisfies(fmi -> assertThat(fmi)
                        .hasString(Tag.MediaStorageSOPClassUID, UID.MRImageStorage)
                        .hasString(Tag.MediaStorageSOPInstanceUID, "1.113654.5.15.1504")
                );
    }
}
