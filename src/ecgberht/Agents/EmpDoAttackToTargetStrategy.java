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

public class EmpDoAttackToTargetStrategy implements DoAttackToTargetStrategy{
    public Unit doAttackToTarget(UnitInfo unitInfo, Unit target, Unit oldTarget, ScienceVessel unit){
        if (unitInfo.currentOrder == Order.CastEMPShockwave) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.emp(unit, target.getPosition());
                getGs().wizard.addEMPed(unit, (PlayerUnit) target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastEMPShockwave) {
            UtilMicro.emp(unit, target.getPosition());
            getGs().wizard.addEMPed(unit, (PlayerUnit) target);
            oldTarget = target;
        }
        return oldTarget;
    }
}
