package io.github.damienvdb.dcm4junit.dicom;

import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.DateUtils;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static org.dcm4che3.data.ElementDictionary.getStandardElementDictionary;

@RequiredArgsConstructor
public class AttributesBuilder {

    private final Attributes attributes;

    public static AttributesBuilder builder() {
        return new AttributesBuilder(new Attributes());
    }

    public static AttributesBuilder builder(Attributes attributes) {
        return new AttributesBuilder(new Attributes(attributes));
    }

    private static VR vrOf(int tag) {
        return getStandardElementDictionary().vrOf(tag);
    }


    public AttributesBuilder addItems(int tag, Attributes... items) {
        Sequence seq = attributes.ensureSequence(tag, items.length);
        for (Attributes item : items) {
            seq.add(new Attributes(item));
        }
        return this;
    }

    public AttributesBuilder remove(int tag) {
        attributes.remove(tag);
        return this;
    }

    public AttributesBuilder remove(ItemPointer[] itemPointers, int tag) {
        attributes.getNestedDataset(itemPointers).remove(tag);
        return this;
    }

    public AttributesBuilder setString(int tag, String value) {
        attributes.setString(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setInt(int tag, int... value) {
        attributes.setInt(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setNull(int tag) {
        attributes.setNull(tag, vrOf(tag));
        return this;
    }

    public AttributesBuilder setDateTime(int dateTag, int timeTag, Instant instant) {
        Date date = Date.from(instant);
        String dateValue = DateUtils.formatDA(TimeZone.getDefault(), date);
        String timeValue = DateUtils.formatTM(TimeZone.getDefault(), date);
        this.setString(dateTag, dateValue);
        this.setString(timeTag, timeValue);
        return this;
    }

    public AttributesBuilder setReadOnly() {
        attributes.setReadOnly();
        return this;
    }

    public Attributes build() {
        return attributes;
    }
}
