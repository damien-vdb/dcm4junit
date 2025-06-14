package io.github.damienvdb.dcm4junit.dicom;

import io.github.damienvdb.dcm4junit.utils.Faker;
import org.dcm4che3.data.Tag;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.github.damienvdb.dcm4junit.assertions.Assertions.assertThat;
import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static org.dcm4che3.data.Tag.*;

class AttributesBuilderTest {

    @Test
    void setDateTime() {
        var date = new Date();
        var sut = AttributesBuilder.builder()
                .setDateTime(Tag.StudyDateAndTime, date)
                .build();

        assertThat(sut).hasString(Tag.StudyDate, new SimpleDateFormat("yyyyMMdd").format(date));
        assertThat(sut).hasString(Tag.StudyTime, new SimpleDateFormat("HHmmss").format(date));
    }

    @Test
    void remove_nested() {
        String uid = Faker.uid();

        var nested = builder()
                .addItems(ReferencedSeriesSequence,
                        builder()
                                .setString(SeriesInstanceUID, uid)
                                .addItems(ReferencedInstanceSequence,
                                        builder()
                                                .setString(ReferencedSOPClassUID, uid)
                                                .setString(ReferencedSOPInstanceUID, uid)
                                                .build(),
                                        builder()
                                                .setString(ReferencedSOPClassUID, uid)
                                                .setString(ReferencedSOPInstanceUID, uid)
                                                .build()
                                )
                                .build()
                )
                .setNull(SOPInstanceUID);

        nested.remove("DicomAttribute[@tag=\"00080018\"]");
        nested.remove("DicomAttribute[@tag=\"00081115\"]/Item[@number=\"1\"]/DicomAttribute[@tag=\"0008114A\"]");

        var result = nested.build();
        assertThat(result).isEqualTo(
                builder()
                        .addItems(ReferencedSeriesSequence,
                                builder()
                                        .setString(SeriesInstanceUID, uid)
                                        .build()
                        )
                        .build());

    }
}
