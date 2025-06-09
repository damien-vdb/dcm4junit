package io.github.damienvdb.dcm4junit.dicom;

import io.github.damienvdb.dcm4junit.utils.Faker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import org.dcm4che3.data.Attributes;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.dcm4che3.data.Tag.*;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Series {

    @Builder.Default
    private String seriesInstanceUid = Faker.uid();
    @Builder.Default
    private String seriesDescription = Faker.description();
    @Builder.Default
    private String modality = Faker.modality();
    @Builder.Default
    private int instancesCount = 1;
    @Singular(value = "customize")
    private List<Consumer<Attributes>> customizers;

    public Stream<Attributes> generateInstances(Attributes studyBase) {
        Attributes seriesBase = AttributesBuilder.builder(studyBase)
                .setString(SeriesInstanceUID, seriesInstanceUid)
                .setString(SeriesDescription, seriesDescription)
                .setString(Modality, modality)
                .build();

        customizers.forEach(customizer -> customizer.accept(seriesBase));

        return IntStream.range(1, instancesCount + 1)
                .mapToObj(i -> AttributesBuilder.builder(seriesBase)
                        .setString(SOPInstanceUID, Faker.uid())
                        .setInt(InstanceNumber, i)
                        .build()
                );
    }
}
