package io.github.damienvdb.dcm4junit.dimse.jupiter;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ExtendWith(DimseMockExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DimseMockSettings {

    String name() default "dimsemock";

    boolean perTestSuite() default false;

    String hostname() default "localhost";

    int port() default 0;

    String aet() default "storescp";

    CStoreScp cstoreScp() default @CStoreScp;

    CFindScp cfindScp() default @CFindScp;

    CMoveScp cmoveScp() default @CMoveScp;
}
