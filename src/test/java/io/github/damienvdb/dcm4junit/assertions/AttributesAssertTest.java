package io.github.damienvdb.dcm4junit.assertions;

import io.github.damienvdb.dcm4junit.dicom.DicomField;
import io.github.damienvdb.dcm4junit.dicom.DicomTag;
import io.github.damienvdb.dcm4junit.utils.Faker;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.damienvdb.dcm4junit.assertions.Assertions.assertThat;
import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static io.github.damienvdb.dcm4junit.utils.AttributesUtils.at;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dcm4che3.data.Tag.*;

class AttributesAssertTest {

    private final Attributes simple = builder()
            .setString(StudyInstanceUID, Faker.uid())
            .setString(StudyDescription, Faker.description())
            .setNull(SeriesInstanceUID)
            .setReadOnly()
            .build();

    private final Attributes nested = builder()
            .setString(StudyInstanceUID, Faker.uid())
            .setString(SeriesInstanceUID, Faker.uid())
            .setString(StudyDescription, Faker.description())
            .addItems(VOILUTSequence,
                    builder()
                            .setInt(LUTDescriptor, 0)
                            .setNull(LUTExplanation)
                            .build()
            )
            .addItems(ReferencedSeriesSequence,
                    builder()
                            .setString(SeriesInstanceUID, Faker.uid())
                            .addItems(ReferencedInstanceSequence,
                                    builder()
                                            .setString(ReferencedSOPClassUID, Faker.uid())
                                            .setString(ReferencedSOPInstanceUID, Faker.uid())
                                            .build(),
                                    builder()
                                            .setString(ReferencedSOPClassUID, Faker.uid())
                                            .setString(ReferencedSOPInstanceUID, Faker.uid())
                                            .build()
                            )
                            .build()
            )
            .setNull(SOPInstanceUID)
            .setReadOnly()
            .build();

    private Attributes copyNestedAndRemoveNestedReferencedSOPClassUID() {
        return builder(nested)
                .remove(new ItemPointer[]{at(ReferencedSeriesSequence, 0), at(ReferencedInstanceSequence, 0)}, ReferencedSOPClassUID)
                .build();
    }

    @Nested
    class SimpleRecursive extends Simple {

        @Override
        RecursiveAttributesAssert assertAttributes() {
            return assertThat(simple);
        }
    }

    @Nested
    class SimpleRoot extends Simple {

        @Override
        RootOnlyAttributesAssert assertAttributes() {
            return assertThat(simple)
                    .usingRootOnlyComparison();
        }
    }

    abstract class Simple {


        @Test
        void containField() {
            assertAttributes()
                    .contains(new DicomField(new DicomTag(StudyInstanceUID), VR.UI, simple.getString(StudyInstanceUID)));

            assertThatThrownBy(() -> assertAttributes()
                    .contains(new DicomField(new DicomTag(StudyInstanceUID), VR.LO, simple.getString(StudyInstanceUID)))
            ).hasMessageContaining("to contain '(0020,000D) LO [%s] StudyInstanceUID'\n\nbut it does not", simple.getString(StudyInstanceUID));
        }

        @Test
        void containsValue() {
            assertAttributes()
                    .containsValue(StudyInstanceUID);

            assertThatThrownBy(() -> assertAttributes().containsValue(StudyDate))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to contain a value for tag:
                              (0008,0020) StudyDate
                            
                            but tag is not present""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        abstract AttributesAssert<?> assertAttributes();

        @Test
        void containsValueIgnoringTags() {
            assertThatThrownBy(() ->
                    assertAttributes()
                            .ignoringTags(StudyInstanceUID)
                            .containsValue(StudyInstanceUID)
            )
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to contain a value for tag:
                              (0020,000D) StudyInstanceUID
                            
                            but tag is not present""".formatted(simple.getString(StudyDescription)));
        }


        @Test
        void containsValues() {
            assertAttributes()
                    .containsValues(StudyInstanceUID, StudyDescription);

            assertThatThrownBy(() -> assertAttributes().containsValues(StudyInstanceUID, StudyDate))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to contain values for tags:
                              [(0008,0020) StudyDate, (0020,000D) StudyInstanceUID]
                            but could not find the following element(s):
                              [(0008,0020) StudyDate]""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        @Test
        void containTags() {
            assertAttributes()
                    .containsTags(StudyInstanceUID, SeriesInstanceUID);

            assertThatThrownBy(() -> assertAttributes().containsTags(StudyInstanceUID, SeriesDate))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to contain tags:
                              [(0008,0021) SeriesDate, (0020,000D) StudyInstanceUID]
                            but could not find the following tag(s):
                              [(0008,0021) SeriesDate]""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        @Test
        void containTagsIgnoringTags() {

            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .containsTags(StudyInstanceUID, SeriesDate))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to contain tags:
                              [(0008,0021) SeriesDate, (0020,000D) StudyInstanceUID]
                            but could not find the following tag(s):
                              [(0008,0021) SeriesDate, (0020,000D) StudyInstanceUID]""".formatted(simple.getString(StudyDescription)));
        }


        @Test
        void doesNotContainValue() {
            assertAttributes()
                    .doesNotContainValue(StudyDate);

            assertThatThrownBy(() -> assertAttributes().doesNotContainValue(StudyInstanceUID))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to not contain a value for tag:
                              (0020,000D) StudyInstanceUID
                            
                            but it does contain a value""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        @Test
        void doesNotContainValueIgnoringTags() {
            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .doesNotContainValue(StudyInstanceUID);
        }

        @Test
        void doesNotContainTags() {
            assertAttributes()
                    .doesNotContainTags(SeriesDate, SeriesTime);

            assertThatThrownBy(() -> assertAttributes().doesNotContainTags(SeriesInstanceUID, StudyTime))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to not contain tags:
                              [(0008,0030) StudyTime, (0020,000E) SeriesInstanceUID]
                            
                            but it does contain the following tag(s):
                              [(0020,000E) SeriesInstanceUID]""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        @Test
        void doesNotContainTagsIgnoringTags() {
            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .doesNotContainTags(StudyInstanceUID);
        }

        @Test
        void doesNotContainValues() {
            assertAttributes()
                    .doesNotContainValues(StudyDate, SeriesDate);

            assertThatThrownBy(() -> assertAttributes().doesNotContainValues(StudyInstanceUID, SeriesDate))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to not contain values for tags:
                              [(0008,0021) SeriesDate, (0020,000D) StudyInstanceUID]
                            
                            but it does contain values for the following element(s):
                              [(0020,000D) StudyInstanceUID]""".formatted(simple.getString(StudyDescription), simple.getString(StudyInstanceUID)));
        }

        @Test
        void isEqualTo() {
            var copy = new Attributes(simple);

            assertAttributes()
                    .isEqualTo(copy);

            var modified = builder(simple)
                    .remove(StudyInstanceUID)
                    .build();

            assertThatThrownBy(() -> assertAttributes().isEqualTo(modified))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to be equal to:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            but was not""".formatted(
                            simple.getString(StudyDescription),
                            simple.getString(StudyInstanceUID),
                            simple.getString(StudyDescription)));
        }

        @Test
        void isEqualToIgnoringTags() {
            var copy = new Attributes(simple);

            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .isEqualTo(copy);

            var modified = builder(simple)
                    .remove(StudyInstanceUID)
                    .build();

            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .isEqualTo(modified);

        }

        @Test
        void isNotEqualTo() {
            var modified = builder(simple)
                    .remove(StudyInstanceUID)
                    .build();

            assertAttributes()
                    .isNotEqualTo(modified);

            var copy = new Attributes(simple);

            assertThatThrownBy(() -> assertAttributes().isNotEqualTo(copy))
                    .hasMessage("""
                            
                            Expecting actual:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            to not be equal to:
                            (0008,1030) LO [%s] StudyDescription
                            (0020,000D) UI [%s] StudyInstanceUID
                            (0020,000E) UI [] SeriesInstanceUID
                            
                            but was equal""".formatted(
                            simple.getString(StudyDescription),
                            simple.getString(StudyInstanceUID),
                            simple.getString(StudyDescription),
                            simple.getString(StudyInstanceUID)));
        }

        @Test
        void hasString() {
            String description = simple.getString(StudyDescription, "");

            assertAttributes()
                    .hasString(StudyDescription, description);

            String wrongDescription = description + "x";
            assertThatThrownBy(() -> assertAttributes()
                    .hasString(StudyDescription, wrongDescription))
                    .hasMessageContaining("to have value '%s' for tag:\n  %s", wrongDescription, "(0008,1030) StudyDescription")
                    .hasMessageContaining("value is different");

            assertThatThrownBy(() -> assertAttributes()
                    .hasString(ReasonForStudy, description))
                    .hasMessageContaining("tag is not present");
        }

        @Test
        void hasStringSatisfying() {
            String description = simple.getString(StudyDescription, "");

            String wrongDescription = description + "x";
            assertAttributes()
                    .hasStringSatisfying(StudyDescription, value -> value.length() == description.length(), "have length " + description.length());

            assertThatThrownBy(() -> assertAttributes()
                    .hasStringSatisfying(StudyDescription, value -> value.length() == wrongDescription.length(), "have length " + wrongDescription.length())
            )
                    .hasMessageContaining("to have length %d for tag:\n  %s", wrongDescription.length(), "(0008,1030) StudyDescription")
                    .hasMessageContaining("value is different");
        }

    }

    @Nested
    class NestedRoot {

        @Test
        void containsTags() {
            assertAttributes()
                    .containsTags(StudyInstanceUID);

            assertThatThrownBy(() -> assertAttributes().containsTags(ReferencedSOPClassUID))
                    .hasMessageContaining("to contain tags:\n" +
                            "  [(0008,1150) ReferencedSOPClassUID]");
        }


        @Test
        void containTagsIgnoringTags() {
            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .containsTags(ReferencedSOPClassUID, SeriesDate))
                    .hasMessageContaining("but could not find the following tag(s):\n" +
                            "  [(0008,0021) SeriesDate, (0008,1150) ReferencedSOPClassUID]");
        }

        @Test
        void containsValue() {
            assertAttributes()
                    .containsValue(StudyInstanceUID);

            assertThatThrownBy(() -> assertAttributes().containsValue(ReferencedSOPClassUID))
                    .hasMessageContaining("to contain a value for tag:\n" +
                            "  (0008,1150) ReferencedSOPClassUID");

        }

        @Test
        void containsValues() {

            assertThatThrownBy(() -> assertAttributes().containsValues(ReferencedSOPClassUID, ReferencedSOPInstanceUID))
                    .hasMessageContaining("to contain values for tags:\n" +
                            "  [(0008,1150) ReferencedSOPClassUID, (0008,1155) ReferencedSOPInstanceUID]");

        }

        @Test
        void doesNotContainValue() {
            assertAttributes()
                    .doesNotContainValue(ReferencedSOPClassUID);
        }

        @Test
        void doesNotContainValues() {
            assertAttributes()
                    .doesNotContainValues(ReferencedSOPClassUID, ReferencedSOPInstanceUID);
        }

        @Test
        void doesNotContainTags() {
            assertAttributes()
                    .doesNotContainTags(ReferencedSOPClassUID, ReferencedSOPInstanceUID);
        }

        @Test
        void doesNotContainTagsIgnoringTags() {
            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .doesNotContainTags(StudyInstanceUID, ReferencedSOPInstanceUID);
        }

        @Test
        void isEqualTo() {
            var copy = new Attributes(nested);

            assertAttributes()
                    .isEqualTo(copy);

            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertAttributes().isEqualTo(modified);
        }

        @Test
        void isEqualToIgnoringTags() {
            var modified = builder(nested)
                    .remove(StudyInstanceUID)
                    .build();

            assertAttributes()
                    .ignoringTags(StudyInstanceUID)
                    .isEqualTo(modified);
        }

        @Test
        void isNotEqualTo() {
            var copy = new Attributes(nested);

            assertThatThrownBy(() -> assertAttributes().isNotEqualTo(copy))
                    .isInstanceOf(AssertionError.class);

            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertThatThrownBy(() -> assertAttributes().isNotEqualTo(modified))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void isNotEqualToIgnoringTags() {
            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .isNotEqualTo(modified)
            )
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void hasNullValue() {
            assertThatThrownBy(() -> assertAttributes()
                    .hasNullValue(LUTExplanation)
            )
                    .hasMessageContaining("but tag is not present");

            assertAttributes()
                    .hasNullValue(SOPInstanceUID);
        }

        private RootOnlyAttributesAssert assertAttributes() {
            return assertThat(nested)
                    .usingRootOnlyComparison();
        }
    }

    @Nested
    class NestedRecursive {


        @Test
        void containsTags() {
            assertAttributes()
                    .containsTags(ReferencedSOPClassUID);
        }

        @Test
        void containTagsIgnoringTags() {
            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .containsTags(ReferencedSOPClassUID, ReferencedSOPInstanceUID)
            )
                    .hasMessageContaining("but could not find the following tag(s):\n" +
                            "  [(0008,1150) ReferencedSOPClassUID]");
        }

        @Test
        void containsValue() {
            assertAttributes()
                    .containsValue(ReferencedSOPClassUID);
        }

        @Test
        void containsValues() {
            assertAttributes()
                    .containsValues(ReferencedSOPClassUID, ReferencedSOPInstanceUID);
        }

        @Test
        void doesNotContainTags() {

            assertThatThrownBy(() -> assertAttributes().doesNotContainTags(ReferencedSOPClassUID))
                    .hasMessageContaining("""
                            to not contain tags:
                              [(0008,1150) ReferencedSOPClassUID]
                            
                            but it does contain the following tag(s):
                              [(0008,1150) ReferencedSOPClassUID]""");
        }

        @Test
        void doesNotContainTagsIgnoringTags() {
            assertAttributes()
                    .ignoringTags(ReferencedSOPInstanceUID)
                    .doesNotContainTags(ReferencedSOPInstanceUID);
        }

        @Test
        void doesNotContainValue() {
            assertThatThrownBy(() -> assertAttributes()
                    .doesNotContainValue(ReferencedSOPClassUID))
                    .hasMessageContaining("to not contain a value for tag:\n" +
                            "  (0008,1150) ReferencedSOPClassUID");
        }

        @Test
        void doesNotContainValues() {
            assertThatThrownBy(() -> assertAttributes()
                    .doesNotContainValues(ReferencedSOPClassUID, ReferencedSOPInstanceUID))
                    .hasMessageContaining("to not contain values for tags:\n" +
                            "  [(0008,1150) ReferencedSOPClassUID, (0008,1155) ReferencedSOPInstanceUID]");
        }


        @Test
        void isEqualTo() {
            var copy = new Attributes(nested);

            assertAttributes()
                    .isEqualTo(copy);

            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertThatThrownBy(() -> assertAttributes().isEqualTo(modified))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void isEqualToIgnoringTags() {
            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .isEqualTo(modified);
        }

        @Test
        void isNotEqualTo() {
            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertAttributes()
                    .isNotEqualTo(modified);

            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .isNotEqualTo(new Attributes(nested))
            )
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void isNotEqualToIgnoringTags() {
            var modified = copyNestedAndRemoveNestedReferencedSOPClassUID();

            assertThatThrownBy(() -> assertAttributes()
                    .ignoringTags(ReferencedSOPClassUID)
                    .isNotEqualTo(modified)
            )
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void hasNullValue() {
            assertAttributes()
                    .hasNullValue(LUTExplanation);
            assertAttributes()
                    .hasNullValue(SOPInstanceUID);
        }

        private RecursiveAttributesAssert assertAttributes() {
            return assertThat(nested);
        }
    }

}
