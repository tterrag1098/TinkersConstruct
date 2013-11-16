package tconstruct.blocks.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import tconstruct.library.blocks.InventoryLogic;
import tconstruct.library.util.CoordTuple;
import tconstruct.library.util.IActiveLogic;
import tconstruct.library.util.IFacingLogic;
import tconstruct.library.util.IMasterLogic;
import tconstruct.library.util.IServantLogic;

public class TankMasterLogic implements IMasterLogic
{
    public boolean validStructure;
    public boolean tempValidStructure;
    int internalTemp;
    public int useTime;
    public int fuelGague;
    public int fuelAmount;
    boolean inUse;

    Set<CoordTuple> lavaTanks;
    CoordTuple controller; 
    CoordTuple activeLavaTank;
    CoordTuple electedMaster;
    
    CoordTuple minCoords;
    CoordTuple maxCoords;
    
    public CoordTuple centerPos; // Center bottom air block

    public int[] activeTemps;
    public int[] meltingTemps;
    int tick;

    public ArrayList<FluidStack> moltenMetal = new ArrayList<FluidStack>();
    int maxLiquid;
    int currentLiquid;
    public int layers;
    int slag;

    Random rand = new Random();
    boolean needsUpdate;

    public TankMasterLogic()
    {
        lavaTanks = new HashSet<CoordTuple>();
        
        activeTemps = new int[0];
        meltingTemps = new int[0];
    }
    
    public boolean hasMaster()
    {
        return (electedMaster instanceof CoordTuple);
    }
    
    public CoordTuple getMasterCoords()
    {
        return electedMaster;
    }
    
    public void setMasterCoords(int x, int y, int z)
    {
        electedMaster = new CoordTuple(x, y, z);
    }
    
    public void setMasterCoords(CoordTuple coords)
    {
        this.setMasterCoords(coords.x, coords.y, coords.z);
    }

    @Override
    public void notifyChange (IServantLogic servant, int x, int y, int z)
    {
        needsUpdate = true;
    }
    
    void adjustLayers (int lay, boolean forceAdjust)
    {
        if (lay != layers || forceAdjust)
        {
            needsUpdate = true;
            layers = lay;
            maxLiquid = 20000 * lay;
            int[] tempActive = activeTemps;
            activeTemps = new int[9 * lay];
            int activeLength = tempActive.length > activeTemps.length ? activeTemps.length : tempActive.length;
            System.arraycopy(tempActive, 0, activeTemps, 0, activeLength);

            int[] tempMelting = meltingTemps;
            meltingTemps = new int[9 * lay];
            int meltingLength = tempMelting.length > meltingTemps.length ? meltingTemps.length : tempMelting.length;
            System.arraycopy(tempMelting, 0, meltingTemps, 0, meltingLength);

            ItemStack[] tempInv = inventory;
            inventory = new ItemStack[9 * lay];
            int invLength = tempInv.length > inventory.length ? inventory.length : tempInv.length;
            System.arraycopy(tempInv, 0, inventory, 0, invLength);

            if (activeTemps.length > 0 && activeTemps.length > tempActive.length)
            {
                for (int i = tempActive.length; i < activeTemps.length; i++)
                {
                    activeTemps[i] = 20;
                    meltingTemps[i] = 20;
                }
            }

            if (tempInv.length > inventory.length)
            {
                for (int i = inventory.length; i < tempInv.length; i++)
                {
                    ItemStack stack = tempInv[i];
                    if (stack != null)
                    {
                        float jumpX = rand.nextFloat() * 0.8F + 0.1F;
                        float jumpY = rand.nextFloat() * 0.8F + 0.1F;
                        float jumpZ = rand.nextFloat() * 0.8F + 0.1F;

                        int offsetX = 0;
                        int offsetZ = 0;
                        switch (getRenderDirection())
                        {
                        case 2: // +z
                            offsetZ = -1;
                            break;
                        case 3: // -z
                            offsetZ = 1;
                            break;
                        case 4: // +x
                            offsetX = -1;
                            break;
                        case 5: // -x
                            offsetX = 1;
                            break;
                        }

                        while (stack.stackSize > 0)
                        {
                            int itemSize = rand.nextInt(21) + 10;

                            if (itemSize > stack.stackSize)
                            {
                                itemSize = stack.stackSize;
                            }

                            stack.stackSize -= itemSize;
                            EntityItem entityitem = new EntityItem(worldObj, (double) ((float) xCoord + jumpX + offsetX), (double) ((float) yCoord + jumpY),
                                    (double) ((float) zCoord + jumpZ + offsetZ), new ItemStack(stack.itemID, itemSize, stack.getItemDamage()));

                            if (stack.hasTagCompound())
                            {
                                entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                            }

                            float offset = 0.05F;
                            entityitem.motionX = (double) ((float) rand.nextGaussian() * offset);
                            entityitem.motionY = (double) ((float) rand.nextGaussian() * offset + 0.2F);
                            entityitem.motionZ = (double) ((float) rand.nextGaussian() * offset);
                            worldObj.spawnEntityInWorld(entityitem);
                        }
                    }
                }
            }
        }
    }

}
