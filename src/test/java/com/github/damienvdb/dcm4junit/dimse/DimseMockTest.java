package com.github.damienvdb.dcm4junit.dimse;

import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockExtension;
import com.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import org.assertj.core.util.Closeables;
import org.dcm4che3.data.Tag;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DimseMockExtension.class)
@DimseMockSettings(aet = "dimsemocktest")
public class DimseMockTest {

    public static final File FILE = new File(DimseMockTest.class.getClassLoader().getResource("MR01.dcm").getPath());

    @Test
    void storescp_keeps_track_of_stored_dicoms(DimseMock mock) {
        String[] args = new String[]{
                "-b", "storescu",
                "-c", mock.getAeTitle() + "@" + mock.getHost() + ":" + mock.getPort(),
                FILE.getAbsolutePath()
        };

        StoreSCU.main(args);

        assertThat(mock.getAssociationsAccepted())
                .hasSize(1)
                .first()
                .matches(association -> association.getCalledAET().equals("dimsemocktest"))
                .matches(association -> association.getCallingAET().equals("storescu"));

        assertThat(mock.getFmis())
                .hasSize(1)
                .first()
                .matches(fmi -> fmi.getString(Tag.MediaStorageSOPInstanceUID).equals("1.113654.5.15.1504"));

        assertThat(mock.getStoredFiles()).hasSize(1);
    }

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
}
