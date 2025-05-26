package com.github.damienvdb.dcm4junit.utils.visitor;

import com.github.damienvdb.dcm4junit.dicom.DicomField;
import com.github.damienvdb.dcm4junit.dicom.DicomTag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class SearchOneVisitor implements Attributes.Visitor {

    private final DicomTag expectedTag;
    private final BiPredicate<Attributes, DicomField> fieldPredicate;
    @Getter
    private boolean tagExists = false;
    @Getter
    private boolean found = false;

    public SearchOneVisitor(DicomTag expectedTag, Predicate<Object> valuePredicate) {
        this(expectedTag, (attrs, field) -> valuePredicate.test(field.value()));
    }

    public SearchOneVisitor(DicomField expectedField) {
        this(expectedField.tag(), (attrs, field) -> expectedField.equals(field));
    }

    @Override
    public boolean visit(Attributes attrs, int tag, VR vr, Object value) {
        DicomField field = new DicomField(new DicomTag(tag), vr, value);
        if (!expectedTag.equals(field.tag())) {
            return true;
        }
        tagExists = true;
        if (fieldPredicate.test(attrs, field)) {
            found = true;
            return false;
        }
        return true;
    }
}
