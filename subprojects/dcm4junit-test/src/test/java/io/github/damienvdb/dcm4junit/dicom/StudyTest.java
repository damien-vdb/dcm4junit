package io.github.damienvdb.dcm4junit.dicom;

import io.github.damienvdb.dcm4junit.assertions.Assertions;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StudyTest {

    @Test
    void generate() {
        Study study = Study.builder()
                .series(List.of(
                        Series.builder()
                                .instancesCount(2)
                                .build()
                ))
                .build();

        List<Attributes> instances = study.generate().toList();

        assertThat(instances)
                .hasSize(2)
                .allSatisfy(attrs -> Assertions.assertThat(attrs)
                        .containsValues(
                                Tag.StudyInstanceUID,
                                Tag.StudyDescription,
                                Tag.PatientID,
                                Tag.PatientName,
                                Tag.PatientAge,
                                Tag.StudyDate,
                                Tag.StudyTime,
                                Tag.SeriesInstanceUID,
                                Tag.SeriesDescription,
                                Tag.Modality,
                                Tag.SOPInstanceUID,
                                Tag.InstanceNumber
                        ));


        // Verify that each instance has a unique SOPInstanceUID
        var sopInstanceUIDs = instances.stream()
                .map(attrs -> attrs.getString(Tag.SOPInstanceUID))
                .collect(Collectors.toSet());
        assertEquals(instances.size(), sopInstanceUIDs.size(), "Each instance should have a unique SOPInstanceUID");

    }
}
