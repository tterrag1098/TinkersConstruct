package tconstruct.items.tools;

import tconstruct.common.TRepo;
import tconstruct.library.tools.HarvestTool;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class Pickaxe extends HarvestTool
{
    public Pickaxe()
    {
        super(1);
        this.setUnlocalizedName("InfiTool.Pickaxe");
    }

    @Override
    protected String getHarvestType ()
    {
        return "pickaxe";
    }

    @Override
    protected Material[] getEffectiveMaterials ()
    {
        return materials;
    }

    static Material[] materials = new Material[] { Material.rock, Material.field_151573_f, Material.ice, Material.field_151592_s, Material.piston, Material.field_151574_g, Material.field_151594_q };

    @Override
    public Item getHeadItem ()
    {
        return TRepo.pickaxeHead;
    }

    @Override
    public Item getAccessoryItem ()
    {
        return TRepo.binding;
    }

    @Override
    public String getIconSuffix (int partType)
    {
        switch (partType)
        {
        case 0:
            return "_pickaxe_head";
        case 1:
            return "_pickaxe_head_broken";
        case 2:
            return "_pickaxe_handle";
        case 3:
            return "_pickaxe_accessory";
        default:
            return "";
        }
    }

    @Override
    public String getEffectSuffix ()
    {
        return "_pickaxe_effect";
    }

    @Override
    public String getDefaultFolder ()
    {
        return "pickaxe";
    }

    @Override
    public boolean isOffhandHandDualWeapon ()
    {
        return false;
    }
}