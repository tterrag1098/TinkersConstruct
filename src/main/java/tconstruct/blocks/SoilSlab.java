package tconstruct.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import tconstruct.common.TRepo;
import tconstruct.library.TConstructRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SoilSlab extends SlabBase
{
    public SoilSlab()
    {
        super(Material.ground);
        this.setCreativeTab(TConstructRegistry.blockTab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons (IIconRegister iconRegister)
    {
        this.blockIcon = iconRegister.registerIcon("tinker:grass_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon (int side, int meta)
    {
        switch (meta % 8)
        {
        case 0:
            return this.blockIcon;// Block.grass.getIcon(1, 0);
        case 1:
            return Blocks.dirt.getIcon(side, 0);
        case 2:
            return Blocks.mycelium.getIcon(1, 0);
        default:
            return TRepo.craftedSoil.getIcon(side, meta - 3);
        }
    }

    @Override
    public void getSubBlocks (Item b, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < 8; iter++)
        {
            list.add(new ItemStack(b, 1, iter));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor ()
    {
        double d0 = 0.5D;
        double d1 = 1.0D;
        return ColorizerGrass.getGrassColor(d0, d1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor (int par1)
    {
        // if (par1 % 8 == 0)
        return this.getBlockColor();
        // return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier (IBlockAccess world, int x, int y, int z)
    {
        int l = 0;
        int i1 = 0;
        int j1 = 0;

        for (int k1 = -1; k1 <= 1; ++k1)
        {
            for (int l1 = -1; l1 <= 1; ++l1)
            {
                int i2 = world.getBiomeGenForCoords(x + l1, z + k1).getBiomeGrassColor(x, y, z);
                l += (i2 & 16711680) >> 16;
                i1 += (i2 & 65280) >> 8;
                j1 += i2 & 255;
            }
        }
        return (l / 9 & 255) << 16 | (i1 / 9 & 255) << 8 | j1 / 9 & 255;
    }
}
