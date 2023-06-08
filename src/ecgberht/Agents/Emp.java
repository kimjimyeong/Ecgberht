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

public class Emp extends VesselAttack{
    public Emp(UnitInfo unitInfo, Unit target, Unit oldTarget, ScienceVessel unit){
        super(unitInfo,target,oldTarget,unit);
        setDoAttackToTargetStrategy(new EmpDoAttackToTargetStrategy());
        setIsOldTargetDeadStrategy( new EmpIsOldTargetDeadStrategy());
        setIsTargetDeadStrategy( new EmpIsTargetDeadStrategy() );
    }
    public void doAttackToTarget(){
        oldTarget = getDoAttackToTargetStrategy().doAttackToTarget(unitInfo,target,oldTarget,unit);
    }
    public void isOldTargetDead(){
        oldTarget = getIsOldTargetDeadStrategy().isOldTargetDeadStrategy(oldTarget);
    }
    public void isTargetDead(){
        oldTarget = getIsTargetDeadStrategy().isTargetDeadStrategy(Target);
    }
}
