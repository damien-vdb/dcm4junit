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

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static com.github.damienvdb.dcm4junit.dimse.MockFindScpRegistry.Stub;
import static java.util.function.Predicate.isEqual;

/**
 * TODO: Map a behavior (delays, errors) by expected keys
 * TODO: Requests verification
 */
@Slf4j
public class MockFindScp extends BasicCFindSCP {

    private final MockFindScpRegistry registry = new MockFindScpRegistry();

    public MockFindScp(CFindScp cfindScp) {
        super(cfindScp.sopClasses());
    }

    public OngoingCFindStub stubFor(Attributes keys) {
        return new OngoingCFindStub(Stub.builder().expectedKeys(isEqual(keys)), registry);
    }

    /**
     * Initiate a stub from a predicate on C-FIND dataset keys.
     *
     * @param predicate Predicate on expected keys
     * @return ongoing stub
     * @see com.github.damienvdb.dcm4junit.assertions.AttributesAssert#toPredicate(java.util.function.Function) to generate predicates from assertions.
     */
    public OngoingCFindStub stubFor(Predicate<Attributes> predicate) {
        return new OngoingCFindStub(Stub.builder().expectedKeys(predicate), registry);
    }


    @Override
    protected QueryTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException {
        if (this.registry.isEmpty()) {
            throw new DicomServiceException(Status.UnableToProcess, "No ongoing stub");
        }
        List<Attributes> datasets = this.registry.get(rq, keys);
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
