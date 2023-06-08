package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.UnitInfoDistance;
import ecgberht.Util.ColorUtil;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class IrradiateIsTargetDeadStrategy implements IsTargetDeadStrategy{
    public Unit isTargetDeadStrategy(Unit Target){
        if (target != null && (!target.exists() || ((PlayerUnit) target).isIrradiated())) target = null;
        return target;
    }
}
