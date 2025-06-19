package com.github.damienvdb.dcm4junit.dimse.jupiter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CStoreScp {

    boolean enabled() default true;
}
