package ecgberht;

import bwem.Area;
import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static ecgberht.Ecgberht.getGs;

public class BuildingMap implements Cloneable {

    public final static Map<Area, Set<TilePosition>> tilesArea = new HashMap<>();
    private Player self;
    private int height;
    private int width;
    private String[][] map;
    private BW bw;
    private BWEM bwem;

    BuildingMap(BW bw, Player self, BWEM bwem) {
        this.bw = bw;
        this.self = self;
        this.height = bw.getBWMap().mapHeight();
        this.width = bw.getBWMap().mapWidth();
        this.map = new String[this.height][this.width];
        this.bwem = bwem;
        if (tilesArea.isEmpty()) initTilesArea();
    }

    private BuildingMap(BW bw, Player self, int height, int width, String[][] map, BWEM bwem) {
        this.bw = bw;
        this.self = self;
        this.height = height;
        this.width = width;
        this.map = copyMap(map);
        this.bwem = bwem;
        if (tilesArea.isEmpty()) initTilesArea();
    }

    private void initTilesArea() {
        boolean startOrdered = false;
        boolean naturalOrdered = false;
        TilePosition startTile = self.getStartLocation();
        for (Area a : bwem.getMap().getAreas()) {
            if (!startOrdered && bwem.getMap().getArea(startTile).equals(a)) {
                tilesArea.put(a, new TreeSet<>(new tilesAreaComparator(startTile)));
                startOrdered = true;
            } else if (!naturalOrdered && getGs().naturalArea.equals(a)) {
                tilesArea.put(a, new TreeSet<>(new tilesAreaComparator(getGs().BLs.get(1).getLocation())));
                naturalOrdered = true;
            } else if (a.getBases().size() == 1)
                tilesArea.put(a, new TreeSet<>(new tilesAreaComparator(a.getBases().get(0).getLocation())));
            else tilesArea.put(a, new TreeSet<>(new tilesAreaComparator(a.getTop().toTilePosition())));
        }
        for (int jj = 0; jj < height; jj++) {
            for (int ii = 0; ii < width; ii++) {
                TilePosition x = new TilePosition(ii, jj);
                Area a = bwem.getMap().getArea(x);
                if (a != null) tilesArea.get(a).add(x);
            }
        }
    }

    public Set<TilePosition> getTilesArea(Area area) {
        return tilesArea.get(area);
    }

    private String[][] copyMap(String[][] map) {
        String[][] copiedMap = new String[map.length][map[0].length];
        for (int ii = 0; ii < map.length; ii++) {
            System.arraycopy(map[ii], 0, copiedMap[ii], 0, map[0].length);
        }
        return copiedMap;
    }

    public String[][] getMap() {
        return this.map;
    }

    @Override
    public BuildingMap clone() {
        return new BuildingMap(bw, self, height, width, map, bwem);
    }

    // Generates an initial building map
    void initMap() {
        //Find valid and no valid positions for building
        for (int jj = 0; jj < height; jj++) {
            for (int ii = 0; ii < width; ii++) {
                TilePosition x = new TilePosition(ii, jj);
                //if (bw.getBWMap().isBuildable(x, false)) map[jj][ii] = "6";
                if (bw.getBWMap().isBuildable(x, true)) map[jj][ii] = "6";
                else map[jj][ii] = "0";
            }
        }
        // Finds minerals
        for (MineralPatch resource : bw.getMineralPatches()) {
            TilePosition resourceTile = resource.getTilePosition();
            TilePosition resourceSize = resource.getType().tileSize();
            for (int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
                for (int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) continue;
                    if (!map[i][j].equals("V")) map[i][j] = "M";
                }
            }
        }
        // Finds geysers
        for (VespeneGeyser resource : bw.getVespeneGeysers()) {
            TilePosition resourceTile = resource.getTilePosition();
            TilePosition resourceSize = resource.getType().tileSize();
            for (int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
                for (int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) continue;
                    map[i][j] = "V";
                }
            }
        }
        // Finds weird neutral buildings
        for (Unit resource : bw.getAllUnits()) {
            if (!(resource instanceof SpecialBuilding)) continue;
            TilePosition resourceTile = resource.getTilePosition();
            TilePosition resourceSize = resource.getType().tileSize();
            for (int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
                for (int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) continue;
                    map[i][j] = "E";
                }
            }
        }

        for (Area a : bwem.getMap().getAreas()) {
            for (Base b : a.getBases()) {
                TilePosition starting = b.getLocation();
                for (int i = starting.getY(); i < starting.getY() + UnitType.Terran_Command_Center.tileHeight(); i++) {
                    for (int j = starting.getX(); j < starting.getX() + UnitType.Terran_Command_Center.tileWidth(); j++) {
                        if (i < 0 || i >= height || j < 0 || j >= width) continue;
                        map[i][j] = "E";
                    }
                }
                map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
                map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
                map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
                map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
            }
        }
        map = fillMap(map);
    }

    // Fills the map with the correct values for each tile
    private String[][] fillMap(String[][] map) {
        int height = map.length;
        int width = map[0].length;
        for (int map_x = height - 1; map_x >= 0; jj--) {
            if (!map[map_x][width - 1].equals("M") && !map[map_x][width - 1].equals("V") && !map[map_x][width - 1].equals("0")
                    && !map[map_x][width - 1].equals("E") && !map[map_x][width - 1].equals("B")
                    && (map_x == this.height - 1 || width - 1 == this.width - 1)) {
                map[map_x][width - 1] = "1";
            }
        }
        for (int map_y = width - 1; map_y >= 0; map_y--) {
            if (!map[height - 1][map_y].equals("M") && !map[height - 1][map_y].equals("V") && !map[height - 1][map_y].equals("0")
                    && !map[height - 1][map_y].equals("E") && !map[height - 1][map_y].equals("B")
                    && (height - 1 == this.height - 1 || map_y == this.width - 1)) {
                map[height - 1][map_y] = "1";
            }
        }
        // Sets to "B" adjacent tiles to 0,M,V by the left and top with value "6"
        for (int map_x = height - 1; map_x >= 0; map_x--) {
            for (int map_y = width - 1; map_y >= 0; map_y--) {
                boolean mapTypeEqual_EMV = map[map_x][map_y].equals("E") || map[map_x][map_y].equals("M") || map[map_x][map_y].equals("V");
                boolean currentLeftEqualSix = map_x - 1 >= 0 && map[map_x - 1][map_y].equals("6");
                boolean currentTopEqualSix = map_y - 1 >= 0 && map[map_x][map_y - 1].equals("6");
                boolean currentDiagonallyUpperLeftEqualSix = map_x - 1 >= 0 && map_y - 1 >= 0 && map[map_x - 1][map_y - 1].equals("6");
                boolean currentRightEqualSix = map_x + 1 < height && map[map_x + 1][map_y].equals("6");
                boolean currenBottomEqualSix = map_y + 1 < width && map[map_x][map_y + 1].equals("6");
                boolean currentDiagonallyLowerRightEqualSix = map_x + 1 < height && map_y + 1 < width && map[map_x + 1][map_y + 1].equals("6");
                boolean currentDiagonallyLowerLeftEqualSix = map_x - 1 >= 0 && map_y + 1 < width && map[map_x - 1][map_y + 1].equals("6");
                boolean currentDiagonallyUpperRightEqualSix = map_x + 1 < height && map_y - 1 >= 0 && map[map_x + 1][map_y - 1].equals("6");

                if ( mapTypeEqual_EMV ) {
                    if ( currentLeftEqualSix ) map[map_x - 1][map_y] = "B";
                    if ( currentTopEqualSix ) map[map_x][map_y] = "B";
                    if ( currentDiagonallyUpperLeftEqualSix ) map[map_x - 1][map_y - 1] = "B";
                    if ( currentRightEqualSix ) map[map_x + 1][map_y] = "B";
                    if ( currenBottomEqualSix ) map[map_x][map_y + 1] = "B";
                    if ( currentDiagonallyLowerRightEqualSix ) map[map_x + 1][map_y + 1] = "B";
                    if ( currentDiagonallyLowerLeftEqualSix ) map[map_x - 1][map_y + 1] = "B";
                    if ( currentDiagonallyUpperRightEqualSix ) map[map_x + 1][map_y - 1] = "B";
                }
            }
        }
        
        FactoryChangeMapType factoryChangeMapType = new FactoryChangeMapType();

        ChangeMapType changeMapType_compareWithB0MVE = factoryChangeMapType.getInstance(height,width,"BOMVE",map);
        map = changeMapType_compareWithB0MVE.getMap();
        ChangeMapType changeMapType_compareWithOne = factoryChangeMapType.getInstance(height,width,"ONE",map);
        map = changeMapType_compareWithOne.getMap();
        ChangeMapType changeMapType_compareWithTwo = factoryChangeMapType.getInstance(height,width,"TWO",map);
        map = changeMapType_compareWithTwo.getMap();
        ChangeMapType changeMapType_compareWithThree = factoryChangeMapType.getInstance(height,width,"THREEE",map);
        map = changeMapType_compareWithThree.getMap();
        ChangeMapType changeMapType_compareWithFour = factoryChangeMapType.getInstance(height,width,"FOUR",map);
        map = changeMapType_compareWithFour.getMap();

        return map;
    }

    // Updates a portion of the map around the building
    public void updateMap(TilePosition position, UnitType building, boolean destroyed) {
        TilePosition buildingSize = building.tileSize();
        int tamY = buildingSize.getY();
        int tamX = buildingSize.getX();
        //Updates the map with the next building to be built
        for (int i = position.getY() - 1; i < position.getY() + tamY + 1; i++) {
            for (int j = position.getX() - 1; j < position.getX() + tamX + 1; j++) {
                if (i < 0 || i >= height || j < 0 || j >= width) continue;
                if (destroyed) {
                    if (i == position.getY() - 1 || i == position.getY() + tamY || j == position.getX() - 1
                            || j == position.getX() + tamX) {
                        if (!map[i][j].equals("0")) map[i][j] = "6";
                    } else if (!map[i][j].equals("V")) map[i][j] = "6";
                } else if (i != position.getY() - 1 && i != position.getY() + tamY && j != position.getX() - 1
                        && j != position.getX() + tamX) {
                    if (!map[i][j].equals("M") && !map[i][j].equals("V") && !map[i][j].equals("0")
                            && !map[i][j].equals("E") && !map[i][j].equals("B")) {
                        if (building == UnitType.Terran_Bunker) map[i][j] = "0";
                        else map[i][j] = "E";
                    }
                }
            }
        }
        if (building.canBuildAddon()) {
            for (int i = position.getY() + tamY; i > position.getY() + tamY - 4; i--) {
                for (int j = position.getX() + tamX - 1; j < position.getX() + tamX + 3; j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) continue;
                    if (destroyed) {
                        if (i == position.getY() + tamY - 3 || i == position.getY() + tamY
                                || j == position.getX() + tamX + 2 || j == position.getX() + tamX - 1) {
                            if (!map[i][j].equals("0")) map[i][j] = "6";
                        } else if (!map[i][j].equals("V")) map[i][j] = "6";
                    } else if (i != position.getY() + tamY - 3 && i != position.getY() + tamY
                            && j != position.getX() + tamX + 2 && j != position.getX() + tamX - 1) {
                        if (!map[i][j].equals("M") && !map[i][j].equals("V") && !map[i][j].equals("0")
                                && !map[i][j].equals("E") && !map[i][j].equals("B")) {
                            map[i][j] = "E";
                        }
                    }
                }
            }
        }
        // Finds the corners around the building
        int init_i = 0;
        if (position.getY() - height > 0) init_i = position.getY() - height;
        int end_i = height;
        if (position.getY() + tamY + height < height) end_i = position.getY() + tamY + height;
        int init_j = 0;
        if (position.getX() - width > 0) init_j = position.getX() - width;
        int fin_j = width;
        if (position.getX() + tamX + width < width) fin_j = position.getX() + tamX + width;
        // Generates a submatrix as a portion of the map delimited by the corners and resets the 1,2,3 values for 4
        String[][] submap = new String[end_i - init_i][fin_j - init_j];
        int i = 0;
        int j;
        for (int ii = init_i; ii < end_i; ii++) {
            j = 0;
            for (int jj = init_j; jj < fin_j; jj++) {
                if (map[ii][jj].equals("M") || map[ii][jj].equals("V") || map[ii][jj].equals("0")
                        || map[ii][jj].equals("E") || map[ii][jj].equals("B")) {
                    submap[i][j] = map[ii][jj];
                } else submap[i][j] = "6";
                j++;
            }
            i++;
        }
        submap = fillMap(submap);
        // Updates the map using the submatrix
        i = 0;
        for (int ii = init_i; ii < end_i; ii++) {
            j = 0;
            for (int jj = init_j; jj < fin_j; jj++) {
                map[ii][jj] = submap[i][j];
                j++;
            }
            i++;
        }
    }

    private TilePosition findPositionArea(UnitType buildingType, Area a) {
        TilePosition buildingSize = buildingType.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        if (buildingType.canBuildAddon()) size = Math.max(buildingSize.getY(), buildingSize.getX() + 2);
        Set<TilePosition> tilesToCheck = tilesArea.get(a);
        for (TilePosition t : tilesToCheck) {
            int ii = t.getY();
            int jj = t.getX();
            if (!map[ii][jj].equals("M") && !map[ii][jj].equals("V") && !map[ii][jj].equals("E") && !map[ii][jj].equals("B")
                    && Integer.parseInt(map[ii][jj]) >= size && checkUnitsChosenBuildingGrid(t, buildingType)) {
                return t;
            }
        }
        return null;
    }

    // Finds a valid position in the map for a specific building type starting with a given tileposition, searches owned areas
    public TilePosition findPositionNew(UnitType buildingType, TilePosition starting) {
        if (buildingType == UnitType.Terran_Bunker || buildingType == UnitType.Terran_Missile_Turret)
            return findPosition(buildingType, starting);
        Area find = bwem.getMap().getArea(starting);
        if (find == null) return findPosition(buildingType, starting);
        TilePosition tile = findPositionArea(buildingType, find);
        if (tile != null) return tile;
        for (Base b : getGs().CCs.keySet()) {
            if (find.equals(b.getArea()) || (getGs().fortressSpecialBLs.containsKey(b) && buildingType != UnitType.Terran_Starport))
                continue;
            tile = findPositionArea(buildingType, b.getArea());
            if (tile != null) return tile;
        }
        return findPosition(buildingType, starting);
    }

    // Finds a valid position in the map for a specific building type starting with a given tileposition
    private TilePosition findPosition(UnitType buildingType, TilePosition starting) {
        TilePosition buildingSize = buildingType.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        if (buildingType.canBuildAddon()) size = Math.max(buildingSize.getY(), buildingSize.getX() + 2);
        if (starting == null) starting = getGs().getPlayer().getStartLocation();
        int x = starting.getY();
        int y = starting.getX();
        int[] coord = new int[2];
        int i = 2;
        int j = 2;
        boolean control = false;
        // Finds the first valid tileposition starting around the given tileposition
        while (!control) {
            for (int ii = (x - i); ii <= (x + i); ii++) {
                for (int jj = (y - j); jj <= (y + j); jj++) {
                    if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width) && (!map[ii][jj].equals("M")
                            && !map[ii][jj].equals("V") && !map[ii][jj].equals("E") && !map[ii][jj].equals("B"))
                            && Integer.parseInt(map[ii][jj]) >= size) {
                        if (buildingType == UnitType.Terran_Bunker) {
                            Area bunk = bwem.getMap().getArea(new TilePosition(jj, ii));
                            if (bunk != null && !bunk.equals(bwem.getMap().getArea(self.getStartLocation()))) continue;
                        }
                        if (checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), buildingType)) {
                            coord[0] = ii;
                            coord[1] = jj;
                            control = true;
                            break;
                        }
                    }
                }
                if (control) break;
            }
            i++;
            j++;
        }
        return new TilePosition(coord[1], coord[0]);
    }


    public boolean tileBuildable(TilePosition pos, UnitType type) {
        int x = pos.getY();
        int y = pos.getX();
        int size = Math.max(type.tileSize().getY(), type.tileSize().getX());
        return !map[x][y].equals("M") && !map[x][y].equals("V") && !map[x][y].equals("E") && !map[x][y].equals("B") && Integer.parseInt(map[x][y]) >= size;
    }

    public TilePosition findBunkerPosition(ChokePoint choke) {
        TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        double chokeWidth = Util.getChokeWidth(choke);
        Position starting = choke.getCenter().toPosition();
        int x = starting.toTilePosition().getY();
        int y = starting.toTilePosition().getX();
        int i = 10;
        int j = 10;
        boolean expandBunker = choke.equals(getGs().naturalChoke);
        // Finds the first valid tileposition starting around the given tileposition
        TilePosition position = null;
        double dist = Double.MAX_VALUE;
        for (int ii = (x - i); ii <= (x + i); ii++) {
            for (int jj = (y - j); jj <= (y + j); jj++) {
                if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
                    if ((!map[ii][jj].equals("M") && !map[ii][jj].equals("V") && !map[ii][jj].equals("E")
                            && !map[ii][jj].equals("B")) && Integer.parseInt(map[ii][jj]) >= size) {
                        Area area = bwem.getMap().getArea(new TilePosition(jj, ii));
                        if (area != null && area.equals(getGs().naturalArea) && !expandBunker) continue;
                        if (area != null && !area.equals(getGs().naturalArea) && expandBunker) continue;
                        if (checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
                            TilePosition newPosition = new TilePosition(jj, ii);
                            Position centerBunker = Util.getUnitCenterPosition(newPosition.toPosition(), UnitType.Terran_Bunker);
                            if (chokeWidth <= 64.0 && Util.broodWarDistance(choke.getCenter().toPosition(), centerBunker) <= 64)
                                continue;
                            double newDist = Util.broodWarDistance(centerBunker, starting);
                            if (position == null || newDist < dist) {
                                position = newPosition;
                                dist = newDist;
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private boolean checkUnitsChosenBuildingGrid(TilePosition BL, UnitType type) {
        try {
            Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
            Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
            List<Unit> blockers = Util.getUnitsInRectangle(topLeft, bottomRight); // Test
            if (blockers.isEmpty() && !getGs().getGame().canBuildHere(BL, type)) return false;
            if (blockers.isEmpty()) return true;
            if (blockers.size() > 1) return false;
            else {
                Unit blocker = blockers.get(0);
                return blocker instanceof PlayerUnit && ((PlayerUnit) blocker).getPlayer().getId() == self.getId() && blocker instanceof Worker &&
                        ((Worker) blocker).getBuildType() == type;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public TilePosition findBunkerPositionAntiPool() {
        TilePosition starting = getGs().MBs.iterator().next().getTilePosition();
        TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        int x = starting.getY();
        int y = starting.getX();
        ChokePoint choke = getGs().mainChoke;
        int i = 4;
        int j = 4;
        //Finds the first valid tileposition starting around the given tileposition
        TilePosition bunkerPlace = null;
        double dist = Double.MAX_VALUE;
        for (int ii = (x - i); ii <= (x + i); ii++) {
            for (int jj = (y - j); jj <= (y + j); jj++) {
                if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
                    if ((!map[ii][jj].equals("M") && !map[ii][jj].equals("V") && !map[ii][jj].equals("E")
                            && !map[ii][jj].equals("B")) && Integer.parseInt(map[ii][jj]) >= size) {
                        if (bwem.getMap().getArea(new TilePosition(jj, ii)).equals(getGs().naturalArea)) continue;
                        if (checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
                            TilePosition newPosition = new TilePosition(jj, ii);
                            double newDist = Util.broodWarDistance(Util.getUnitCenterPosition(newPosition.toPosition(), UnitType.Terran_Bunker), choke.getCenter().toPosition());
                            if (bunkerPlace == null || newDist < dist) {
                                bunkerPlace = newPosition;
                                dist = newDist;
                            }
                        }
                    }
                }
            }
        }
        return bunkerPlace;
    }

    // Writes the map to a file
    public void writeMap(String fileName) {
        FileWriter sw = null;
        try {
            sw = new FileWriter(fileName);
            for (int ii = 0; ii < height; ii++) {
                for (int jj = 0; jj < width; jj++) {
                    try {
                        sw.write(map[ii][jj]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ii != height - 1) {
                    try {
                        sw.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class tilesAreaComparator implements Comparator<TilePosition> {

        private TilePosition center;

        private tilesAreaComparator(TilePosition center) {
            this.center = center;
        }

        @Override
        public int compare(TilePosition o1, TilePosition o2) {
            if (o1.equals(o2)) return 0;
            double dist1 = o1.getDistance(center);
            double dist2 = o2.getDistance(center);
            return dist1 < dist2 ? -1 : 1;
        }
    }
}

public abstract class ChangeMapType{
    protected String[][] map;
    protected height;
    protected width;
    public String[][] getMap() {
        return map;
        }     
}

public class FactoryChangeMapType{
    public ChangeMapType getInstance(int height, int width, String condition, String[][] map){
        if(condition.equals("B0MVE")) return new ChangeMapType_compareWithB0MVE(height,width,map);
        else if(condition.equals("ONE")) return new ChangeMapType_compareWithOne(height,width,map);
        else if(condition.equals("TWO")) return new ChangeMapType_compareWithTwo(height,width,map);
        else if(condition.equals("THREE")) return new ChangeMapType_compareWithThree(height,width,map);
        else if(condition.equals("FOUR")) return new ChangeMapType_compareWithFour(height,width,map);
    }
}

public class ChangeMapType_compareWithB0MVE extends ChangeMapType{
    public ChangeMapType_compareWithB0MVE(int map_height, int map_width, String[][] mapping){
        map = mapping;
        height = map_height;
        width = map_width;

        for (int map_x = height - 1; map_x > 0; map_x--) {
            for (int map_y = width - 1; map_y > 0; map_y--) {
                boolean condition_compareWithB0MVE = ( map[map_x][map_y].equals("B") || map[map_x][map_y].equals("0") || map[map_x][map_y].equals("M")
                                                || map[map_x][map_y].equals("V") || map[map_x][map_y].equals("E") );
                if ( condition_compareWithB0MVE ) {
                    if (map[map_x - 1][map_y].equals("6")) map[map_x - 1][map_yi] = "1";
                    if (map[map_x][map_y - 1].equals("6")) map[map_x][map_y - 1] = "1";
                    if (map[map_x - 1][map_y - 1].equals("6")) map[map_x - 1][map_y - 1] = "1";
                }
            }
        }
    }
}
public class ChangeMapType_compareWithOne extends ChangeMapType{
    public ChangeMapType_compareWithOne(int map_height, int map_width, String[][] mapping){
        map = mapping;
        height = map_height;
        width = map_width;

        for (int map_x = height - 1; map_x > 0; map_x--) {
            for (int map_y = width - 1; map_y > 0; map_y--) {
                boolean condition_compareWithOne = map[map_x][map_y].equals("1");
                if ( condition_compareWithOne ) {
                    if (map[map_x - 1][map_y].equals("6")) map[map_x - 1][map_yi] = "2";
                    if (map[map_x][map_y - 1].equals("6")) map[map_x][map_y - 1] = "2";
                    if (map[map_x - 1][map_y - 1].equals("6")) map[map_x - 1][map_y - 1] = "2";
                }
            }
        }
    }
}
public class ChangeMapType_compareWithTwo extends ChangeMapType{
    public ChangeMapType_compareWithTwo(int map_height, int map_width, String[][] mapping){
        map = mapping;
        height = map_height;
        width = map_width;

        for (int map_x = height - 1; map_x > 0; map_x--) {
            for (int map_y = width - 1; map_y > 0; map_y--) {
                boolean condition_compareWithTwo = map[map_x][map_y].equals("2");
                if ( condition_compareWithTwo ) {
                    if (map[map_x - 1][map_y].equals("6")) map[map_x - 1][map_yi] = "3";
                    if (map[map_x][map_y - 1].equals("6")) map[map_x][map_y - 1] = "3";
                    if (map[map_x - 1][map_y - 1].equals("6")) map[map_x - 1][map_y - 1] = "3";
                }
            }
        }
    }
}
public class ChangeMapType_compareWithThree extends ChangeMapType{
    public ChangeMapType_compareWithThree(int map_height, int map_width, String[][] mapping){
        map = mapping;
        height = map_height;
        width = map_width;

        for (int map_x = height - 1; map_x > 0; map_x--) {
            for (int map_y = width - 1; map_y > 0; map_y--) {
                boolean condition_compareWithThree = map[map_x][map_y].equals("3");
                if ( condition_compareWithThree ) {
                    if (map[map_x - 1][map_y].equals("6")) map[map_x - 1][map_yi] = "3";
                    if (map[map_x][map_y - 1].equals("6")) map[map_x][map_y - 1] = "3";
                    if (map[map_x - 1][map_y - 1].equals("6")) map[map_x - 1][map_y - 1] = "3";
                }
            }
        }
    }
}

public class ChangeMapType_compareWithFour extends ChangeMapType{
    public ChangeMapType_compareWithFour(int map_height, int map_width, String[][] mapping){
        map = mapping;
        height = map_height;
        width = map_width;

        for (int map_x = height - 1; map_x > 0; map_x--) {
            for (int map_y = width - 1; map_y > 0; map_y--) {
                boolean condition_compareWithFour = map[map_x][map_y].equals("4");
                if ( condition_compareWithThree ) {
                    if (map[map_x - 1][map_y].equals("6")) map[map_x - 1][map_yi] = "4";
                    if (map[map_x][map_y - 1].equals("6")) map[map_x][map_y - 1] = "4";
                    if (map[map_x - 1][map_y - 1].equals("6")) map[map_x - 1][map_y - 1] = "4";
                }
            }
        }
    }
}
