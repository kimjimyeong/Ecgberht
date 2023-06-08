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

public abstract class VesselAttack{
    private Unit target;
    private Unit oldTarget;
    private UnitInfo unitInfo;
    private ScienceVessel unit;

    private DoAttackToTargetStrategy doAttackToTargetStrategy;
    private IsOldTargetDeadStrategy isOldTargetDeadStrategy;
    private IsTargetDeadStrategy isTargetDeadStrategy;

    public VesselAttack(UnitInfo unitInfo, Unit target, Unit oldTarget, ScienceVessel unit){
        this.unitInfo = unitInfo;
        this.target = target;
        this.oldTarget = oldTarget;
        this.unit = unit;
    }
    
    abstract public void doAttackToTarget();
    abstract public void isOldTargetDead();
    abstract public void isTargetDead();

    public void setDoAttackToTargetStrategy(DoAttackToTargetStrategy doAttackToTargetStrategy){
        this.doAttackToTargetStrategy = doAttackToTargetStrategy;
    }
    public DoAttackToTargetStrategy getDoAttackToTargetStrategy(){
        return doAttackToTargetStrategy;
    }
    public void setIsOldTargetDeadStrategy(IsOldTargetDeadStrategy isOldTargetDeadStrategy){
        this.isOldTargetDeadStrategy = isOldTargetDeadStrategy;
    }
    public IsOldTargetDeadStrategy getIsOldTargetDeadStrategy(){
        return isOldTargetDeadStrategy;
    }
    public void setIsTargetDeadStrategy(IsTargetDeadStrategy isTargetDeadStrategy){
        this.isTargetDeadStrategy = isTargetDeadStrategy;
    }
    public IsTargetDeadStrategy getIsTargetDeadStrategy(){
        return isTargetDeadStrategy;
    }


    public Unit getTarget(){
        return target;
    }
    public Unit getOldTarget(){
        return oldTarget;
    }
}
