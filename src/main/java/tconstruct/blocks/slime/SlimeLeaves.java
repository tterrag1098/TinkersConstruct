package tconstruct.blocks.slime;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tconstruct.common.TRepo;
import tconstruct.library.TConstructRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlimeLeaves extends BlockLeaves
{
    private static final String[] fastLeaves = new String[] { "slimeleaves_blue_fast" };
    private static final String[] fancyLeaves = new String[] { "slimeleaves_blue_fancy" };
    @SideOnly(Side.CLIENT)
    private IIcon[] fastIcons;
    @SideOnly(Side.CLIENT)
    private IIcon[] fancyIcons;

    public SlimeLeaves()
    {
        super();
        setCreativeTab(TConstructRegistry.blockTab);
        setLightOpacity(1);
        this.setHardness(0.3f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor ()
    {
        return 0xffffff;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor (int par1)
    {
        return 0xffffff;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier (IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        return 0xffffff;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons (IIconRegister iconRegister)
    {
        this.fastIcons = new IIcon[fastLeaves.length];
        this.fancyIcons = new IIcon[fancyLeaves.length];

        for (int i = 0; i < this.fastIcons.length; i++)
        {
            this.fastIcons[i] = iconRegister.registerIcon("tinker:" + fastLeaves[i]);
            this.fancyIcons[i] = iconRegister.registerIcon("tinker:" + fancyLeaves[i]);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon (int side, int meta)
    {
        int tex = meta % 4;

        if (net.minecraft.client.Minecraft.getMinecraft().gameSettings.fancyGraphics)
            return fancyIcons[tex];
        else
            return fastIcons[tex];
    }

    @Override
    public void getSubBlocks (Item b, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < fastIcons.length; iter++)
        {
            list.add(new ItemStack(b, 1, iter));
        }
    }

    @Override
    public boolean isLeaves (IBlockAccess world, int x, int y, int z)
    {
        return true;
    }

    /* Drops */

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public Item getItemDropped (int par1, Random par2Random, int par3)
    {
        return new ItemStack(TRepo.slimeSapling).getItem();
    }

    /**
     * Drops the block items with a specified chance of dropping the specified
     * items
     */
    @Override
    public void dropBlockAsItemWithChance (World world, int x, int y, int z, int meta, float chance, int fortune)
    {
        if (!world.isRemote)
        {
            int dropChance = 35;

            /*
             * if ((meta & 3) == 3) { j1 = 40; }
             */

            if (fortune > 0)
            {
                dropChance -= 2 << fortune;

                if (dropChance < 15)
                {
                    dropChance = 15;
                }
            }

            if (world.rand.nextInt(dropChance) == 0)
            {
                Item k1 = this.getItemDropped(meta, world.rand, fortune);
                this.dropBlockAsItem(world, x, y, z, new ItemStack(k1, 1, this.damageDropped(meta)));
            }

            dropChance = 80;

            if (fortune > 0)
            {
                dropChance -= 10 << fortune;

                if (dropChance < 20)
                {
                    dropChance = 20;
                }
            }

            if ((meta & 3) == 0 && world.rand.nextInt(dropChance) == 0)
            {
                this.dropBlockAsItem(world, x, y, z, new ItemStack(TRepo.strangeFood, 1, 0));
            }
        }
    }

    @Override
    public String[] func_150125_e ()
    {
        return new String[] { "slime" };
    }
}
