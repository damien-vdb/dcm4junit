package io.github.damienvdb.dcm4junit.dimse.cmove;

import io.github.damienvdb.dcm4junit.dimse.Stub;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

@SuperBuilder
public class CMoveStub extends Stub<Path> {

    @Getter
    private final String aem;
    @Getter
    private final String hostname;
    @Getter
    private final int port;
}
