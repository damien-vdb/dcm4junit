package com.github.damienvdb.dcm4junit.dimse;

import org.assertj.core.util.Closeables;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DimseMockTest {

    @Test
    void start_binds_dynamic_socket() {
        DimseMock[] mocks = new DimseMock[100];
        try {
            var start = System.currentTimeMillis();
            for (int i = 0; i < mocks.length; i++) {
                mocks[i] = new DimseMock(Optional.empty());
                mocks[i].start();
            }

            System.out.println("Bound " + mocks.length + " SCPs in " + (System.currentTimeMillis() - start) + " ms");
        } finally {
            Closeables.closeQuietly(mocks);
        }
    }

    @Test
    void cstoreScpDisabled_throws_exception() {
        var mock = new DimseMock(Optional.empty());
        mock.start();

        assertThatThrownBy(mock::getCStoreScp)
                .isInstanceOf(IllegalStateException.class);
    }
}
