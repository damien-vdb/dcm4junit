package io.github.damienvdb.dcm4junit.utils;

import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.TagUtils;
import org.junit.jupiter.api.Test;

import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static io.github.damienvdb.dcm4junit.utils.AttributesUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.dcm4che3.data.Tag.*;

class AttributesUtilsTest {

    @Test
    void pointers_singleAttribute() {
        ItemPointer[] expected = new ItemPointer[]{
                new ItemPointer(Tag.PatientID)
        };

        assertThat(pointers("DicomAttribute[@tag=\"00100020\"]"))
                .isEqualTo(expected);
    }

    @Test
    void pointers_nestedSequence() {
        ItemPointer[] expected = new ItemPointer[]{
                at(ReferencedSeriesSequence, 0),
                at(ReferencedInstanceSequence, 0),
                new ItemPointer(Tag.ReferencedSOPInstanceUID)
        };

        assertThat(pointers(
                "DicomAttribute[@tag=\"00081115\"]/Item[@number=\"1\"]/DicomAttribute[@tag=\"0008114A\"]/Item[@number=\"1\"]/DicomAttribute[@tag=\"00081155\"]"
        )).isEqualTo(expected);
    }

    @Test
    void pointers_privateTag() {
        ItemPointer[] expected = new ItemPointer[]{
                new ItemPointer("XXX", TagUtils.intFromHexString("00410275"))
        };

        assertThat(pointers("DicomAttribute[@tag=\"00410275\" and @privateCreator=\"XXX\"]"))
                .isEqualTo(expected);
    }

    @Test
    void selectString_simpled() {

        var nested = builder()
                .setString(ImageType, "ORIGINAL", "PRIMARY")
                .build();
        String path = "DicomAttribute[@tag=\"00080008\"]";

        assertThat(selectString(path, nested))
                .isEqualTo(selectString(path, nested, 0))
                .isEqualTo("ORIGINAL");

        assertThat(selectString(path, nested, 1))
                .isEqualTo("PRIMARY");
    }

    @Test
    void selectString_nested() {

        String uid = Faker.uid();
        var nested = builder()
                .addItems(ReferencedSeriesSequence,
                        builder()
                                .setString(SeriesInstanceUID, uid)
                                .build())
                .build();
        String path = "DicomAttribute[@tag=\"00081115\"]/Item[@number=\"1\"]/DicomAttribute[@tag=\"0020000E\"]";

        assertThat(selectString(path, nested))
                .isEqualTo(uid);
    }
}
