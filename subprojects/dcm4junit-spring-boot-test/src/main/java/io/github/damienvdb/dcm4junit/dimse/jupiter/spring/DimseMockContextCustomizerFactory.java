package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DimseMockContextCustomizerFactory implements ContextCustomizerFactory {

    static final DimseMockSettings DEFAULT_CONFIGURE_WIREMOCK =
            DimseMockSettings.class.getAnnotation(DimseMockSettings.class);

    static DimseMockSettings[] getDimseMockSettingsOrDefault(
            final DimseMockSettings... settings) {
        if (settings == null || settings.length == 0) {
            return new DimseMockSettings[]{DimseMockContextCustomizerFactory.DEFAULT_CONFIGURE_WIREMOCK};
        }
        return settings;
    }

    @Override
    public ContextCustomizer createContextCustomizer(
            final Class<?> testClass, final List<ContextConfigurationAttributes> configAttributes) {
        final ConfigureDimsemockHolder holder = new ConfigureDimsemockHolder();
        this.parseDefinitions(testClass, holder);

        if (holder.isEmpty()) {
            return null;
        } else {
            return new DimseMockContextCustomizer(Arrays.asList(holder.asArray()));
        }
    }

    private void parseDefinitions(final Class<?> testClass, final ConfigureDimsemockHolder parser) {
        for (EnableDimseMock enableDimseMockAnnotation : getEnableDimseMockAnnotations(testClass)) {
            parser.add(getDimseMockSettingsOrDefault(enableDimseMockAnnotation.value()));
        }
    }

    private List<EnableDimseMock> getEnableDimseMockAnnotations(final Class<?> testClass) {
        final List<EnableDimseMock> annotations = new ArrayList<>();
        AnnotationUtils.findAnnotation(testClass, EnableDimseMock.class)
                .ifPresent(annotations::add);

        Stream.of(testClass.getEnclosingClass(), testClass.getSuperclass())
                .filter(Objects::nonNull)
                .forEach(clazz -> annotations.addAll(
                        getEnableDimseMockAnnotations(clazz).stream()
                                .filter(it -> !annotations.contains(it))
                                .collect(Collectors.toList())));

        return annotations;
    }

    @DimseMockSettings(name = "")
    private static class DefaultDimseMockSettings {
    }

    private static class ConfigureDimsemockHolder {
        private final List<DimseMockSettings> annotations = new ArrayList<>();

        void add(final DimseMockSettings... annotations) {
            this.annotations.addAll(Arrays.asList(annotations));
        }

        boolean isEmpty() {
            return this.annotations.isEmpty();
        }

        DimseMockSettings[] asArray() {
            return this.annotations.toArray(new DimseMockSettings[]{
            });
        }
    }
}
