package com.github.damienvdb.dcm4junit.utils;

import lombok.SneakyThrows;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;

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

    public static ItemPointer at(int sequenceTag, int index) {
        return new ItemPointer(sequenceTag, index);
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
