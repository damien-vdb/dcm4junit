package com.github.damienvdb.dcm4junit.dimse;

import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OngoingCFindStub {

    private final Attributes expectedKeys;
    private final Map<Attributes, List<Attributes>> registry;

    public void willReturn(Attributes... datasets) {
        this.registry.put(expectedKeys, Optional.ofNullable(datasets)
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toList()));
    }
}
