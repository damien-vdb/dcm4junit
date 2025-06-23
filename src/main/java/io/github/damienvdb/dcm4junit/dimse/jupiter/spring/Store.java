package io.github.damienvdb.dcm4junit.dimse.jupiter.spring;

import io.github.damienvdb.dcm4junit.dimse.DimseMock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Store {

    INSTANCE;

    private final Map<ApplicationContext, Map<String, DimseMock>> store =
            new ConcurrentHashMap<>();


    public DimseMock findRequiredMockInstance(ExtensionContext context, String name) {
        return find(SpringExtension.getApplicationContext(context), name)
                .orElseThrow(() -> new IllegalStateException("Mock " + name + " not found"));
    }

    public Optional<DimseMock> find(ApplicationContext applicationContext, String name) {
        Map<String, DimseMock> map = store.computeIfAbsent(applicationContext, ctx -> new ConcurrentHashMap<>());
        return Optional.ofNullable(map.get(name));
    }

    public void put(ConfigurableApplicationContext context, String name, DimseMock mock) {
        store.computeIfAbsent(context, ctx -> new ConcurrentHashMap<>())
                .put(name, mock);
    }
}
