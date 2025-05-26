package com.github.damienvdb.dcm4junit.assertions;

import org.dcm4che3.data.Attributes;

public class Assertions {

    /**
     * Make assertions on the given attributes.
     * The assertions are configured to be recursive by default.
     *
     * @param attributes the attributes to make assertions on
     * @return a {@link RecursiveAttributesAssert} object
     */
    public static RecursiveAttributesAssert assertThat(Attributes attributes) {
        return new RecursiveAttributesAssert(attributes, new int[0]);
    }

}
