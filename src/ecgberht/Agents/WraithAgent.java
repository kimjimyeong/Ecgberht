package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent extends Agent implements Comparable<Unit> {

    public Wraith unit;
    public String name = "Pepe";
    private Set<Unit> airAttackers = new TreeSet<>();

    public WraithAgent(Unit unit) {
        super();
        this.unit = (Wraith) unit;
        this.myUnit = unit;
    }

    public WraithAgent(Unit unit, String name) {
        super();
        this.unit = (Wraith) unit;
        this.name = name;
        this.myUnit = unit;
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists()) return true;
            if (unit.getHitPoints() <= 15) {
                Position cc = getGs().MainCC.second.getPosition();
                if (cc != null) unit.move(cc);
                else unit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().myArmy.add(unit);
                return true;
            }
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            closeEnemies.clear();
            mainTargets.clear();
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            //Status old = status;
            getNewStatus();
            //if (old == status && status != Status.COMBAT && status != Status.ATTACK) return false;
            if (status != Status.COMBAT) attackUnit = null;
            if (status == Status.ATTACK && (unit.isIdle() || unit.getOrder() == Order.PlayerGuard)) {
                Position pos = Util.chooseAttackPosition(unit.getPosition(), true);
                if (pos != null && getGs().bw.getBWMap().isValidPosition(pos)) {
                    unit.attack(pos);
                    return false;
                }
            }
            switch (status) {
                case ATTACK:
                    attack();
                    break;
                case COMBAT:
                    combat();
                    break;
                case KITE:
                    kite();
                    break;
                default:
                    break;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception WraithAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void kite() {
        Position kite = getGs().kiteAway(unit, airAttackers);
        if (!getGs().getGame().getBWMap().isValidPosition(kite)) return;
        Position target = unit.getOrderTargetPosition();
        if (target != null && !target.equals(kite)) unit.move(kite);
        if (target == null) unit.move(kite);
    }

    private void combat() {
        Unit toAttack = getUnitToAttack(unit, mainTargets);
        if (toAttack != null) {
            if (attackUnit != null && attackUnit.equals(toAttack)) return;
            unit.attack(toAttack);
            attackUnit = toAttack;
        } else if (!airAttackers.isEmpty()) {
            toAttack = getUnitToAttack(unit, airAttackers);
            if (toAttack != null && attackUnit != null && !attackUnit.equals(toAttack)) {
                unit.attack(toAttack);
                attackUnit = toAttack;
                attackPos = null;
            }
        } else if (!closeEnemies.isEmpty()) {
            toAttack = getUnitToAttack(unit, closeEnemies);
            if (toAttack != null) {
                if (attackUnit != null && attackUnit.equals(toAttack)) return;
                unit.attack(toAttack);
                attackUnit = toAttack;
            }
        }
    }

    private void getNewStatus() {
        SimInfo mySimAir = getGs().sim.getSimulation(unit, SimInfo.SimType.AIR);
        SimInfo mySimMix = getGs().sim.getSimulation(unit, SimInfo.SimType.MIX);
        boolean chasenByScourge = false;
        if (mySimMix.enemies.isEmpty()) {
            status = Status.ATTACK;
            return;
        }
        if (getGs().enemyRace == Race.Zerg && !mySimAir.enemies.isEmpty()) {
            for (Unit u : mySimAir.enemies) {
                if (u instanceof Scourge && ((Scourge) u).getOrderTarget().equals(unit)) {
                    chasenByScourge = true;
                    break;
                }
            }
        }
        for (Unit u : mySimMix.enemies) {
            if (u instanceof Worker || u instanceof Overlord) mainTargets.add(u);
        }
        airAttackers = mySimAir.enemies;
        closeEnemies = mySimMix.enemies;
        if (closeEnemies.isEmpty()) status = Status.ATTACK; // TODO add target search logic and fix attack spam
        else if (!airAttackers.isEmpty()) {
            Unit closestAirAttacker = Util.getClosestUnit(unit, airAttackers);
            if (mySimAir.lose || chasenByScourge) status = Status.KITE;
            if (closestAirAttacker != null && closestAirAttacker.getDistance(unit) < closestAirAttacker.getType().airWeapon().maxRange() * 1.5)
                status = Status.KITE;
            else status = Status.ATTACK;
        } else status = Status.ATTACK;
    }

    private void attack() {
        Position newAttackPos;
        if (getGs().enemyMainBase != null) newAttackPos = getGs().enemyMainBase.getLocation().toPosition();
        else newAttackPos = selectNewAttack();
        attackPos = newAttackPos;
        if (attackPos == null || !getGs().bw.getBWMap().isValidPosition(attackPos)) attackPos = null;
        else if (getGs().bw.getBWMap().isValidPosition(attackPos)) unit.attack(newAttackPos);
        attackUnit = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof WraithAgent)) return false;
        WraithAgent wraith = (WraithAgent) o;
        return unit.equals(wraith.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }

}
