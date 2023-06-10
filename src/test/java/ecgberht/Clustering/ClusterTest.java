package ecgberht.Clustering;

import ecgberht.UnitInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Dropship;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;


import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
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
        cluster.units.add(unitInfo);
    }

    @DisplayName("유닛들간의 중심좌표 업데이트")
    @Test
    public void testUpdateCentroid(){
        PlayerUnit unit1 = mock(PlayerUnit.class);

        when(unit1.getPosition()).thenReturn(new Position(20, 20));
        UnitInfo unitInfo1 = new UnitInfo(unit1);
        unitInfo1.visible = true;


        PlayerUnit unit2 = mock(PlayerUnit.class);
        when(unit2.getPosition()).thenReturn(new Position(10, 10));
        UnitInfo unitInfo2 = new UnitInfo(unit2);
        unitInfo2.visible = true;

        cluster.units.add(unitInfo1);
        cluster.units.add(unitInfo2);


        double expectedX = 13.333;
        double expectedY = 13.333;

        cluster.updateCentroid();

        assertEquals(expectedX, cluster.modeX, 0.001);
        assertEquals(expectedY, cluster.modeY, 0.001);

    }

    @DisplayName("유닛이 하나일 떄 중심좌표로부터 CMaxDist 구하기 ")
    @Test
    void testUpdateCMaxDistFromCenterOneUnit(){
        double expectedMaxDistFromCenter = 0;
        cluster.updateCMaxDistFromCenter();

        assertEquals(expectedMaxDistFromCenter, cluster.maxDistFromCenter);
    }

    @DisplayName("유닛이 여러개일 때 중심좌표로부터 CMaxDist 구하기")
    @Test
    void testUpdateCMaxDistFromCenterUnits(){
        PlayerUnit unit1 = mock(PlayerUnit.class);
        when(unit1.getPosition()).thenReturn(new Position(10, 10));
        UnitInfo unitInfo1 = new UnitInfo(unit1);
        unitInfo1.visible = true;

        PlayerUnit unit2 = mock(PlayerUnit.class);
        when(unit2.getPosition()).thenReturn(new Position(10, 10));
        UnitInfo unitInfo2 = new UnitInfo(unit2);
        unitInfo2.visible = true;

        cluster.units.add(unitInfo1);
        cluster.units.add(unitInfo2);

        cluster.updateCMaxDistFromCenter();

        double expectedMaxDistFromCenter = 13.0;

        assertEquals(expectedMaxDistFromCenter, cluster.maxDistFromCenter);
    }
}