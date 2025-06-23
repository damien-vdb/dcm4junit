package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class DimseMockSpringBootExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static <T extends Annotation> void injectMockInstances(
            final ExtensionContext extensionContext,
            final Class<T> annotation,
            final Function<T, String> fn)
            throws IllegalAccessException {
        for (final Object testInstance :
                extensionContext.getRequiredTestInstances().getAllInstances()) {
            final List<Field> annotatedFields =
                    AnnotationSupport.findAnnotatedFields(testInstance.getClass(), annotation);
            for (final Field annotatedField : annotatedFields) {
                final T annotationValue = annotatedField.getAnnotation(annotation);
                annotatedField.setAccessible(true);

                final DimseMock mock =
                        Store.INSTANCE.findRequiredMockInstance(
                                extensionContext, fn.apply(annotationValue));
                annotatedField.set(testInstance, mock);
            }
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        List<EnableDimseMock> annotations = findSettingsAnnotation(context);
        List<DimseMock> mocks = annotations.stream()
                .map(it -> Store.INSTANCE.findRequiredMockInstance(context, it.value().name()))
                .collect(Collectors.toList());

        mocks.forEach(DimseMock::reset);

        injectMockInstances(context, AutowiredDimseMock.class, AutowiredDimseMock::value);

        injectIntoSpringContext(context);
    }

    private void injectIntoSpringContext(final ExtensionContext extensionContext) {
        DimseMock mock = null;
        String mockName = null;
        for (EnableDimseMock enableDimseMockAnnotation : findSettingsAnnotation(extensionContext)) {
            final DimseMockSettings[] settings =
                    DimseMockContextCustomizerFactory.getDimseMockSettingsOrDefault(enableDimseMockAnnotation.value());
            if (settings.length > 1) {
                log.info("Not configuring DimseMock for default instance when several ConfigureDimseMock ({})", settings.length);
            }
            if (mock != null) {
                log.info("Not configuring DimseMock for default instance when several candidates found");
                return;
            }
            mockName = settings[0].name();
            mock = Store.INSTANCE.findRequiredMockInstance(extensionContext, mockName);
        }

    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(DimseMock.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        final String name = parameterContext.findAnnotation(AutowiredDimseMock.class).get().value();
        return Store.INSTANCE.findRequiredMockInstance(extensionContext, name);
    }

    private List<EnableDimseMock> findSettingsAnnotation(ExtensionContext context) {
        final List<Object> instances = context.getRequiredTestInstances().getAllInstances();
        return instances.stream()
                .flatMap(element -> AnnotationSupport.findAnnotation(element.getClass(), EnableDimseMock.class).stream())
                .collect(Collectors.toList());
    }

}
