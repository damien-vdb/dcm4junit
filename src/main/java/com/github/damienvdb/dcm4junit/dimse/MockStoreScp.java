package com.github.damienvdb.dcm4junit.dimse;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Builder
public class MockStoreScp extends BasicCStoreSCP {

    private final int status;
    private final File storageDir;
    private final LinkedHashMap<Attributes, File> storedFiles = new LinkedHashMap<>();


    public List<File> getStoredFiles() {
        return new ArrayList<>(this.storedFiles.values());
    }

    public List<Attributes> getFmis() {
        return new ArrayList<>(this.storedFiles.keySet());
    }


    @Override
    protected void store(Association as, PresentationContext pc,
                         Attributes rq, PDVInputStream data, Attributes rsp)
            throws IOException {
        try {
            rsp.setInt(Tag.Status, VR.US, status);
            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
            String tsuid = pc.getTransferSyntax();
            Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
            File stored = storeTo(fmi, data);
            storedFiles.put(fmi, stored);
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    private File storeTo(Attributes fmi, PDVInputStream data) throws IOException {
        File target = File.createTempFile(fmi.getString(Tag.MediaStorageSOPInstanceUID), ".dcm", storageDir);
        target.deleteOnExit();
        File tmp = new File(target.getPath() + ".tmp");
        tmp.deleteOnExit();
        tmp.getParentFile().mkdirs();
        try (var out = new DicomOutputStream(tmp)) {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
        tmp.renameTo(target);
        return target;
    }

    public void clear() {
        this.storedFiles.values().forEach(File::delete);
        this.storedFiles.clear();
    }
}
