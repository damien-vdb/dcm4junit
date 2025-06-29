package io.github.damienvdb.dcm4junit.dimse;

import lombok.Getter;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationMonitor;
import org.dcm4che3.net.pdu.AAssociateRJ;

import java.util.ArrayList;
import java.util.List;

@Getter
class FakeAssociationMonitor implements AssociationMonitor {

    private final List<Association> associationsEstablished = new ArrayList<>();
    private final List<Association> associationsFailed = new ArrayList<>();
    private final List<Association> associationsRejected = new ArrayList<>();
    private final List<Association> associationsAccepted = new ArrayList<>();

    public void clear() {
        associationsEstablished.clear();
        associationsFailed.clear();
        associationsRejected.clear();
        associationsAccepted.clear();
    }

    @Override
    public void onAssociationEstablished(Association as) {
        associationsEstablished.add(as);
    }

    @Override
    public void onAssociationFailed(Association as, Throwable e) {
        associationsFailed.add(as);
    }

    @Override
    public void onAssociationRejected(Association as, AAssociateRJ aarj) {
        associationsRejected.add(as);
    }

    @Override
    public void onAssociationAccepted(Association as) {
        associationsAccepted.add(as);
    }
}
