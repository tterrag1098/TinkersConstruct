package tconstruct.blocks;

import java.util.List;

import mantle.blocks.MantleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tconstruct.library.TConstructRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlabBase extends MantleBlock
{
    Block modelBlock;
    int startingMeta;
    int totalSize;

    public SlabBase(Material material)
    {
        super(material);
        this.setCreativeTab(TConstructRegistry.blockTab);
    }

    public SlabBase(Material material, Block model, int meta, int totalSize)
    {
        super(material);
        this.setCreativeTab(TConstructRegistry.blockTab);
        this.modelBlock = model;
        this.startingMeta = meta;
        this.totalSize = totalSize;
    }

    @Override
    public void addCollisionBoxesToList (World world, int x, int y, int z, AxisAlignedBB axisalignedbb, List arraylist, Entity entity)
    {
        setBlockBoundsBasedOnState(world, x, y, z);
        super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, entity);
    }

    @Override
    public void setBlockBoundsForItemRender ()
    {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    @Override
    public void setBlockBoundsBasedOnState (IBlockAccess world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z) / 8;
        float minY = meta == 1 ? 0.5F : 0.0F;
        float maxY = meta == 1 ? 1.0F : 0.5F;
        setBlockBounds(0.0F, minY, 0F, 1.0F, maxY, 1.0F);
    }

    @Override
    public int onBlockPlaced (World par1World, int blockX, int blockY, int blockZ, int side, float clickX, float clickY, float clickZ, int metadata)
    {
        if (side == 1)
            return metadata;
        if (side == 0 || clickY >= 0.5F)
            return metadata | 8;

        return metadata;
    }

    @Override
    public boolean isOpaqueCube ()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock ()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons (IIconRegister iconRegister)
    {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon (int side, int meta)
    {
        meta = meta % 8 + startingMeta;
        return modelBlock.getIcon(side, meta);
    }

    @Override
    public void getSubBlocks (Item b, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < totalSize; iter++)
        {
            list.add(new ItemStack(b, 1, iter));
        }
    }

    @Override
    public int damageDropped (int meta)
    {
        return meta % 8;
    }
}
