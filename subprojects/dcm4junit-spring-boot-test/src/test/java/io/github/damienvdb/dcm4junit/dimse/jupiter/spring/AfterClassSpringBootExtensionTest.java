package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static io.github.damienvdb.dcm4junit.dicom.AttributesBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableDimseMock(value =
@DimseMockSettings(
        name = "AFTER_CLASS",
        aet = "MOCK"
)
)
@SpringBootTest
class AfterClassSpringBootExtensionTest {

    @AutowiredDimseMock("AFTER_CLASS")
    DimseMock dimseMock;

    @Execution(ExecutionMode.SAME_THREAD)
    @RepeatedTest(100)
    void repeatedTest() {
        assertThat(dimseMock.getAeTitle()).isEqualTo("MOCK");
        assertThat(dimseMock.getHostname()).isEqualTo("localhost");
        assertThat(dimseMock.getPort()).isGreaterThanOrEqualTo(4100);
        assertThat(dimseMock.getCFindScp().countStubs()).isEqualTo(0);
        assertThat(dimseMock.isStarted()).isTrue();

        dimseMock.getCFindScp().stubFor(builder().build()).willReturn(builder().build());
    }
}
