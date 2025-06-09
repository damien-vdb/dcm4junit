# DCM4JUnit

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

DCM4JUnit is a testing library that provides utilities and extensions for testing DICOM applications using JUnit 5. It's
built on top of the dcm4che library and provides a simple way to mock DICOM services and verify DICOM operations in your
tests.

## Features

- **DIMSE Mock Server**: Easily create a mock DICOM SCP for testing DICOM network operations
- **DICOM Assertions**: Rich set of assertions for verifying DICOM attributes and datasets
- **Test Data Generation**: Generate test DICOM data with customizable attributes
- **JUnit 5 Integration**: Seamless integration with JUnit 5's extension model
- **DICOM Tag Utilities**: Helper utilities for working with DICOM tags and attributes

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven or Gradle

### Installation

Add the following dependency to your project:

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    testImplementation("io.github.damien-vdb:dcm4junit:0.1.0")
}
```

#### Maven

```xml

<dependency>
    <groupId>io.github.damien-vdb</groupId>
    <artifactId>dcm4junit</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Using the DIMSE Mock Server

```java

@ExtendWith(DimseMockExtension.class)
class MyDicomTest {

    @Test
    void testDicomStore(DimseMock mock) {
        // Test code that interacts with the mock DICOM server
        String[] args = {
                "-b", "storescu",
                "-c", mock.getAeTitle() + "@" + mock.getHost() + ":" + mock.getPort(),
                "test.dcm"
        };

        // Run your DICOM client
        StoreSCU.main(args);

        // Verify the mock received the expected DICOM file
        assertThat(mock.getStoredFiles()).hasSize(1);
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
