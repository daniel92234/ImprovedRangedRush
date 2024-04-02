package RangedRushImproved;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.AbstractAction;
import ai.abstraction.Harvest;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;

/**
 *
 * @author santi (original)
 * modified to allow more barracks and workers
 * modified by Danny Nagura
 */
public class RangedRushImproved extends AbstractionLayerAI {

    Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;
    UnitType heavyType;
    UnitType rangedType;

    // If we have any "light": send it to attack to the nearest enemy unit
    // If we have a base: train worker until we have 1 workers
    // If we have a barracks: train light
    // If we have a worker: do this if needed: build base, build barracks, harvest resources
    public RangedRushImproved(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }

    public RangedRushImproved(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        reset(a_utt);
    }

    public void reset() {
    	super.reset();
    }
    
    public void reset(UnitTypeTable a_utt) {
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        heavyType = utt.getUnitType("Heavy");
        rangedType = utt.getUnitType("Ranged");
    }

    public AI clone() {
        return new RangedRushImproved(utt, pf);
    }

    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
//        System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");

        int nbases = 0;
        int nebases = 0;
        int nallbases = 0;
        int nbarracks = 0;
        int nebarracks = 0;
        int nallbarracks = 0;
        int nlights = 0;
        int nelights = 0;
        int nalllights = 0;
        int nheavies = 0;
        int neheavies = 0;
        int nallheavies = 0;
        int nranged = 0;
        int neranged = 0;
        int nallranged = 0;
        int nworkers = 0;
        int neworkers = 0;
        int nallworkers = 0;
        int nunitsnoworker = 0;
        int neunitsnoworker = 0;
        int nallunitsnoworker = 0;
        int nunits = 0;
        int nallunits = 0;
        int neunits = 0;

        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType) {
                nallbases++;
                if (u.getPlayer() == p.getID()) {
                    nbases++;
                }
                else {
                    nebases++;
                }
            }
            else if (u.getType() == barracksType) {
                nallbarracks++;
                if (u.getPlayer() == p.getID()) {
                    nbarracks++;
                }
                else {
                    nebarracks++;
                }
            }
            else if (u.getType() == lightType) {
                nalllights++;
                if (u.getPlayer() == p.getID()) {
                    nlights++;
                }
                else {
                    nelights++;
                }
            }
            else if (u.getType() == heavyType) {
                nallheavies++;
                if (u.getPlayer() == p.getID()) {
                    nheavies++;
                }
                else {
                    neheavies++;
                }
            }
            else if (u.getType() == rangedType) {
                nallranged++;
                if (u.getPlayer() == p.getID()) {
                    nranged++;
                }
                else {
                    neranged++;
                }
            }
            else if (u.getType() == workerType) {
                nallworkers++;
                if (u.getPlayer() == p.getID()) {
                    nworkers++;
                }
                else {
                    neworkers++;
                }
            }
            if (u.getType() == workerType || u.getType() == lightType || u.getType() == heavyType || u.getType() == rangedType) {
                nallunits++;
                if (u.getPlayer() == p.getID()) {
                    nunits++;
                    if (u.getType() == lightType || u.getType() == heavyType || u.getType() == rangedType) {
                        nunitsnoworker++;
                    }
                }
                else {
                    neunits++;
                    if (u.getType() == lightType || u.getType() == heavyType || u.getType() == rangedType) {
                        neunitsnoworker++;
                    }
                }
            }
        }

        // behavior of bases:
        if (nbases != 0) {
            for (Unit u : pgs.getUnits()) {
                if (u.getType() == baseType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                    baseBehavior(u, p, pgs, nworkers, nbases);
                }
            }
        }

        // behavior of barracks:
        if (nbarracks != 0) {
            for (Unit u : pgs.getUnits()) {
                if (u.getType() == barracksType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                    barracksBehavior(u, p, pgs);
                }
            }
        }

        // attack behavior of worker units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest && u.getType() == workerType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                meleeUnitBehaviorRush(u, p, gs);
            }
        }

        // attack behavior of ranged units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && (u.getType() == lightType || u.getType() == heavyType || u.getType() == rangedType) && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                if (nunitsnoworker >= 6 || p.getResources() < 2 || nbases == 0 || nebases == 0 || neunitsnoworker == 0) {
                    meleeUnitBehaviorRush(u, p, gs);
                } else {
                    meleeUnitBehaviorDefense(u, p, gs);
                }
            }
        }

        // behavior of workers:
        List<Unit> workers = new LinkedList<>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, gs, nbases, nbarracks);

        return translateActions(player, gs);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs, int nworkers, int nbases) {

        if ((nworkers < 3 * nbases) && p.getResources() >= workerType.cost) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if (p.getResources() >= rangedType.cost) {
            train(u, rangedType);
        }
    }

    public void meleeUnitBehaviorRush(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
        //    System.out.println("LightRushAI.meleeUnitBehavior: " + u + " attacks " + closestEnemy);
            attack(u, closestEnemy);
        }
    }

    public void meleeUnitBehaviorDefense(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        int mybase = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        else if(u2.getPlayer()==p.getID() && u2.getType() == baseType)
            {
                mybase = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
            }
        }
        if (closestEnemy != null && (closestDistance < pgs.getHeight()/3 || mybase < pgs.getHeight()/3)) {
            attack(u,closestEnemy);
        }
        else {
            attack(u, null);
        }
    }

    public void workersBehavior(List<Unit> workers, Player p, GameState gs, int nbases, int nbarracks) {
        PhysicalGameState pgs = gs.getPhysicalGameState();

        int resourcesUsed = 0;
        List<Unit> freeWorkers = new LinkedList<>(workers);

        if (workers.isEmpty()) {
            return;
        }

        List<Integer> reservedPositions = new LinkedList<>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += baseType.cost;
            }
        }

        if (nbarracks < 3 && !freeWorkers.isEmpty()) {
            // build (a) barrack(s):
            
            if (p.getResources() >= barracksType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                int u_pos_x = u.getX();
                int u_pos_y = u.getY();

                boolean valid_build = true;

                if (u_pos_x <= 0 || u_pos_y <= 0 || u_pos_x >= pgs.getWidth() - 1 || u_pos_y >= pgs.getHeight() - 1) {
                    valid_build = false;
                }

                if ((nbarracks == 0 || // Zero barracks currently on field
                (nbarracks == 1 && p.getResources() > 13)) // One barrack currently on field
                && valid_build) {
                    buildIfNotAlreadyBuilding(u,barracksType,u_pos_x,u_pos_y,reservedPositions,p,pgs);
                    resourcesUsed += barracksType.cost;
                } else {
                    freeWorkers.add(u);
                }
            }
        }

        // harvest with all the free workers:
        List<Unit> stillFreeWorkers = new LinkedList<>();
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            boolean resourceNearEnemyBase = false;

            // Get closest resource for each worker
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY()); // Movement distance between worker and resource
                    if ((closestResource == null || d < closestDistance) && !resourceNearEnemyBase) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;

            // Get closest base for each worker if they are carrying a resource
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }

            boolean workerStillFree = true;
            if (u.getResources() > 0) {
                if (closestBase!=null) {
                    AbstractAction aa = getAbstractAction(u);
                    if (aa instanceof Harvest) {
                        Harvest h_aa = (Harvest)aa;
                        if (h_aa.getBase()!=closestBase) {
                            harvest(u, null, closestBase);
                        }
                    } else {
                        harvest(u, null, closestBase);
                    }
                    workerStillFree = false;
                }
            } else {
                if (closestResource!=null && closestBase!=null) {
                    AbstractAction aa = getAbstractAction(u);
                    if (aa instanceof Harvest) {
                        Harvest h_aa = (Harvest)aa;
                        if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase) {
                            harvest(u, closestResource, closestBase); // Member values target and base are private. Using accessors instead...
                        }
                    } else {
                        harvest(u, closestResource, closestBase);
                    }
                    workerStillFree = false;
                }
            }
            
            if (workerStillFree) stillFreeWorkers.add(u);
        }
        
        for(Unit u:stillFreeWorkers) meleeUnitBehaviorRush(u, p, gs);
    }

    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
