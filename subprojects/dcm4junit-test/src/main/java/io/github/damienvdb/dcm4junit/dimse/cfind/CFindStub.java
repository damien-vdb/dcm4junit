package io.github.damienvdb.dcm4junit.dimse.cfind;

import io.github.damienvdb.dcm4junit.dimse.Stub;
import lombok.experimental.SuperBuilder;
import org.dcm4che3.data.Attributes;

@SuperBuilder
class CFindStub extends Stub<Attributes> {

    public static abstract class CFindStubBuilder<C extends CFindStub, B extends CFindStub.CFindStubBuilder<C, B>>
            extends Stub.StubBuilder<Attributes, C, B> {
    }
}
