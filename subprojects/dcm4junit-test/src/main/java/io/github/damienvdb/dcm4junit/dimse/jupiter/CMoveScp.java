package io.github.damienvdb.dcm4junit.dimse.jupiter;

import org.dcm4che3.data.UID;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface CMoveScp {

    boolean enabled() default true;

    String[] sopClasses() default {
            UID.StudyRootQueryRetrieveInformationModelMove,
            UID.PatientRootQueryRetrieveInformationModelMove,
            UID.PatientStudyOnlyQueryRetrieveInformationModelMove
    };
}
