package com.github.damienvdb.dcm4junit.dicom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.util.TagUtils;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DicomTag implements Comparable<DicomTag> {

    @Getter
    @EqualsAndHashCode.Include
    private final int tag;
    @Getter
    private final String keyword;

    public DicomTag(int tag) {
        this.tag = tag;
        this.keyword = ElementDictionary.keywordOf(tag, "");
    }

    public static TreeSet<DicomTag> of(int[] tags) {
        return Arrays.stream(tags).mapToObj(DicomTag::new).collect(Collectors.toCollection(TreeSet::new));
    }


    @Override
    public String toString() {
        return TagUtils.toString(tag) + " " + (keyword.isEmpty() ? "?" : keyword);
    }

    @Override
    public int compareTo(DicomTag o) {
        return Integer.compare(tag, o.tag);
    }
}
