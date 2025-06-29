package io.github.damienvdb.dcm4junit.utils;

import lombok.SneakyThrows;
import org.dcm4che3.data.*;

public class AttributesUtils {
    @SneakyThrows
    public static Attributes copyIgnoringTags(Attributes attributes, int[] tagsToIgnore) {
        if (tagsToIgnore == null || tagsToIgnore.length == 0) {
            return new Attributes(attributes);
        }
        var result = new Attributes(attributes.bigEndian(), attributes.size());
        result.addNotSelected(attributes, tagsToIgnore);
        result.accept(new RemoveTagsSequenceVisitor(tagsToIgnore), true);
        return result;
    }

    /**
     * Create a sequence of ItemPointers from a DICOM path string.
     * <p>
     * Examples of DICOM paths:
     * - "DicomAttribute[@tag="00200010"]" - Points to a single attribute (Patient ID)
     * - DicomAttribute[@tag="00400275"]/Item[@number="1"]/DicomAttribute[@tag="00400007"] - Points to the first item of a sequence and then to an attribute
     * - DicomAttribute[@tag="00410275" and @privateCreator="XXX"]" - Points to a private tag
     *
     * @param path The DICOM path string following the format above
     * @return An array of ItemPointers representing the path
     * @throws IllegalArgumentException if the path is invalid
     */
    public static ItemPointer[] pointers(String path) {
        AttributeSelector selector = AttributeSelector.valueOf(path);
        return extractPointers(selector);
    }

    public static ItemPointer[] extractPointers(AttributeSelector selector) {
        ItemPointer[] itemPointers = new ItemPointer[selector.level() + 1];
        for (int i = 0; i < selector.level(); i++) {
            itemPointers[i] = selector.itemPointer(i);
        }
        itemPointers[itemPointers.length - 1] = new ItemPointer(selector.privateCreator(), selector.tag());
        return itemPointers;
    }

    public static ItemPointer at(int sequenceTag, int index) {
        return new ItemPointer(sequenceTag, index);
    }

    /**
     * Selects a string value from the given attributes based on a DICOM path.
     *
     * @param path       The DICOM path string following the format of {@link #pointers(String)}
     * @param attributes The attributes to select the string value from
     * @return The selected string value
     */
    public static String selectString(String path, Attributes attributes) {
        return selectString(path, attributes, 0);
    }

    /**
     * Selects a string value from the given attributes based on a DICOM path.
     *
     * @param path       The DICOM path string following the format of {@link #pointers(String)}
     * @param attributes The attributes to select the string value from
     * @param index      The string value index (useful for tags with value multiplicity 2-n)
     * @return The selected string value
     */
    public static String selectString(String path, Attributes attributes, int index) {
        return new ValueSelector(AttributeSelector.valueOf(path), index).selectStringValue(attributes, null);
    }

    private static class RemoveTagsSequenceVisitor implements Attributes.SequenceVisitor {

        private final int[] tagsToRemove;
        private Attributes previousAttributes = null;

        public RemoveTagsSequenceVisitor(int[] tagsToRemove) {
            this.tagsToRemove = tagsToRemove;
        }

        @Override
        public void startItem(int sqTag, int itemIndex) {

        }

        @Override
        public void endItem() {
            if (previousAttributes != null) {
                for (int tag : tagsToRemove) {
                    previousAttributes.remove(tag);
                }
            }

        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
            previousAttributes = attrs;
            return true;
        }
    }
}
