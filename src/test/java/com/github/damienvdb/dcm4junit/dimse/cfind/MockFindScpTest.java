package com.github.damienvdb.dcm4junit.dimse.cfind;

import com.github.damienvdb.dcm4junit.dimse.DimseMock;
import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import com.github.damienvdb.dcm4junit.utilities.FindScu;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import static io.github.damienvdb.dcm4junit.assertions.AttributesAssert.toPredicate;
import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DimseMockSettings(aet = "MockFindScpTest")
public class MockFindScpTest {

    public static final Attributes QUERY = builder()
            .setString(Tag.QueryRetrieveLevel, "STUDY")
            .setString(Tag.PatientID, "PID")
            .setString(Tag.StudyInstanceUID, "1.2.3.4.5")
            .setNull(Tag.StudyDate)
            .build();
    public static final Attributes RESPONSE = builder(QUERY)
            .setString(Tag.StudyDate, "20250101")
            .build();

    @Test
    void findscp_responds_on_expected_keys(DimseMock mock) {

        mock.getCFindScp().stubFor(QUERY)
                .willReturn(RESPONSE);

        assertThat(FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .containsExactly(RESPONSE);


        mock.getCFindScp().verify(isEqual(QUERY));
    }

    @Test
    void findscp_respondsNothing_on_wrong_keys(DimseMock mock) {

        mock.getCFindScp().stubFor(QUERY)
                .willReturn(RESPONSE);

        Attributes query = builder(QUERY)
                .setString(Tag.PatientID, "PID2")
                .build();

        assertThat(FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, query))
                .isEmpty();
        mock.getCFindScp().verify(isEqual(query));
    }

    @Test
    void findscp_responds_by_predicate(DimseMock mock) {

        mock.getCFindScp().stubFor(
                        toPredicate(as -> as.hasString(Tag.PatientID, QUERY.getString(Tag.PatientID)))
                )
                .willReturn(RESPONSE);

        assertThat(FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .containsExactly(RESPONSE);

        assertThat(FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind,
                builder(QUERY)
                        .setString(Tag.PatientID, "PID2")
                        .build()))
                .isEmpty();

        mock.getCFindScp().verify(toPredicate(as -> as.hasString(Tag.PatientID, "PID2")));
    }

    @Test
    void findscp_respondsNothing_on_wrong_model(DimseMock mock) {

        mock.getCFindScp()
                .stubFor(QUERY)
                .withAffectedSopClassUid(UID.PatientRootQueryRetrieveInformationModelFind)
                .willReturn(RESPONSE);


        assertThat(FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .isEmpty();
    }

    @Test
    void findscp_aborts_on_error(DimseMock mock) {

        mock.getCFindScp()
                .stubFor(QUERY)
                .willThrow(new DicomServiceException(Status.OutOfResources, "out of resources"));

        assertThatThrownBy(() -> FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .isInstanceOf(DicomServiceException.class)
                .hasMessage("out of resources");
    }

    @Test
    void findscp_delays_responses(DimseMock mock) throws Exception {
        Duration delay = Duration.ofSeconds(1);
        mock.getCFindScp()
                .stubFor(QUERY)
                .withDelay(delay)
                .willReturn(RESPONSE);

        Callable<List<Attributes>> callable = () -> FindScu.query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY);

        long start = System.currentTimeMillis();
        assertThat(callable.call())
                .containsExactly(RESPONSE);
        assertThat(System.currentTimeMillis() - start)
                .isGreaterThanOrEqualTo(delay.toMillis());
    }
}
