package tconstruct.items.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import tconstruct.common.TRepo;
import tconstruct.entity.projectile.DaggerEntity;
import tconstruct.library.tools.Weapon;

public class Dagger extends Weapon
{
    public Dagger()
    {
        super(1);
        this.setUnlocalizedName("InfiTool.Dagger");
    }

    @Override
    public ItemStack onItemRightClick (ItemStack itemstack, World world, EntityPlayer player)
    {
        ItemStack stack = itemstack.copy();
        if (!world.isRemote)
        {
            DaggerEntity dagger = new DaggerEntity(stack, world, player);
            if (player.capabilities.isCreativeMode)
                dagger.doNotRetrieve = true;
            world.spawnEntityInWorld(dagger);
        }
        itemstack.stackSize--;
        return itemstack;
    }

    @Override
    public ItemStack onEaten (ItemStack itemstack, World world, EntityPlayer player)
    {
        ItemStack stack = itemstack.copy();
        if (!world.isRemote)
        {
            DaggerEntity dagger = new DaggerEntity(stack, world, player);
            world.spawnEntityInWorld(dagger);
        }
        itemstack.stackSize--;
        return itemstack;
    }

    @Override
    public String[] toolCategories ()
    {
        return new String[] { "weapon", "melee", "throwing" };
    }

    @Override
    public EnumAction getItemUseAction (ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    @Override
    public int getMaxItemUseDuration (ItemStack stack)
    {
        return 10;
    }

    public boolean rangedTool ()
    {
        return true;
    }

    @Override
    public String getIconSuffix (int partType)
    {
        switch (partType)
        {
        case 0:
            return "_dagger_blade";
        case 1:
            return "_dagger_blade_broken";
        case 2:
            return "_dagger_handle";
        case 3:
            return "_dagger_accessory";
        default:
            return "";
        }
    }

    @Override
    public String getEffectSuffix ()
    {
        return "_dagger_effect";
    }

    @Override
    public String getDefaultFolder ()
    {
        return "dagger";
    }

    @Override
    public Item getHeadItem ()
    {
        return TRepo.knifeBlade;
    }

    @Override
    public Item getAccessoryItem ()
    {
        return TRepo.crossbar;
    }
}
