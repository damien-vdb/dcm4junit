package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DimseMockSpringBootExtension.class)
public @interface EnableDimseMock {

    DimseMockSettings value() default @DimseMockSettings;
}
