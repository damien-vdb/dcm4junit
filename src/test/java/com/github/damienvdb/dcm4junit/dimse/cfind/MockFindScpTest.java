package com.github.damienvdb.dcm4junit.dimse.cfind;

import com.github.damienvdb.dcm4junit.dimse.DimseMock;
import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockExtension;
import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import com.github.damienvdb.dcm4junit.utilities.FindScu;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.damienvdb.dcm4junit.assertions.AttributesAssert.toPredicate;
import static com.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DimseMockExtension.class)
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

    private static FindScu.Options configureOptions(DimseMock mock) {
        return FindScu.Options.builder()
                .calledAetTitle(mock.getAeTitle())
                .hostname(mock.getHostname())
                .port(mock.getPort())
                .build();
    }

    private static List<Attributes> query(DimseMock mock, String abstractSyntax, Attributes query) {
        try (var scu = new FindScu(configureOptions(mock))) {
            return scu.query(abstractSyntax, query);
        }
    }

    @Test
    void findscp_responds_on_expected_keys(DimseMock mock) {

        mock.getCFindScp().stubFor(QUERY)
                .willReturn(RESPONSE);

        assertThat(query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .containsExactly(RESPONSE);
    }

    @Test
    void findscp_respondsNothing_on_wrong_keys(DimseMock mock) {

        mock.getCFindScp().stubFor(QUERY)
                .willReturn(RESPONSE);

        List<Attributes> results = query(mock, UID.StudyRootQueryRetrieveInformationModelFind,
                builder(QUERY)
                        .setString(Tag.PatientID, "PID2")
                        .build());

        assertThat(results)
                .isEmpty();
    }

    @Test
    void findscp_responds_by_predicate(DimseMock mock) {

        mock.getCFindScp().stubFor(
                        toPredicate(as -> as.hasString(Tag.PatientID, QUERY.getString(Tag.PatientID)))
                )
                .willReturn(RESPONSE);

        assertThat(query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .containsExactly(RESPONSE);

        assertThat(query(mock, UID.StudyRootQueryRetrieveInformationModelFind,
                builder(QUERY)
                        .setString(Tag.PatientID, "PID2")
                        .build()))
                .isEmpty();
    }

    @Test
    void findscp_respondsNothing_on_wrong_model(DimseMock mock) {

        mock.getCFindScp()
                .stubFor(QUERY)
                .withAffectedSopClassUid(UID.PatientRootQueryRetrieveInformationModelFind)
                .willReturn(RESPONSE);


        assertThat(query(mock, UID.StudyRootQueryRetrieveInformationModelFind, QUERY))
                .isEmpty();
    }
}
