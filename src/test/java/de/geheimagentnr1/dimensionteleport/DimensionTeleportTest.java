package de.geheimagentnr1.dimensionteleport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DimensionTeleportTest {

    @Test
    void modIdIsValid() {

        String modId = "dimensionteleport";
        assertTrue( modId.matches( "[a-z][a-z0-9_]{1,63}" ) );
    }
}
