package io.github.damienvdb.dcm4junit.dicom;

import org.dcm4che3.data.Tag;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.github.damienvdb.dcm4junit.assertions.Assertions.assertThat;

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
}
