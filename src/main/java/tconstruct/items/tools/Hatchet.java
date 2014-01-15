package tconstruct.items.tools;

import tconstruct.common.TRepo;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.HarvestTool;
import tconstruct.util.config.PHConstruct;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Hatchet extends HarvestTool
{
    public Hatchet()
    {
        super(3);
        this.setUnlocalizedName("InfiTool.Axe");
    }

    @Override
    protected Material[] getEffectiveMaterials ()
    {
        return materials;
    }

    @Override
    protected String getHarvestType ()
    {
        return "axe";
    }

    @Override
    public boolean onBlockDestroyed (ItemStack itemstack, World world, Block block, int x, int y, int z, EntityLivingBase player)
    {
        if (block != null && block.func_149688_o() == Material.field_151584_j)
            return false;

        return AbilityHelper.onBlockChanged(itemstack, world, block, x, y, z, player, random);
    }

    static Material[] materials = { Material.field_151575_d, Material.field_151584_j, Material.field_151582_l, Material.field_151594_q, Material.cactus, Material.pumpkin };

    @Override
    public Item getHeadItem ()
    {
        return TRepo.hatchetHead;
    }

    @Override
    public Item getAccessoryItem ()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses (int metadata)
    {
        return 8;
    }

    @Override
    public int getPartAmount ()
    {
        return 2;
    }

    @Override
    public void registerPartPaths (int index, String[] location)
    {
        headStrings.put(index, location[0]);
        brokenPartStrings.put(index, location[1]);
        handleStrings.put(index, location[2]);
    }

    @Override
    public String getIconSuffix (int partType)
    {
        switch (partType)
        {
        case 0:
            return "_axe_head";
        case 1:
            return "_axe_head_broken";
        case 2:
            return "_axe_handle";
        default:
            return "";
        }
    }

    @Override
    public String getEffectSuffix ()
    {
        return "_axe_effect";
    }

    @Override
    public String getDefaultFolder ()
    {
        return "axe";
    }

    @Override
    public boolean isOffhandHandDualWeapon ()
    {
        return PHConstruct.isHatchetWeapon;
    }
}