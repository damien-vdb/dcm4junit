package io.github.damienvdb.dcm4junit.dicom;

import io.github.damienvdb.dcm4junit.utils.AttributesUtils;
import lombok.RequiredArgsConstructor;
import org.dcm4che3.data.*;

import java.util.Calendar;
import java.util.Date;

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

    public AttributesBuilder remove(String path) {
        ItemPointer[] pointers = AttributesUtils.pointers(path);
        ItemPointer lastPointer = pointers[pointers.length - 1];
        Attributes targetAttributes;
        if (pointers.length > 1) {
            ItemPointer[] parents = new ItemPointer[pointers.length - 1];
            System.arraycopy(pointers, 0, parents, 0, parents.length);
            targetAttributes = attributes.getNestedDataset(parents);
        } else {
            targetAttributes = attributes;
        }
        targetAttributes
                .remove(lastPointer.privateCreator, lastPointer.sequenceTag);
        return this;
    }

    public AttributesBuilder remove(ItemPointer[] itemPointers, int tag) {
        attributes.getNestedDataset(itemPointers).remove(tag);
        return this;
    }

    public AttributesBuilder setBytes(int tag, byte[] value) {
        attributes.setBytes(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setString(int tag, String... value) {
        attributes.setString(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setDouble(int tag, double... value) {
        attributes.setDouble(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setFloat(int tag, float... value) {
        attributes.setFloat(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setInt(int tag, int... value) {
        attributes.setInt(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setLong(int tag, long... value) {
        attributes.setLong(tag, vrOf(tag), value);
        return this;
    }

    public AttributesBuilder setNull(int tag) {
        attributes.setNull(tag, vrOf(tag));
        return this;
    }

    public AttributesBuilder setDateTime(long dateTimeTag, Date date) {
        return setDateTime(dateTimeTag, date, Calendar.SECOND);
    }

    public AttributesBuilder setDateTime(long dateTimeTag, Date date, int calendarPrecision) {
        attributes.setDate(dateTimeTag, new DatePrecision(calendarPrecision), date);
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
