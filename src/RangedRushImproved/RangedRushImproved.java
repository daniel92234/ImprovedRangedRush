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
import java.lang.Math;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;
import util.Pair;

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
    UnitType resourceType;

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
        resourceType = utt.getUnitType("Resource");
    }

    public AI clone() {
        return new RangedRushImproved(utt, pf);
    }

    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
//        System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");

        // Lists of different unit types
        List<Unit> bases = new LinkedList<>(); // Player bases
        List<Unit> ebases = new LinkedList<>(); // Enemy bases
        List<Unit> allbases = new LinkedList<>(); // All bases
        List<Unit> barracks = new LinkedList<>(); // Player barracks
        List<Unit> ebarracks = new LinkedList<>(); // Enemy barracks
        List<Unit> allbarracks = new LinkedList<>(); // All barracks
        List<Unit> lights = new LinkedList<>(); // Player light units
        List<Unit> elights = new LinkedList<>(); // Enemy light units
        List<Unit> alllights = new LinkedList<>(); // All light units
        List<Unit> heavies = new LinkedList<>(); // Player heavy units
        List<Unit> eheavies = new LinkedList<>(); // Enemy heavy units
        List<Unit> allheavies = new LinkedList<>(); // All heavy units
        List<Unit> ranged = new LinkedList<>(); // Player ranged units
        List<Unit> eranged = new LinkedList<>(); // Enemy ranged units
        List<Unit> allranged = new LinkedList<>(); // All ranged units
        List<Unit> workers = new LinkedList<>(); // Player worker units
        List<Unit> eworkers = new LinkedList<>(); // Enemy worker units
        List<Unit> allworkers = new LinkedList<>(); // All worker units
        List<Unit> unitsnoworker = new LinkedList<>(); // Player non-worker units
        List<Unit> eunitsnoworker = new LinkedList<>(); // Enemy non-worker units
        List<Unit> allunitsnoworker = new LinkedList<>(); // All non-worker units
        List<Unit> buildings = new LinkedList<>(); // All player buildings
        List<Unit> ebuildings = new LinkedList<>(); // All enemy buildings
        List<Unit> allbuildings = new LinkedList<>(); // All buildings
        List<Unit> units = new LinkedList<>(); // All player units
        List<Unit> eunits = new LinkedList<>(); // All enemy units
        List<Unit> allunits = new LinkedList<>(); // All units
        List<Unit> unitsbuildings = new LinkedList<>(); // All player units and buildings
        List<Unit> eunitsbuildings = new LinkedList<>(); // All enemy units and buildings
        List<Unit> allunitsbuildings = new LinkedList<>(); // All units and buildings
        List<Unit> resources = new LinkedList<>(); // Resources

        // Add all the units to lists
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType) {
                if (u.getPlayer() == p.getID()) {
                    bases.add(u);
                }
                else {
                    ebases.add(u);
                }
            }
            else if (u.getType() == barracksType) {
                if (u.getPlayer() == p.getID()) {
                    barracks.add(u);
                }
                else {
                    ebarracks.add(u);
                }
            }
            else if (u.getType() == lightType) {
                if (u.getPlayer() == p.getID()) {
                    lights.add(u);
                }
                else {
                    elights.add(u);
                }
            }
            else if (u.getType() == heavyType) {
                if (u.getPlayer() == p.getID()) {
                    heavies.add(u);
                }
                else {
                    eheavies.add(u);
                }
            }
            else if (u.getType() == rangedType) {
                if (u.getPlayer() == p.getID()) {
                    ranged.add(u);
                }
                else {
                    eranged.add(u);
                }
            }
            else if (u.getType() == workerType) {
                if (u.getPlayer() == p.getID()) {
                    workers.add(u);
                }
                else {
                    eworkers.add(u);
                }
            }
            else if (u.getType() == resourceType) {
                resources.add(u);
            }
        }

        // Create combined lists
        allbases.addAll(bases);
        allbases.addAll(ebases);
        allbarracks.addAll(barracks);
        allbarracks.addAll(ebarracks);
        alllights.addAll(lights);
        alllights.addAll(elights);
        allheavies.addAll(heavies);
        allheavies.addAll(eheavies);
        allranged.addAll(ranged);
        allranged.addAll(eranged);
        allworkers.addAll(workers);
        allworkers.addAll(eworkers);
        buildings.addAll(bases);
        buildings.addAll(barracks);
        ebuildings.addAll(ebases);
        ebuildings.addAll(ebarracks);
        allbuildings.addAll(buildings);
        allbuildings.addAll(ebuildings);
        unitsnoworker.addAll(lights);
        unitsnoworker.addAll(heavies);
        unitsnoworker.addAll(ranged);
        eunitsnoworker.addAll(elights);
        eunitsnoworker.addAll(eheavies);
        eunitsnoworker.addAll(eranged);
        allunitsnoworker.addAll(alllights);
        allunitsnoworker.addAll(allheavies);
        allunitsnoworker.addAll(allranged);
        units.addAll(unitsnoworker);
        units.addAll(workers);
        eunits.addAll(eunitsnoworker);
        eunits.addAll(eworkers);
        allunits.addAll(units);
        allunits.addAll(eunits);
        unitsbuildings.addAll(units);
        unitsbuildings.addAll(buildings);
        eunitsbuildings.addAll(eunits);
        eunitsbuildings.addAll(ebuildings);
        allunitsbuildings.addAll(unitsbuildings);
        allunitsbuildings.addAll(eunitsbuildings);

        List<Unit> resources_near_player_base = new LinkedList<>(); // Resources nearby player's bases
        List<Unit> resources_near_enemy_base = new LinkedList<>(); // Resources nearby enemy's bases
        List<Unit> resources_near_no_base = new LinkedList<>(resources); // Resources nearby no bases
        List<Unit> resources_gather = new LinkedList<>(resources); // Resources not exclusively nearby enemy's bases
        
        for (Unit r : resources) {
            if (getUnitDistance(r, getClosestUnitType(r, bases)) < 11) {
                resources_near_player_base.add(r);
            }
            if (getUnitDistance(r, getClosestUnitType(r, ebases)) < 11) {
                resources_near_enemy_base.add(r);
            }
        }

        resources_near_no_base.removeAll(resources_near_player_base);
        resources_near_no_base.removeAll(resources_near_enemy_base);
        resources_gather.removeAll(resources_near_enemy_base);
        resources_gather.removeAll(resources_near_player_base); // Remove player resources to prevent duplicates
        resources_gather.addAll(resources_near_player_base); // Add player resources including the ones that are possibly also nearby enemy's bases

        // behavior of bases:
        for (Unit u : bases) {
            if (gs.getActionAssignment(u) == null) {
                baseBehavior(u, p, pgs, eranged, workers, eworkers);
            }
        }

        // behavior of barracks:
        for (Unit u : barracks) {
            if (gs.getActionAssignment(u) == null) {
                barracksBehavior(u, p, pgs, barracks, resources_near_player_base, resources_near_no_base);
            }
        }

        // attack behavior of worker units:
        for (Unit u : workers) {
            if ((u.getType().canAttack && !u.getType().canHarvest && gs.getActionAssignment(u) == null)) {
                meleeUnitBehaviorRush(u, p, gs, eunitsbuildings);
            }
        }

        // attack behavior of ranged units:
        for (Unit u : unitsnoworker) {
            if (u.getType().canAttack && gs.getActionAssignment(u) == null) {
                boolean rushMode = false;

                if (unitsnoworker.size() >= Math.max(8, eunitsnoworker.size() + (eworkers.size()/2)) ||
                    p.getResources() < 2 ||
                    bases.isEmpty() ||
                    ebases.isEmpty() ||
                    eunitsnoworker.isEmpty()
                ) {
                    rushMode = true;
                }

                for (Unit b : bases) {
                    if (getUnitDistance(b, getClosestUnitType(b, eunitsbuildings)) < 8)
                        rushMode = true;
                }

                if (rushMode) {
                    meleeUnitBehaviorRush(u, p, gs, eunitsbuildings);
                } 
                else {
                    meleeUnitBehaviorDefense(u, p, gs, allunitsbuildings);
                }
            }
        }

        // behavior of workers:
        List<Unit> workersCanHarvest = new LinkedList<>();
        for (Unit u : workers) {
            if (u.getType().canHarvest) {
                workersCanHarvest.add(u);
            }
        }
        workersBehavior(p, gs, workersCanHarvest,
            bases,
            ebases,
            barracks,
            eranged,
            workers,
            eworkers,
            eunitsbuildings,
            ebuildings,
            allbuildings,
            resources,
            resources_gather
        );

        return translateActions(player, gs);
    }

    public Unit getClosestUnitType(Unit u, List<Unit> units) {
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : units) {
            int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
            if (closestEnemy == null || d < closestDistance) {
                closestEnemy = u2;
                closestDistance = d;
            }
        }
        return closestEnemy;
    }

    public int getUnitDistance(Unit u, Unit u2) {
        if (u == null || u2 == null) {
            return Integer.MAX_VALUE;
        }
        
        return Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
    }

    public int getPointDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs, List<Unit> eranged, List<Unit> workers, List<Unit> eworkers) {

        boolean rushMode = (pgs.getWidth() <= 12 && eranged.isEmpty()) || (pgs.getWidth() <= 16 && workers.size() < eworkers.size()*0.8);

        if (((workers.size() < 3 && !rushMode) || rushMode) && p.getResources() >= workerType.cost) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs, List<Unit> barracks, List<Unit> resources_near_player_base, List<Unit> resources_near_no_base) {
        if (p.getResources() >= rangedType.cost) {
            train(u, rangedType);
        }
    }

    public void meleeUnitBehaviorRush(Unit u, Player p, GameState gs,
        List<Unit> eunitsbuildings
    ) {
        Unit closestEnemy = getClosestUnitType(u, eunitsbuildings);
        if (closestEnemy != null) {
        //    System.out.println("LightRushAI.meleeUnitBehavior: " + u + " attacks " + closestEnemy);
            attack(u, closestEnemy);
        }
    }

    public void meleeUnitBehaviorDefense(Unit u, Player p, GameState gs, List<Unit> allunitsbuildings) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        int mybase = 0;
        for (Unit u2 : allunitsbuildings) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = getUnitDistance(u, u2);
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
            else if (u2.getPlayer()==p.getID() && u2.getType() == baseType) {
                mybase = getUnitDistance(u, u2);
            }
        }
        if (closestEnemy != null && (closestDistance < pgs.getHeight()/4 || mybase < pgs.getHeight()/4)) {
            attack(u, closestEnemy);
        }
        else {
            attack(u, null);
        }
    }

    public void buildBaseAroundNearestResource(Unit u, PhysicalGameState pgs,
        List<Unit> bases,
        List<Unit> ebases,
        List<Unit> allbuildings,
        List<Unit> resources_gather
    ) {

        Unit closestResource = getClosestUnitType(u, resources_gather);

        if (closestResource != null) {

            Unit closestEnemyBase = getClosestUnitType(u, ebases);
            List<Pair<Integer, Integer>> buildPositions = new LinkedList<>();
            
            for (int building_distance = 2; building_distance < 7; building_distance++) {
                x_loop:
                for (int x = closestResource.getX()-building_distance; x <= closestResource.getX()+building_distance; x++) {
                    y_loop:
                    for (int y = closestResource.getY()-building_distance; y <= closestResource.getY()+building_distance; y++) {

                        // Don't build at edges
                        if (x < 1 || x >= pgs.getWidth()-1) {
                            continue x_loop;
                        }
                        if (y < 1 || y >= pgs.getHeight()-1) {
                            continue;
                        }

                        // Don't build around resources
                        for (Unit resource : resources_gather) {
                            if (x < resource.getX()+2 && x > resource.getX()-2 && y < resource.getY()+2 && y > resource.getY()-2) {
                                continue y_loop;
                            }
                        }

                        // Don't build around buildings
                        for (Unit building : allbuildings) {
                            if (x < building.getX()+2 && x > building.getX()-2 && y < building.getY()+2 && y > building.getY()-2) {
                                continue y_loop;
                            }
                        }

                        // Don't build around walls
                        xx_loop:
                        for (int xx = x - 1; xx < x + 2; xx++) {
                            for (int yy = y - 1; yy < y + 2; yy++) {
                                if (xx < 1 || xx >= pgs.getWidth()-1) {
                                    continue xx_loop;
                                }
                                if (yy < 1 || yy >= pgs.getHeight()-1) {
                                    continue;
                                }
                                if (pgs.getTerrain(xx, yy) == PhysicalGameState.TERRAIN_WALL) {
                                    continue y_loop;
                                }
                            }
                        }

                        // Add valid building position
                        buildPositions.add(new Pair<Integer,Integer>(x, y));
                    }
                }

                if (!buildPositions.isEmpty()) {

                    // Build base further away from enemy base
                    if (closestEnemyBase != null) {
                        int furthest_d = 0;
                        Pair<Integer,Integer> furthest_point = null;

                        for (Pair<Integer,Integer> point : buildPositions) {
                            int pointDistance = getPointDistance(point.m_a, point.m_b, closestEnemyBase.getX(), closestEnemyBase.getY());
                            if (pointDistance > furthest_d) {
                                furthest_d = pointDistance;
                                furthest_point = point;
                            }
                        }
                        build(u, baseType, furthest_point.m_a, furthest_point.m_b);
                    }
                    else {
                        build(u, baseType, buildPositions.getFirst().m_a, buildPositions.getFirst().m_b);
                    }
                    break;
                }
            }
        }
    }

    public void buildBarracksAroundNearestBase(Unit u, PhysicalGameState pgs,
        List<Unit> bases,
        List<Unit> ebases,
        List<Unit> allbuildings,
        List<Unit> resources
    ) {
        Unit closestPlayerBase = getClosestUnitType(u, bases);
        if (closestPlayerBase != null) {

            Unit closestEnemyBase = getClosestUnitType(u, ebases);
            List<Pair<Integer, Integer>> buildPositions = new LinkedList<>();

            for (int building_distance = 2; building_distance < 7; building_distance++) {
                x_loop:
                for (int x = closestPlayerBase.getX()-building_distance; x <= closestPlayerBase.getX()+building_distance; x++) {
                    y_loop:
                    for (int y = closestPlayerBase.getY()-building_distance; y <= closestPlayerBase.getY()+building_distance; y++) {

                        // Don't build at edges
                        if (x < 1 || x >= pgs.getWidth()-1) {
                            continue x_loop;
                        }
                        if (y < 1 || y >= pgs.getHeight()-1) {
                            continue;
                        }

                        // Don't build around resources
                        for (Unit resource : resources) {
                            if (x < resource.getX()+2 && x > resource.getX()-2 && y < resource.getY()+2 && y > resource.getY()-2) {
                                continue y_loop;
                            }
                        }

                        // Don't build around buildings
                        for (Unit building : allbuildings) {
                            if (x < building.getX()+2 && x > building.getX()-2 && y < building.getY()+2 && y > building.getY()-2) {
                                continue y_loop;
                            }
                        }

                        // Don't build around walls
                        xx_loop:
                        for (int xx = x - 1; xx < x + 2; xx++) {
                            for (int yy = y - 1; yy < y + 2; yy++) {
                                if (xx < 1 || xx >= pgs.getWidth()-1) {
                                    continue xx_loop;
                                }
                                if (yy < 1 || yy >= pgs.getHeight()-1) {
                                    continue;
                                }
                                if (pgs.getTerrain(xx, yy) == PhysicalGameState.TERRAIN_WALL) {
                                    continue y_loop;
                                }
                            }
                        }

                        // Add valid building position
                        buildPositions.add(new Pair<Integer,Integer>(x, y));
                    }
                }
                if (!buildPositions.isEmpty()) {

                    // Build barrack further away from enemy base
                    if (closestEnemyBase != null) {
                        int cloest_d = Integer.MAX_VALUE;
                        Pair<Integer,Integer> closest_point = null;

                        for (Pair<Integer,Integer> point : buildPositions) {
                            int pointDistance = getPointDistance(point.m_a, point.m_b, closestEnemyBase.getX(), closestEnemyBase.getY());
                            if (pointDistance < cloest_d) {
                                cloest_d = pointDistance;
                                closest_point = point;
                            }
                        }
                        build(u, barracksType, closest_point.m_a, closest_point.m_b);
                    }
                    else {
                        build(u, barracksType, buildPositions.getFirst().m_a, buildPositions.getFirst().m_b);
                    }
                    break;
                }
            }
        }
    }

    public void workersBehavior(Player p, GameState gs, List<Unit> workersCanHarvest,
        List<Unit> bases,
        List<Unit> ebases,
        List<Unit> barracks,
        List<Unit> eranged,
        List<Unit> workers,
        List<Unit> eworkers,
        List<Unit> eunitsbuildings,
        List<Unit> ebuildings,
        List<Unit> allbuildings,
        List<Unit> resources,
        List<Unit> resources_gather
    ) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int resourcesUsed = 0;
        List<Unit> freeWorkers = new LinkedList<>(workersCanHarvest);
        boolean rushMode = false;

        if (workersCanHarvest.isEmpty()) {
            return;
        }

        if ((pgs.getWidth() <= 12 && eranged.isEmpty())) {
            rushMode = true;
        }

        // Make workers defend the nearest base if enemies are nearby it
        for (Unit b : bases) {
            if (getUnitDistance(getClosestUnitType(b, bases), getClosestUnitType(b, eworkers)) < 5 || getUnitDistance(getClosestUnitType(b, bases), getClosestUnitType(b, ebuildings)) < 5 ) {
                rushMode = true;
            }
        }

        // build (a) base(s):
        if (p.getResources() >= baseType.cost + resourcesUsed && !resources_gather.isEmpty() && bases.isEmpty()) {
            Unit u = freeWorkers.remove(0);
            buildBaseAroundNearestResource(u, pgs, bases, ebases, allbuildings, resources_gather);
            resourcesUsed += baseType.cost;
        }

        if (barracks.size() < 2 && !freeWorkers.isEmpty() && !rushMode) {
            // build (a) barrack(s):

            if (p.getResources() >= barracksType.cost + resourcesUsed) {
                if ((barracks.isEmpty() || // Zero barracks currently on field
                (barracks.size() == 1 && p.getResources() > 11))) { // One barrack currently on field
                    Unit u = freeWorkers.remove(0);
                    buildBarracksAroundNearestBase(u, pgs, bases, ebases, allbuildings, resources);
                    resourcesUsed += barracksType.cost;
                }
            }
        }

        if (rushMode) {
            Unit harvestWorker = null;
            
            if (freeWorkers.size() > 0) harvestWorker = freeWorkers.remove(0);
        
            // harvest with only one free worker:
            if (harvestWorker != null) {
                Unit closestBase = getClosestUnitType(harvestWorker, bases);
                Unit closestResource = getClosestUnitType(harvestWorker, resources_gather);
                
                boolean harvestWorkerFree = true;
                if (harvestWorker.getResources() > 0) {
                    if (closestBase != null) {
                        AbstractAction aa = getAbstractAction(harvestWorker);
                        if (aa instanceof Harvest) {
                            Harvest h_aa = (Harvest)aa;
                            if (h_aa.getBase() != closestBase) harvest(harvestWorker, null, closestBase);
                        }
                        else {
                            harvest(harvestWorker, null, closestBase);
                        }
                        harvestWorkerFree = false;
                    }
                }
                else {            
                    if (closestResource!=null && closestBase!=null) {
                        AbstractAction aa = getAbstractAction(harvestWorker);
                        if (aa instanceof Harvest) {
                            Harvest h_aa = (Harvest)aa;
                            if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase) harvest(harvestWorker, closestResource, closestBase);
                        }
                        else {
                            harvest(harvestWorker, closestResource, closestBase);
                        }
                        harvestWorkerFree = false;
                    }
                }
                
                if (harvestWorkerFree) freeWorkers.add(harvestWorker);
            }

            for (Unit u : freeWorkers) meleeUnitBehaviorRush(u, p, gs, eunitsbuildings);
        }
        else {

            // harvest with all the free workers:
            List<Unit> stillFreeWorkers = new LinkedList<>();
            for (Unit u : freeWorkers) {
                Unit closestBase = getClosestUnitType(u, bases);
                Unit closestResource = getClosestUnitType(u, resources_gather);

                boolean workerStillFree = true;
                if (u.getResources() > 0) {
                    if (closestBase != null) {
                        AbstractAction aa = getAbstractAction(u);
                        if (aa instanceof Harvest) {
                            Harvest h_aa = (Harvest)aa;
                            if (h_aa.getBase() != closestBase) {
                                harvest(u, null, closestBase);
                            }
                        } else {
                            harvest(u, null, closestBase);
                        }
                        workerStillFree = false;
                    }
                } else {
                    if (closestResource != null && closestBase != null) {
                        AbstractAction aa = getAbstractAction(u);
                        if (aa instanceof Harvest) {
                            Harvest h_aa = (Harvest)aa;
                            if (h_aa.getTarget() != closestResource || h_aa.getBase() != closestBase) {
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

            for(Unit u:stillFreeWorkers) meleeUnitBehaviorRush(u, p, gs, eunitsbuildings);
        }
    }

    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}
