package com.github.damienvdb.dcm4junit.dicom;

import com.github.damienvdb.dcm4junit.utils.Faker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.dcm4che3.data.Tag.*;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Study {

    @Builder.Default
    private String studyInstanceUid = Faker.uid();
    @Builder.Default
    private String studyDescription = Faker.description();
    @Builder.Default
    private String patientID = Faker.patientID();
    @Builder.Default
    private String patientName = Faker.personName();
    @Builder.Default
    private String patientAge = Faker.AS();
    @Builder.Default
    private String studyDate = Faker.DA();
    @Builder.Default
    private String studyTime = Faker.TM();
    @Builder.Default
    private List<Series> series = List.of(Series.builder().build());
    @Singular(value = "customize")
    private List<Consumer<Attributes>> customizers;

    public Stream<Attributes> generate() {
        var base = AttributesBuilder.builder()
                .setString(StudyInstanceUID, studyInstanceUid)
                .setString(StudyDescription, studyDescription)
                .setString(PatientID, patientID)
                .setString(PatientName, patientName)
                .setString(PatientAge, patientAge)
                .setString(StudyDate, studyDate)
                .setString(StudyTime, studyTime)
                .build();

        customizers.forEach(customizer -> customizer.accept(base));

        return series.stream()
                .flatMap(s -> s.generateInstances(base));
    }

    public static class StudyBuilder {

        public StudyBuilder studyDateTime(Date date) {
            this.studyDate(DateUtils.formatDA(TimeZone.getDefault(), date));
            this.studyTime(DateUtils.formatTM(TimeZone.getDefault(), date));
            return this;
        }
    }
}
