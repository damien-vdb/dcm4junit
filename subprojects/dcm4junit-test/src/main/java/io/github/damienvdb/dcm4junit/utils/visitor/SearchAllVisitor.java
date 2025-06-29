package io.github.damienvdb.dcm4junit.utils.visitor;

import io.github.damienvdb.dcm4junit.dicom.DicomField;
import io.github.damienvdb.dcm4junit.dicom.DicomTag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class SearchAllVisitor implements Attributes.Visitor {

    private final Predicate<DicomTag> tagPredicate;
    private final Predicate<VR> vrPredicate;
    private final Predicate<Object> valuePredicate;
    @Getter
    private final SortedSet<DicomTag> existingTags = new TreeSet<>();
    @Getter
    private final SortedSet<DicomTag> found = new TreeSet<>();

    public SearchAllVisitor(Predicate<DicomTag> tagPredicate) {
        this(tagPredicate, $ -> true);
    }

    public SearchAllVisitor(Predicate<DicomTag> tagPredicate, Predicate<Object> valuePredicate) {
        this(tagPredicate, $ -> true, valuePredicate);
    }

    @Override
    public boolean visit(Attributes attrs, int tag, VR vr, Object value) {
        DicomTag dicomTag = new DicomTag(tag);
        DicomField field = new DicomField(dicomTag, vr, value);
        if (tagPredicate.test(field.tag())) {
            existingTags.add(dicomTag);
            if (vrPredicate.test(field.vr()) && valuePredicate.test(field.value())) {
                found.add(dicomTag);
            }
        }
        return true;
    }
}
