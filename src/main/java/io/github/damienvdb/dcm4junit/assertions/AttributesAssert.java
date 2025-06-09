package io.github.damienvdb.dcm4junit.assertions;

import io.github.damienvdb.dcm4junit.dicom.DicomField;
import io.github.damienvdb.dcm4junit.dicom.DicomTag;
import io.github.damienvdb.dcm4junit.utils.AttributesUtils;
import io.github.damienvdb.dcm4junit.utils.visitor.SearchAllVisitor;
import io.github.damienvdb.dcm4junit.utils.visitor.SearchOneVisitor;
import lombok.SneakyThrows;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Value;

import java.util.Arrays;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * TODO:
 * contains(DicomField... fields)
 *
 * @param <SELF>
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class AttributesAssert<SELF extends AttributesAssert<SELF>> {

    final Attributes actual;
    final SELF myself;
    int[] tagsToIgnore;

    public AttributesAssert(Attributes actual, Class<SELF> selfType, int[] tagsToIgnore) {
        this.actual = actual;
        this.myself = selfType.cast(this);
        this.setTagsToIgnore(tagsToIgnore);
    }

    public SELF ignoringTags(int... tags) {
        this.setTagsToIgnore(tags);
        return myself;
    }

    private void setTagsToIgnore(int[] tags) {
        if (tags != null) {
            Arrays.sort(tags);
            this.tagsToIgnore = tags;
        } else {
            this.tagsToIgnore = new int[0];
        }
    }

    @SneakyThrows
    protected Attributes visitAttributes(Attributes.Visitor visitor) {
        Attributes attributes = getAttributes();
        attributes.accept(visitor, isRecursive());
        return attributes;
    }

    protected abstract boolean isRecursive();


    public SELF contains(DicomField field) {
        SearchOneVisitor visitor = new SearchOneVisitor(field);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.isFound()) {
            var reason = visitor.isTagExists() ? "it does not" : "tag is not present";
            fail("%nExpecting actual:%n%s%nto contain '" + field + "'%n%nbut " + reason, attributes);
        }
        return myself;

    }

    /**
     * Asserts that the attributes contain all of the specified tags.
     * The order of the tags does not matter.
     * Values are not checked.
     *
     * @param tags the tags to check for presence
     * @return {@code this} assertion object.
     * @throws AssertionError if any of the tags are not found in the attributes
     */
    public SELF containsTags(int... tags) {
        SortedSet<DicomTag> expectedTags = DicomTag.of(tags);
        SearchAllVisitor visitor = new SearchAllVisitor(expectedTags::contains);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.getFound().equals(expectedTags)) {
            TreeSet<DicomTag> missing = new TreeSet<>(expectedTags);
            missing.removeAll(visitor.getFound());
            fail("%nExpecting actual"
                    + ":%n%s%nto contain tags:%n  %s%nbut could not find the following tag(s)"
                    + ":%n  %s", attributes, expectedTags, missing);
        }
        return myself;
    }

    /**
     * Asserts that the attributes contain a non-null value for the specified tag.
     *
     * @param tag the tag to check for a value
     * @return {@code this} assertion object.
     * @throws AssertionError if the attributes do not contain the specified tag
     *                        or the value for the tag is null
     */
    public SELF containsValue(int tag) {
        DicomTag expectedTag = new DicomTag(tag);
        SearchOneVisitor visitor = new SearchOneVisitor(expectedTag, Objects::nonNull);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.isFound()) {
            var reason = visitor.isTagExists() ? "value is null" : "tag is not present";
            fail("%nExpecting actual:%n%s%nto contain a value for tag:%n  %s%n%nbut " + reason, attributes, new DicomTag(tag));
        }
        return myself;
    }


    /**
     * Asserts that the attributes contain a non-null value for all of the specified tags.
     * The order of the tags does not matter.
     *
     * @param tags the tags to check for a non-null value
     * @return {@code this} assertion object.
     * @throws AssertionError if any of the tags are not found in the attributes
     *                        or the value for the tag is null
     */
    public SELF containsValues(int... tags) {
        SortedSet<DicomTag> expectedTags = DicomTag.of(tags);
        SearchAllVisitor visitor = new SearchAllVisitor(expectedTags::contains, Objects::nonNull);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.getFound().equals(expectedTags)) {
            TreeSet<DicomTag> missing = new TreeSet<>(expectedTags);
            missing.removeAll(visitor.getFound());
            fail("%nExpecting actual"
                    + ":%n%s%nto contain values for tags:%n  %s%nbut could not find the following element(s)"
                    + ":%n  %s", attributes, expectedTags, missing);
        }
        return myself;
    }

    /**
     * Asserts that the attributes do not contain a non-null value for the specified tag.
     *
     * @param tag the tag to check for a value
     * @throws AssertionError if the attributes contain the specified tag with a non-null value
     */
    public SELF doesNotContainValue(int tag) {
        DicomTag expectedTag = new DicomTag(tag);
        SearchOneVisitor visitor = new SearchOneVisitor(expectedTag, Objects::nonNull);
        Attributes attributes = visitAttributes(visitor);
        if (visitor.isFound()) {
            fail("%nExpecting actual:%n%s%nto not contain a value for tag:%n  %s%n%nbut it does contain a value", attributes, expectedTag);
        }
        return myself;
    }

    /**
     * Asserts that the attributes do not contain a non-null value for any of the specified tags.
     * The order of the tags does not matter.
     *
     * @param tags the tags to check for absence of a non-null value
     * @throws AssertionError if any of the tags are found in the attributes with a non-null value
     */
    public SELF doesNotContainValues(int... tags) {
        SortedSet<DicomTag> expectedTags = DicomTag.of(tags);
        SearchAllVisitor visitor = new SearchAllVisitor(expectedTags::contains, Objects::nonNull);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.getFound().isEmpty()) {
            fail("%nExpecting actual"
                    + ":%n%s%nto not contain values for tags:%n  %s%n%nbut it does contain values for the following element(s)"
                    + ":%n  %s", attributes, expectedTags, visitor.getFound());
        }
        return myself;
    }

    /**
     * Asserts that the attributes do not contain any of the specified tags.
     * The order of the tags does not matter.
     *
     * @param tags the tags to check for absence
     * @throws AssertionError if any of the tags are found in the attributes
     */
    public SELF doesNotContainTags(int... tags) {
        SortedSet<DicomTag> expectedTags = DicomTag.of(tags);
        SearchAllVisitor visitor = new SearchAllVisitor(expectedTags::contains);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.getFound().isEmpty()) {
            fail("%nExpecting actual"
                    + ":%n%s%nto not contain tags:%n  %s%n%nbut it does contain the following tag(s)"
                    + ":%n  %s", attributes, expectedTags, visitor.getFound());
        }
        return myself;
    }


    /**
     * Asserts that the attributes are equal to the given other attributes.
     * They should have the same size, tags, and values.
     *
     * @param other the attributes to compare with
     * @throws AssertionError if the attributes are not equal to the given other attributes
     */
    public SELF isEqualTo(Attributes other) {
        Attributes attributes = getAttributes();
        Attributes other1 = getAttributes(AttributesUtils.copyIgnoringTags(other, tagsToIgnore), tagsToIgnore);
        if (!attributes.equals(other1)) {
            fail("%nExpecting actual:%n%s%nto be equal to:%n%s%nbut was not", attributes, other1);
        }
        return myself;
    }

    /**
     * Asserts that the attributes are not equal to the given other attributes.
     * They should differ in size, tags, or values.
     *
     * @param other the attributes to compare with
     * @throws AssertionError if the attributes are equal to the given other attributes
     */
    public SELF isNotEqualTo(Attributes other) {
        Attributes attributes = getAttributes();
        Attributes other1 = getAttributes(AttributesUtils.copyIgnoringTags(other, tagsToIgnore), tagsToIgnore);
        if (attributes.equals(other1)) {
            fail("%nExpecting actual:%n%s%nto not be equal to:%n%s%nbut was equal", attributes, other1);
        }
        return myself;
    }

    public SELF hasNullValue(int tag) {
        DicomTag expectedTag = new DicomTag(tag);
        SearchOneVisitor visitor = new SearchOneVisitor(expectedTag, v -> v == null || v == Value.NULL);
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.isFound()) {
            var reason = visitor.isTagExists() ? "value is not null" : "tag is not present";
            fail("%nExpecting actual:%n%s%nto contain a null value for tag:%n  %s%n%nbut " + reason, attributes, expectedTag);
        }
        return myself;
    }

    /**
     * Asserts that the attributes contain a string value for the specified tag
     * equal to the given value.
     *
     * @param tag   the tag to check for a string value
     * @param value the expected string value
     * @return {@code this} assertion object.
     * @throws AssertionError if the tag is not found in the attributes or the
     *                        value is not equal to the given value
     */
    public SELF hasString(int tag, String value) {
        return hasStringSatisfying(tag,
                (actual) -> Objects.equals(actual, value),
                "have value '" + value + "'"
        );
    }

    /**
     * Asserts that the attributes contain a string value for the specified tag
     * that satisfies the given predicate.
     *
     * @param tag         the tag to check for a string value
     * @param predicate   the predicate to apply to the string value
     * @param expectation a description of what the predicate checks
     * @return {@code this} assertion object.
     * @throws AssertionError if the tag is not found in the attributes or the
     *                        value does not satisfy the predicate
     */
    public SELF hasStringSatisfying(int tag, Predicate<String> predicate, String expectation) {
        DicomTag expectedTag = new DicomTag(tag);
        SearchOneVisitor visitor = new SearchOneVisitor(expectedTag,
                (attrs, field) -> {
                    if (!field.vr().isStringType()) {
                        return false;
                    }
                    String actual = attrs.getString(field.tag().getTag());
                    return predicate.test(actual);
                }
        );
        Attributes attributes = visitAttributes(visitor);
        if (!visitor.isFound()) {
            var reason = visitor.isTagExists() ? "value is different" : "tag is not present";
            fail("%nExpecting actual:%n%s%nto " + expectation + " for tag:%n  %s%n%nbut " + reason, attributes, new DicomTag(tag));
        }
        return myself;
    }

    private void fail(String message, Object... arguments) {
        throw new AssertionError(String.format(message, arguments));
    }

    protected Attributes getAttributes() {
        return getAttributes(actual, tagsToIgnore);
    }

    protected abstract Attributes getAttributes(Attributes attributes, int[] tagsToIgnore);

}
