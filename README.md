# DCM4JUnit

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Build Status](https://github.com/damien-vdb/dcm4junit/actions/workflows/build.yml/badge.svg?branch=main)
[![javadoc](https://javadoc.io/badge2/io.github.damien-vdb/dcm4junit-test/javadoc.svg?color=blue&label=javadoc%20dcm4junit-test)](https://javadoc.io/doc/io.github.damien-vdb/dcm4junit-test)
[![javadoc](https://javadoc.io/badge2/io.github.damien-vdb/dcm4junit-spring-boot-test/javadoc.svg?color=blue&label=javadoc%20dcm4junit-spring-boot-test)](https://javadoc.io/doc/io.github.damien-vdb/dcm4junit-spring-boot-test)

DCM4JUnit is a testing library that provides utilities and extensions for testing DICOM applications using JUnit 5. It's
built on top of the dcm4che library and provides a simple way to mock DICOM services and verify DICOM operations in your
tests.

## Features

- **DIMSE Mock Server**: Easily create a mock DICOM SCP for testing DICOM network operations (C-FIND, C-MOVE, C-STORE).
- **DICOM Assertions**: Rich set of assertions for verifying DICOM attributes and datasets
- **Test Data Generation**: Generate test DICOM data with customizable attributes
- **JUnit 5 Integration**: Seamless integration with JUnit 5's extension model
- **DICOM Tag Utilities**: Helper utilities for working with DICOM tags and attributes

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven or Gradle

### Installation

Add the following dependency to your project:

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    testImplementation("io.github.damien-vdb:dcm4junit-test:0.2.0")
}
```

#### Maven

```xml

<dependency>
    <groupId>io.github.damien-vdb</groupId>
    <artifactId>dcm4junit-test</artifactId>
    <version>0.2.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Using the DIMSE Mock Server

```java
@ExtendWith(DimseMockExtension.class)
class MyDicomTest {

    @Test
    void cstore(DimseMock mock) {

        // Run your DICOM client
        String[] args = {
                "-b", "storescu",
                "-c", mock.getAeTitle() + "@" + mock.getHost() + ":" + mock.getPort(),
                "test.dcm"
        };
        StoreSCU.main(args);

        // Verify the mock received the expected DICOM file
        assertThat(mock.getStoredFiles()).hasSize(1);
    }

    @Test
    void cfind(DimseMock mock) {

        var query = builder()
                .setString(Tag.QueryRetrieveLevel, "STUDY")
                .setString(Tag.PatientID, "PID")
                .setString(Tag.StudyInstanceUID, "1.2.3.4.5")
                .setNull(Tag.StudyDate)
                .build();
        var response = builder(QUERY)
                .setString(Tag.StudyDate, "20250101")
                .build();

        // Setup the stub
        mock.getCFindScp()
                .stubFor(query)
                .withAffectedSopClassUid(UID.StudyRootQueryRetrieveInformationModelFind)
                .willReturn(response);

        // Run your DICOM client
        String[] args = new String[]{
                "-b", "findscu",
                "-c", mock.getAeTitle() + "@" + mock.getHostname() + ":" + mock.getPort(),
                "-L", "STUDY",
                "-m", "PatientID=" + query.getString(Tag.PatientID),
                "-m", "StudyInstanceUID=" + query.getString(Tag.StudyInstanceUID),
                "--out-dir", "/tmp"
        };
        FindSCU.main(args);
    }

    @Test
    void cmove(DimseMock mock) {

        String aet = "aet";
        String hostname = "localhost";
        int port = 1234;

        mock.getCMoveScp().stubFor(QUERY)
                .willStore(FILE.toPath())
                .to(aet, hostname, port);

        // Run your DICOM client
    }
}
```

### DICOM Assertions

```java

@Test
void testDicomAttributes() {
    Attributes attrs = new Attributes();
    attrs.setString(Tag.PatientName, VR.PN, "Doe^John");

    assertThat(attrs)
            .containsValue(Tag.PatientName, "Doe^John")
            .doesNotContainTag(Tag.PatientID);
}
```

### Generating Test Data

```java

@Test
void generateTestStudy() {
    Study study = Study.builder()
            .patientId("TEST123")
            .studyDescription("Test Study")
            .series(List.of(
                    Series.builder()
                            .modality("CT")
                            .instancesCount(10)
                            .build()
            ))
            .build();

    List<Attributes> instances = study.generate().toList();
    assertThat(instances).hasSize(10);
}
```

### Spring Boot Integration: dcm4junit-spring-boot-test

The `dcm4junit-spring-boot-test` module provides annotations for seamless integration of the DIMSE mock server with
Spring Boot tests.

#### Provided Annotations

- `@EnableDimseMock` — Enables and configures a DIMSE mock server for your test context. Accepts a `@DimseMockSettings`
  parameter for customization.
- `@DimseMockSettings` — Used to specify settings for the mock server, such as AE title and name like in dcm4junit-test.
- `@AutowiredDimseMock` — Injects a configured `DimseMock` instance into your test class.

#### Example Usage

```java

@EnableDimseMock(value =
@DimseMockSettings(
        name = "server",
        aet = "MOCK"
)
)
@SpringBootTest
class AfterClassSpringBootExtensionTest {
    @AutowiredDimseMock("server")
    DimseMock dimseMock;
    // ...
}
```

This allows you to use the mock server in Spring Boot tests, with full support for dependency injection and custom
settings.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/damien-vdb/dcm4junit.git
cd dcm4junit

# Build the project
./gradlew build
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built on top of the excellent [dcm4che](https://www.dcm4che.org/) DICOM library
- Inspired by various testing utilities in the DICOM ecosystem
