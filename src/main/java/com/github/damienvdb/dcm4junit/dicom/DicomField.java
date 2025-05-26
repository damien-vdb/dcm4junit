package com.github.damienvdb.dcm4junit.dicom;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

@RequiredArgsConstructor
@EqualsAndHashCode
public final class DicomField {
    private final DicomTag tag;
    private final VR vr;
    private final Object value;

    @Override
    public String toString() {
        return String.format("%s %s [%s] %s", TagUtils.toString(tag.getTag()), vr, value, (tag.getKeyword().isEmpty() ? "?" : tag.getKeyword()));
    }

    public DicomTag tag() {
        return tag;
    }

    public VR vr() {
        return vr;
    }

    public Object value() {
        return value;
    }

}
