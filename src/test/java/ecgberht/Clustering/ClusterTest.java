package ecgberht.Clustering;

import ecgberht.UnitInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.PlayerUnit;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClusterTest {

    private Cluster cluster;
    private UnitInfo unitInfo;

    @BeforeEach
    public void setup(){
        cluster = new Cluster();

        PlayerUnit unit = mock(PlayerUnit.class);
        when(unit.getPosition()).thenReturn(new Position(10, 10));
        unitInfo = new UnitInfo(unit);
        unitInfo.visible = true;
    }

    @DisplayName("유닛들간의 중심좌표 업데이트")
    @Test
    public void testUpdateCentroid(){
        PlayerUnit unit1 = mock(PlayerUnit.class);
        when(unit1.getPosition()).thenReturn(new Position(10, 10));
        UnitInfo unitInfo1 = new UnitInfo(unit1);
        unitInfo1.visible = true;

        PlayerUnit unit2 = mock(PlayerUnit.class);
        when(unit2.getPosition()).thenReturn(new Position(10, 10));
        UnitInfo unitInfo2 = new UnitInfo(unit2);
        unitInfo2.visible = true;

        cluster.units.add(unitInfo);
        cluster.units.add(unitInfo1);
        cluster.units.add(unitInfo2);

        double expectedX = 10;
        double expectedY = 10;

        cluster.updateCentroid();

        assertEquals(expectedX, cluster.modeX);
        assertEquals(expectedY, cluster.modeY);

    }

    @DisplayName("중심좌표로부터 CMaxDist 구하기")
    @Test
    void testUpdateCMaxDistFromCenterOneUnit(){
        double expectedMaxDistFromCenter = 0;
        cluster.updateCMaxDistFromCenter();

        assertEquals(expectedMaxDistFromCenter, cluster.maxDistFromCenter);
    }
}