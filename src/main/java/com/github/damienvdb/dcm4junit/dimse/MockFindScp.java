package com.github.damienvdb.dcm4junit.dimse;

import com.github.damienvdb.dcm4junit.dimse.jupiter.CFindScp;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCFindSCP;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryTask;

import java.util.*;

/**
 * TODO: Map a behavior (delays, errors) by expected keys
 * TODO: Expected keys matchers
 * TODO: Requests verification
 */
@Slf4j
public class MockFindScp extends BasicCFindSCP {

    private final Map<Attributes, List<Attributes>> registry = new HashMap<>();

    public MockFindScp(CFindScp cfindScp) {
        super(cfindScp.sopClasses());
    }

    public OngoingCFindStub stubFor(Attributes keys) {
        return new OngoingCFindStub(keys, registry);
    }

    @Override
    protected QueryTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException {
        if (this.registry.isEmpty()) {
            throw new DicomServiceException(Status.UnableToProcess, "No ongoing stub");
        }
        List<Attributes> datasets = this.registry.getOrDefault(keys, Collections.emptyList());
        Iterator<Attributes> iterator = datasets.iterator();

        return new BasicQueryTask(as, pc, rq, keys) {
            @Override
            protected Attributes nextMatch() {
                return iterator.next();
            }

            @Override
            protected boolean hasMoreMatches() {
                return iterator.hasNext();
            }
        };
    }

    public void clear() {
        this.registry.clear();
    }
}
