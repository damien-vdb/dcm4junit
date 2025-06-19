package io.github.damienvdb.dcm4junit.dimse;

import io.github.damienvdb.dcm4junit.dimse.jupiter.DimseMockSettings;
import org.dcm4che3.data.Tag;
import org.dcm4che3.tool.storescu.StoreSCU;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@DimseMockSettings(aet = "MockStoreScpTest")
public class MockStoreScpTest {

    public static final File FILE = new File(DimseMockTest.class.getClassLoader().getResource("MR01.dcm").getPath());

    @Test
    void storescp_keeps_track_of_stored_dicoms(DimseMock mock) {
        String[] args = new String[]{
                "-b", "storescu",
                "-c", mock.getAeTitle() + "@" + mock.getHostname() + ":" + mock.getPort(),
                FILE.getAbsolutePath()
        };

        StoreSCU.main(args);

        assertThat(mock.getAssociationsAccepted())
                .hasSize(1)
                .first()
                .matches(association -> association.getCalledAET().equals("MockStoreScpTest"))
                .matches(association -> association.getCallingAET().equals("storescu"));

        var cStoreScp = mock.getCStoreScp();

        assertThat(cStoreScp.getFmis())
                .hasSize(1)
                .first()
                .matches(fmi -> fmi.getString(Tag.MediaStorageSOPInstanceUID).equals("1.113654.5.15.1504"));

        assertThat(cStoreScp.getStoredFiles()).hasSize(1);
    }
}
