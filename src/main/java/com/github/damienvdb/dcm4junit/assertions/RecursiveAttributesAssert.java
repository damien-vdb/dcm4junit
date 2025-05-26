package com.github.damienvdb.dcm4junit.assertions;

import com.github.damienvdb.dcm4junit.utils.AttributesUtils;
import org.dcm4che3.data.Attributes;

public class RecursiveAttributesAssert extends AttributesAssert<RecursiveAttributesAssert> {

    public RecursiveAttributesAssert(Attributes attributes, int[] tagsToIgnore) {
        super(attributes, RecursiveAttributesAssert.class, tagsToIgnore);
    }

    public RootOnlyAttributesAssert usingRootOnlyComparison() {
        return new RootOnlyAttributesAssert(actual, tagsToIgnore);
    }

    protected Attributes getAttributes(Attributes attributes, int[] tagsToIgnore) {
        return AttributesUtils.copyIgnoringTags(attributes, tagsToIgnore);
    }


    protected boolean isRecursive() {
        return true;
    }

}
