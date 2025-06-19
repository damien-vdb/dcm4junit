package com.github.damienvdb.dcm4junit.dimse.jupiter;

import com.github.damienvdb.dcm4junit.dimse.DimseMock;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

/**
 * JUnit Jupiter extension for DICOM DIMSE mock server.
 * <p>
 * Example usage:
 * <pre>
 * {@code @ExtendWith(DimseMockExtension.class)}
 * class MyTest {
 *     @Test
 *     void testSomething(DimseMock mock) {
 *         // test code here
 *     }
 * }
 * </pre>
 */
public class DimseMockExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {
    /**
     * Shared mock instance when using per-test-suite mode.
     */
    protected static DimseMock perTestSuiteMock;

    /**
     * Custom mock instance provided via constructor.
     */
    protected final DimseMock customMock;

    /**
     * Current mock instance being used.
     */
    protected DimseMock mock;

    /**
     * Whether to use a single mock instance for all tests in the test suite.
     */
    protected boolean perTestSuite;

    /**
     * Default constructor that will create a new mock for each test class.
     */
    public DimseMockExtension() {
        this.customMock = null;
    }

    /**
     * Constructor that uses the provided mock instance.
     *
     * @param mock the mock instance to use
     */
    public DimseMockExtension(DimseMock mock) {
        this.customMock = mock;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return DimseMock.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return mock;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        Optional<DimseMockSettings> settings = retrieveAnnotationFromTestClass(context);
        perTestSuite = settings.map(DimseMockSettings::perTestSuite).orElse(false);

        if (perTestSuite) {
            synchronized (DimseMockExtension.class) {
                if (perTestSuiteMock == null) {
                    perTestSuiteMock = createNewMock(settings);
                }
                mock = perTestSuiteMock;
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (perTestSuite && perTestSuiteMock != null) {
            perTestSuiteMock.stop();
            perTestSuiteMock = null;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!perTestSuite) {
            // Create a new mock for each test if not in per-test-suite mode
            mock = (customMock != null) ? customMock : createNewMock(retrieveAnnotationFromTestClass(context));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!perTestSuite && mock != null) {
            mock.stop();
            mock = null;
        }
    }

    /**
     * Creates a new DimseMock instance with the given settings.
     *
     * @param settings the settings to use for the mock
     * @return a new DimseMock instance
     */
    protected DimseMock createNewMock(Optional<DimseMockSettings> settings) {
        DimseMock mock = new DimseMock(settings);
        mock.start();
        Runtime.getRuntime().addShutdownHook(new Thread(mock::stop));
        return mock;
    }

    private Optional<DimseMockSettings> retrieveAnnotationFromTestClass(final ExtensionContext context) {
        ExtensionContext currentContext = context;
        Optional<DimseMockSettings> annotation;

        do {
            annotation = AnnotationSupport.findAnnotation(currentContext.getElement(), DimseMockSettings.class);
            if (currentContext.getParent().isEmpty()) {
                break;
            }
            currentContext = currentContext.getParent().get();
        } while (annotation.isEmpty() && currentContext != context.getRoot());

        return annotation;
    }
}
