package tconstruct.library.component;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import tconstruct.TConstruct;
import tconstruct.library.util.*;

public class TankLayerScan extends LogicComponent
{
    protected TileEntity master;
    protected IMasterLogic imaster;
    protected Block[] scanBlocks;
    protected CoordTuple masterCoord;

    protected boolean completeStructure;
    
    protected int minX;
    protected int minY;
    protected int minZ;
    protected int maxX;
    protected int maxY;
    protected int maxZ;

    protected int bricks = 0;
    protected int airBlocks = 0;
    protected int structureTop = 0;
    protected HashSet<CoordTuple> layerBlockCoords = new HashSet<CoordTuple>();
    protected HashSet<CoordTuple> layerAirCoords = new HashSet<CoordTuple>();

    public ArrayList<CoordTuple> blockCoords = new ArrayList<CoordTuple>();
    public ArrayList<CoordTuple> airCoords = new ArrayList<CoordTuple>();
    protected ArrayList<int[]> validAirCoords = new ArrayList<int[]>();
    protected CoordTuple returnStone;

    private static boolean debug = System.getenv("DBG_MANTLE_TankLayerScan") != null;
    private static int MAX_LAYER_RECURSION_DEPTH = System.getProperty("os.arch").equals("amd64") ? 4000 : 2000; // Recursion causes overflows on 32-bit, so reduce if not 64-bit

    public TankLayerScan(TileEntity te, Block... ids)
    {
        assert te instanceof IMasterLogic : "TileEntity must be an instance of IMasterLogic";
        master = te;
        imaster = (IMasterLogic) te;
        scanBlocks = ids;
        masterCoord = new CoordTuple(te.xCoord, te.yCoord, te.zCoord);
        validAirCoords.add(new int[] { 1, 0 });
        validAirCoords.add(new int[] { -1, 0 });
        validAirCoords.add(new int[] { 0, 1 });
        validAirCoords.add(new int[] { 0, -1 });
        if (debug)
        {
            TConstruct.logger.info("In debug mode: " + this);
            TConstruct.logger.info("Using recursion size " + MAX_LAYER_RECURSION_DEPTH + " on JVM arch " + System.getProperty("os.arch"));
        }
    }

    public void checkValidStructure ()
    {
        bricks = 0;
        airBlocks = 0;
        blockCoords.clear();
        airCoords.clear();
        boolean validAir = false;
        //Check for air space in front of and behind the structure
        byte dir = getDirection();
        switch (dir)
        {
        case 2: // +z
        case 3: // -z
            if (checkAir(master.xCoord, master.yCoord, master.zCoord - 1) && checkAir(master.xCoord, master.yCoord, master.zCoord + 1))
                validAir = true;
            break;
        case 4: // +x
        case 5: // -x
            if (checkAir(master.xCoord - 1, master.yCoord, master.zCoord) && checkAir(master.xCoord + 1, master.yCoord, master.zCoord))
                validAir = true;
            break;
        }

        //Recurse the structure
        boolean validBlocks = false;

        int xPos = 0, zPos = 0;
        if (dir == 2)
            xPos = 1;
        if (dir == 3)
            xPos = -1;
        if (dir == 4)
            zPos = -1;
        if (dir == 5)
            zPos = 1;

        returnStone = new CoordTuple(master.xCoord - xPos, master.yCoord, master.zCoord - zPos);
//        if (initialRecurseLayer(master.xCoord + xPos, master.yCoord, master.zCoord + zPos))
        if (initialLayerScan(master.xCoord, master.yCoord, master.zCoord, ForgeDirection.OPPOSITES[dir]))
        {
            xPos = 0;
            zPos = 0;
            switch (dir)
            {
            case 2: // +z
                zPos = 1;
                break;
            case 3: // -z
                zPos = -1;
                break;
            case 4: // +x
                xPos = 1;
                break;
            case 5: // -x
                xPos = -1;
                break;
            }
            
            minX = master.xCoord;
            minY = master.yCoord;
            minZ = master.zCoord;
            
            maxX = minX;
            maxY = minY;
            maxZ = minZ;
            
            for (CoordTuple coord : blockCoords)
            {
                if (coord.x < minX)
                    minX = coord.x;
                if (coord.y < minY)
                    minY = coord.y;
                if (coord.z < minZ)
                    minZ = coord.z;
                
                if (coord.x > maxX)
                    maxX = coord.x;
                if (coord.y > maxY)
                    maxY = coord.y;
                if (coord.z > maxZ)
                    maxZ = coord.z;
            }
            if (!world.isRemote && debug)
                TConstruct.logger.info("Bricks in recursion: " + blockCoords.size());
            blockCoords.clear();
            bricks = 0;

            //Does the actual adding of blocks in the ring
//            boolean sealed = floodTest(master.xCoord + xPos, master.yCoord, master.zCoord + zPos);
            boolean sealed = floodScanTest(master.xCoord + xPos, master.yCoord, master.zCoord + zPos);
            if (!world.isRemote && debug)
            {
                TConstruct.logger.info("Air in ring: " + airBlocks);
                TConstruct.logger.info("Bricks in ring: " + bricks);
            }

            if (sealed)
            {
                blockCoords.add(new CoordTuple(master.xCoord, master.yCoord, master.zCoord)); //Don't forget me!
                layerAirCoords = new HashSet<CoordTuple>(airCoords);
                layerBlockCoords = new HashSet<CoordTuple>(blockCoords);

                int lowY = recurseStructureDown(master.yCoord - 1);
                if (lowY != -1)
                {
                    completeStructure = true;
                    structureTop = recurseStructureUp(master.yCoord + 1);
                    finalizeStructure();

                    if (!world.isRemote && debug)
                    {
                        TConstruct.logger.info("Air in structure: " + airCoords.size());
                        TConstruct.logger.info("Bricks in structure: " + blockCoords.size());
                    }
                }
            }
        }
    }

    //@SuppressWarnings({ "unchecked" })
    protected void finalizeStructure ()
    {
        Collections.sort(blockCoords, new CoordTupleSort());
        Collections.sort(airCoords, new CoordTupleSort());

        for (CoordTuple coord : blockCoords)
        {
            TileEntity servant = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            if (servant instanceof IServantLogic)
                ((IServantLogic) servant).verifyMaster(imaster, world, master.xCoord, master.yCoord, master.zCoord);
        }
    }

    public boolean isComplete ()
    {
        return completeStructure;
    }

    public int getAirLayerSize ()
    {
        return layerAirCoords.size();
    }

    public int getAirSize ()
    {
        return airBlocks;
    }

    public CoordTuple getAirByIndex (int index)
    {
        if (index >= airCoords.size() || index < 0)
            return null;

        return airCoords.get(index);
    }

    private byte getDirection ()
    {
        if (master instanceof IFacingLogic)
            return ((IFacingLogic) master).getRenderDirection();
        return 0;
    }

    protected boolean checkAir (int x, int y, int z)
    {
        Block block = Block.blocksList[world.getBlockId(x, y, z)];
        if (block == null || block.isAirBlock(world, x, y, z))// || block == TContent.tankAir)
            return true;

        return false;
    }

    protected boolean checkServant (int x, int y, int z)
    {
        Block block = Block.blocksList[world.getBlockId(x, y, z)];
        if (block == null || block.isAirBlock(world, x, y, z) || !isValidBlock(x, y, z))
            return false;

        if (!block.hasTileEntity(world.getBlockMetadata(x, y, z)))
            return false;

        TileEntity be = world.getBlockTileEntity(x, y, z);
        if (be instanceof IServantLogic)
            return ((IServantLogic) be).setPotentialMaster(this.imaster, this.world, x, y, z);

        return false;
    }

    protected boolean initialRecurseLayer (int x, int y, int z)
    {
        if (bricks > MAX_LAYER_RECURSION_DEPTH)
            return false;

        CoordTuple keystone = new CoordTuple(x, y, z);
        if (keystone.equals(returnStone))
            return true;

        for (int xPos = -1; xPos <= 1; xPos++)
        {
            for (int zPos = -1; zPos <= 1; zPos++)
            {
                CoordTuple coord = new CoordTuple(x + xPos, y, z + zPos);
                if (!blockCoords.contains(coord))
                {
                    if (isValidBlock(x + xPos, y, z + zPos))
                    {
                        bricks++;
                        blockCoords.add(coord);
                        return initialRecurseLayer(x + xPos, y, z + zPos);
                    }
                }
            }
        }

        return false;
    }
    
    protected boolean initialLayerScan (int x, int y, int z, int dir)
    {
        Set<CoordTuple> scanned = new HashSet<CoordTuple>();
        int floorLevel = y;
        int minX, maxX = x;
        int minZ, maxZ = z;
        int scanX, scanY, scanZ = 0;
        int walkDir, scanDir = 0;
        
        CoordTuple currentCoord;
        CoordTuple lastCoord;
        
        blockCoords.clear();
        
        // First Check: Inside floor
        currentCoord = new CoordTuple(x + dirOffsetX(dir), y, z + dirOffsetZ(dir));
        scanned.add(currentCoord);
        if (checkAir(currentCoord.x, currentCoord.y, currentCoord.z))
        {
            // Check the floor for air||structure
            scanY = currentCoord.y;
            while (floorLevel == y)
            {
                scanY = currentCoord.y - 1;
                
                // Void age sanity check
                if (scanY <= 1)
                    return false;
                
                if (!checkAir(currentCoord.x, scanY, currentCoord.z))
                {
                    if (isValidBlock(currentCoord.x, scanY, currentCoord.z))
                    {
                        floorLevel = scanY;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
        
        // Turn right
        walkDir = ForgeDirection.ROTATION_MATRIX[1][dir];
        
        currentCoord = new CoordTuple(currentCoord.x + dirOffsetX(walkDir), y, currentCoord.z + dirOffsetZ(walkDir));
        
        while (currentCoord != null)
        {
            // Did we walk into a wall?
            if (isValidBlock(currentCoord.x, y, currentCoord.z))
            {
                // We dun did dat
                blockCoords.add(currentCoord);
                
                //While we're here, let's check if we have anything useful here
                if (currentCoord.x == x && currentCoord.z == z)
                {
                    // We done did it! We walked right into the controller
                    return true;
                }                
                
                // Step back
                currentCoord = new CoordTuple(currentCoord.x + dirOffsetX(ForgeDirection.OPPOSITES[walkDir]), y, currentCoord.z + dirOffsetZ(ForgeDirection.OPPOSITES[walkDir]));
                
                // Turn left!
                walkDir = ForgeDirection.ROTATION_MATRIX[0][walkDir];
                

                // No? Let's start over then! Step forward
                currentCoord = new CoordTuple(currentCoord.x + dirOffsetX(walkDir), y, currentCoord.z + dirOffsetZ(walkDir));
                continue;
            }
            
            // Floor check
            if (!isValidBlock(currentCoord.x, floorLevel, currentCoord.z))
            {
                return false;
            }
            // Check right-hand wall
            scanDir = ForgeDirection.ROTATION_MATRIX[1][walkDir];
            scanX = currentCoord.x + dirOffsetX(scanDir);
            scanZ = currentCoord.z + dirOffsetZ(scanDir);
            if (!isValidBlock(scanX, y, scanZ))
            {
                if (checkAir(scanX, y, scanZ))
                {
                    // Wall is air, turn right.
                    walkDir = scanDir;
                    scanned.add(currentCoord);
                    currentCoord = new CoordTuple(scanX + dirOffsetX(walkDir), y, scanZ + dirOffsetZ(walkDir));
                    continue;
                }
                else
                {
                    // Wall is...something? No no no, something about this is just all wrong
                    return false;
                }
            }
            else
            {
                // Wall is wall! Take a step forward
                scanned.add(currentCoord);
                blockCoords.add(new CoordTuple(scanX, y, scanZ));
                currentCoord = new CoordTuple(currentCoord.x + dirOffsetX(walkDir), y, currentCoord.z + dirOffsetZ(walkDir));
                
                // Are we there yet?
                if (scanX == x && scanZ == z)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static CoordTuple[] getXZNeighbors (CoordTuple coord)
    {
        return new CoordTuple[]
                {
                    new CoordTuple(coord.x - 1, coord.y, coord.z    ),
                    new CoordTuple(coord.x    , coord.y, coord.z - 1),
                    new CoordTuple(coord.x    , coord.y, coord.z + 1),
                    new CoordTuple(coord.x + 1, coord.y, coord.z    )
                };
    }
    
    private static int dirOffsetX (int dir)
    {
        switch (dir)
        {

        case 4:
            return -1;
        case 5:
            return 1;
        }
        
        return 0;
    }
    
    private static int dirOffsetZ (int dir)
    {
        switch (dir)
        {
        case 2:
            return 1;
        case 3:
            return -1;
        }
        return 0;
    }

    protected boolean isValidBlock (int x, int y, int z)
    {
        Block block = Block.blocksList[world.getBlockId(x, y, z)];
        if (block != null)
        {
            for (int i = 0; i < scanBlocks.length; i++)
            {
                if (block == scanBlocks[i])
                    return true;
            }
        }
        return false;
    }

    protected boolean floodTest (int x, int y, int z)
    {
        if (airBlocks > MAX_LAYER_RECURSION_DEPTH)
            return false;

        for (int[] offset : validAirCoords)
        {
            CoordTuple coord = new CoordTuple(x + offset[0], y, z + offset[1]);
            if (!airCoords.contains(coord))
            {
                if (checkAir(x + offset[0], y, z + offset[1]))
                {
                    addAirBlock(coord.x, y, coord.z);
                    floodTest(x + offset[0], y, z + offset[1]);
                }
                else if (!blockCoords.contains(coord) && checkServant(x + offset[0], y, z + offset[1]))
                {
                    bricks++;
                    blockCoords.add(coord);
                }
            }
        }
        return true;
    }
    
    // Pass first air block (adjacent to controller)
    protected boolean floodScanTest (int x, int y, int z)
    {
//        Set<CoordTuple> airBlocks = new HashSet<CoordTuple>();
        ArrayDeque<CoordTuple> scan = new ArrayDeque<CoordTuple>();
        
        CoordTuple scanCoord;
        scan.push(new CoordTuple(x, y, z));
        
        while (!scan.isEmpty())
        {
            scanCoord = scan.pop();
            
            if (scanCoord.x < minX || scanCoord.y < minY || scanCoord.z < minZ || scanCoord.x > maxX || scanCoord.y > maxY || scanCoord.z > maxZ)
            {
                return false;
            }
            
            if (airCoords.contains(scanCoord))
            {
                continue;
            }
            if (checkAir(scanCoord.x, scanCoord.y, scanCoord.z))
            {
                airCoords.add(scanCoord);
                ++airBlocks;
                scan.addAll(Arrays.asList(getXZNeighbors(scanCoord)));
            }
            else
            {
                if (!blockCoords.contains(scanCoord))
                {
                    ++bricks;
                    blockCoords.add(scanCoord);
                }
            }
        }
        
        return true;
    }

    public int recurseStructureDown (int y)
    {
        Iterator i = layerAirCoords.iterator();
        if (i.hasNext())
        {
            CoordTuple coord = (CoordTuple) i.next();
            if (checkAir(coord.x, y, coord.z))
            {
                boolean valid = true;
                addAirBlock(coord.x, y, coord.z); //Don't skip first one

                //Air blocks
                while (i.hasNext())
                {
                    coord = (CoordTuple) i.next();
                    if (checkAir(coord.x, y, coord.z))
                    {
                        addAirBlock(coord.x, y, coord.z);
                    }
                    else
                    {
                        valid = false;
                        break;
                    }
                }

                //Bricks
                i = layerBlockCoords.iterator();
                while (i.hasNext())
                {
                    coord = (CoordTuple) i.next();
                    if (checkServant(coord.x, y, coord.z))
                        blockCoords.add(new CoordTuple(coord.x, y, coord.z));

                    else
                    {
                        valid = false;
                        break;
                    }
                }

                if (valid)
                    return recurseStructureDown(y - 1);
            }
            else if (checkServant(coord.x, y, coord.z))
            {
                //Bottom floor. All blocks, please vacate the elevator
                boolean valid = true;
                while (i.hasNext())
                {
                    coord = (CoordTuple) i.next();

                    if (checkServant(coord.x, y, coord.z))
                        blockCoords.add(new CoordTuple(coord.x, y, coord.z));

                    else
                    {
                        valid = false;
                        break;
                    }
                }
                if (valid)
                    return y + 1;
            }
        }

        return -1;
    }

    public int recurseStructureUp (int y)
    {
        Iterator i = layerBlockCoords.iterator();
        if (i.hasNext())
        {
            CoordTuple coord = (CoordTuple) i.next();
            if (checkServant(coord.x, y, coord.z))
            {
                boolean valid = true;

                //Bricks
                while (i.hasNext())
                {
                    coord = (CoordTuple) i.next();
                    if (checkServant(coord.x, y, coord.z))
                        blockCoords.add(new CoordTuple(coord.x, y, coord.z));

                    else
                    {
                        valid = false;
                        break;
                    }
                }

                //Air blocks
                if (valid)
                {
                    i = layerAirCoords.iterator();
                    while (i.hasNext())
                    {
                        coord = (CoordTuple) i.next();
                        if (checkAir(coord.x, y, coord.z))
                        {
                            addAirBlock(coord.x, y, coord.z);
                        }
                        else
                        {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid)
                    recurseStructureUp(y + 1);
            }
        }
        return y - 1;
    }

    protected void addAirBlock (int x, int y, int z)
    {
        airBlocks++;
        airCoords.add(new CoordTuple(x, y, z));
    }

    /* Checking for valid structures */

    public void recheckStructure ()
    {
        System.out.println("Rechecking structure");
        int height = -1;
        for (CoordTuple coord : blockCoords)
        {
            TileEntity servant = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            boolean canPass = false;
            if (servant instanceof IServantLogic)
            {
                canPass = ((IServantLogic) servant).verifyMaster(imaster, world, master.xCoord, master.yCoord, master.zCoord);
            }
            else
            {
                canPass = (coord.x > minX && coord.x < maxX && coord.z > minZ && coord.z < maxZ) && (checkAir(coord.x, coord.y, coord.z) || isValidBlock(coord.x, coord.y, coord.z));
            }
            if (canPass)
            {
                height = Math.max(height, coord.y);
            }
            else
            {
                System.out.println("Coord: " + coord);
                height = coord.y;
                break;
            }
        }

        if (height != -1)
        {
            if (height < master.yCoord)
                invalidateStructure();
            else
                invalidateBlocksAbove(height);
        }
        else
        {
            if (structureTop == 0) //Workaround for missing data
            {
                for (CoordTuple coord : blockCoords)
                    structureTop = coord.y;
            }
            structureTop = recurseStructureUp(structureTop + 1);
            finalizeStructure();
        }
    }

    protected void invalidateStructure ()
    {
        completeStructure = false;
        for (CoordTuple coord : blockCoords)
        {
            TileEntity servant = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            if (servant instanceof IServantLogic)
                ((IServantLogic) servant).invalidateMaster(imaster, world, master.xCoord, master.yCoord, master.zCoord);
        }
        master.worldObj.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
    }

    protected void invalidateBlocksAbove (int height)
    {
        for (CoordTuple coord : blockCoords)
        {
            if (coord.y < height)
                continue;

            TileEntity servant = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            if (servant instanceof IServantLogic)
                ((IServantLogic) servant).invalidateMaster(imaster, world, master.xCoord, master.yCoord, master.zCoord);
        }
    }

    /** Do any necessary cleanup here. Remove air blocks, invalidate servants, etc */
    public void cleanup ()
    {
        Iterator i = blockCoords.iterator();
        while (i.hasNext())
        {
            CoordTuple coord = (CoordTuple) i.next();
            TileEntity te = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            if (te != null && te instanceof IServantLogic)
            {
                ((IServantLogic) te).invalidateMaster(imaster, world, master.xCoord, master.yCoord, master.zCoord);
            }
        }
    }

    /* NBT */
    @Override
    public void readFromNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        NBTTagList layerAir = tags.getTagList("AirLayer");
        if (layerAir != null)
        {
            layerAirCoords.clear();

            for (int i = 0; i < layerAir.tagCount(); ++i)
            {
                NBTTagIntArray tag = (NBTTagIntArray) layerAir.tagAt(i);
                int[] coord = tag.intArray;
                layerAirCoords.add(new CoordTuple(coord[0], coord[1], coord[2]));
            }
        }

        NBTTagList blocks = tags.getTagList("Blocks");
        if (blocks != null)
        {
            blockCoords.clear();

            for (int i = 0; i < blocks.tagCount(); ++i)
            {
                NBTTagIntArray tag = (NBTTagIntArray) blocks.tagAt(i);
                int[] coord = tag.intArray;
                blockCoords.add(new CoordTuple(coord[0], coord[1], coord[2]));
            }
        }

        NBTTagList air = tags.getTagList("Air");
        if (air != null)
        {
            airCoords.clear();

            for (int i = 0; i < air.tagCount(); ++i)
            {
                NBTTagIntArray tag = (NBTTagIntArray) air.tagAt(i);
                int[] coord = tag.intArray;
                airCoords.add(new CoordTuple(coord[0], coord[1], coord[2]));
            }
        }
        structureTop = tags.getInteger("structureTop");
    }

    public void readNetworkNBT (NBTTagCompound tags)
    {
        completeStructure = tags.getBoolean("Complete");
    }

    @Override
    public void writeToNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        NBTTagList layerAir = new NBTTagList();
        for (CoordTuple coord : layerAirCoords)
        {
            layerAir.appendTag(new NBTTagIntArray("coord", new int[] { coord.x, coord.y, coord.z }));
        }
        tags.setTag("AirLayer", layerAir);

        NBTTagList blocks = new NBTTagList();
        for (CoordTuple coord : blockCoords)
        {
            blocks.appendTag(new NBTTagIntArray("coord", new int[] { coord.x, coord.y, coord.z }));
        }
        tags.setTag("Blocks", blocks);

        NBTTagList air = new NBTTagList();
        for (CoordTuple coord : airCoords)
        {
            air.appendTag(new NBTTagIntArray("coord", new int[] { coord.x, coord.y, coord.z }));
        }
        tags.setTag("Air", air);
        tags.setInteger("structureTop", structureTop);
    }

    public void writeNetworkNBT (NBTTagCompound tags)
    {
        tags.setBoolean("Complete", completeStructure);
    }
}
