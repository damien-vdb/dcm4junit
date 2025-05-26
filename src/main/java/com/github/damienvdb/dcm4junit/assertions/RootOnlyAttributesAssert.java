package com.github.damienvdb.dcm4junit.assertions;

import com.github.damienvdb.dcm4junit.utils.AttributesUtils;
import lombok.SneakyThrows;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;

public class RootOnlyAttributesAssert extends AttributesAssert<RootOnlyAttributesAssert> {

    public RootOnlyAttributesAssert(Attributes attributes, int[] tagsToIgnore) {
        super(attributes, RootOnlyAttributesAssert.class, tagsToIgnore);
    }

    public RecursiveAttributesAssert usingRecursiveComparison() {
        return new RecursiveAttributesAssert(actual, tagsToIgnore);
    }

    @Override
    protected boolean isRecursive() {
        return false;
    }

    @SneakyThrows
    protected Attributes getAttributes(Attributes attributes, int[] tagsToIgnore) {
        Attributes root = AttributesUtils.copyIgnoringTags(attributes, tagsToIgnore);
        root.accept((attrs, tag, vr, value) -> {
            if (value instanceof Sequence) {
                ((Sequence) value).clear();
            }
            return true;
        }, false);

        return root;
    }
}
