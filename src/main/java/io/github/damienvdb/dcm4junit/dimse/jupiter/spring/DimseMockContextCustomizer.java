package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@EqualsAndHashCode
public class DimseMockContextCustomizer implements ContextCustomizer {

    private final List<DimseMockSettings> configuration;

    private static DimseMock createServer(ConfigurableApplicationContext context, DimseMockSettings options) {
        DimseMock mock = new DimseMock(Optional.of(options));
        mock.start();
        Store.INSTANCE.put(context, options.name(), mock);
        context.addApplicationListener(
                event -> {
                    if (event instanceof ContextClosedEvent) {
                        mock.stop();
                    }
                });
        return mock;
    }

    @Override
    public void customizeContext(
            final ConfigurableApplicationContext context, final MergedContextConfiguration mergedConfig) {
        for (final DimseMockSettings config : this.configuration) {
            this.resolveOrCreateDimseMock(context, config);
        }
    }

    private DimseMock resolveOrCreateDimseMock(final ConfigurableApplicationContext context,
                                               final DimseMockSettings options) {
        return Store.INSTANCE.find(context, options.name())
                .orElseGet(() -> createServer(context, options));
    }
}
