package tconstruct.blocks;

import java.util.List;

import mantle.blocks.abstracts.InventorySlab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import tconstruct.TConstruct;
import tconstruct.blocks.logic.CraftingStationLogic;
import tconstruct.blocks.logic.PartBuilderLogic;
import tconstruct.blocks.logic.PatternChestLogic;
import tconstruct.blocks.logic.StencilTableLogic;
import tconstruct.blocks.logic.ToolForgeLogic;
import tconstruct.blocks.logic.ToolStationLogic;
import tconstruct.common.TProxyCommon;
import tconstruct.common.TRepo;
import tconstruct.library.TConstructRegistry;
import tconstruct.util.config.PHConstruct;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CraftingSlab extends InventorySlab
{
    public CraftingSlab(Material material)
    {
        super(material);
        this.setCreativeTab(TConstructRegistry.blockTab);
        this.setHardness(2f);
        this.stepSound = Block.soundTypeWood;
    }

    /* Rendering */
    @Override
    public String[] getTextureNames ()
    {
        String[] textureNames = { "craftingstation_top", "craftingstation_slab_side", "craftingstation_bottom", "toolstation_top", "toolstation_slab_side", "toolstation_bottom",
                "partbuilder_oak_top", "partbuilder_slab_side", "partbuilder_oak_bottom", "stenciltable_oak_top", "stenciltable_slab_side", "stenciltable_oak_bottom", "patternchest_top",
                "patternchest_slab_side", "patternchest_bottom", "toolforge_top", "toolforge_slab_side", "toolforge_top" };

        return textureNames;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon (int side, int meta)
    {
        return icons[(meta % 8) * 3 + getTextureIndex(side)];
    }

    public int getTextureIndex (int side)
    {
        if (side == 0)
            return 2;
        if (side == 1)
            return 0;

        return 1;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool (World world, int x, int y, int z)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        if (metadata == 5)
            return AxisAlignedBB.getAABBPool().getAABB((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY - 0.125,
                    (double) z + this.maxZ);
        return AxisAlignedBB.getAABBPool().getAABB((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY, (double) z + this.maxZ);
    }

    @Override
    public TileEntity createTileEntity (World world, int metadata)
    {
        switch (metadata % 8)
        {
        case 0:
            return new CraftingStationLogic();
        case 1:
            return new ToolStationLogic();
        case 2:
            return new PartBuilderLogic();
        case 3:
            return new StencilTableLogic();
        case 4:
            return new PatternChestLogic();
        case 5:
            return new ToolForgeLogic();
        default:
            return null;
        }
    }

    @Override
    public Integer getGui (World world, int x, int y, int z, EntityPlayer entityplayer)
    {
        int meta = world.getBlockMetadata(x, y, z) % 8;
        switch (meta)
        {
        case 0:
            return TProxyCommon.craftingStationID;
        case 1:
            return TProxyCommon.toolStationID;
        case 2:
            return TProxyCommon.partBuilderID;
        case 3:
            return TProxyCommon.stencilTableID;
        case 4:
            return TProxyCommon.patternChestID;
        case 5:
            return TProxyCommon.toolForgeID;
        }

        return -1;
    }

    @Override
    public Object getModInstance ()
    {
        return TConstruct.instance;
    }

    @Override
    public void getSubBlocks (Item b, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < 6; iter++)
        {
            list.add(new ItemStack(b, 1, iter));
        }
    }

    @Override
    public void onBlockPlacedBy (World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack stack)
    {
        if (PHConstruct.freePatterns)
        {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == 4)
            {
                PatternChestLogic logic = (PatternChestLogic) world.getTileEntity(x, y, z);
                for (int i = 1; i <= 13; i++)
                {
                    logic.setInventorySlotContents(i - 1, new ItemStack(TRepo.woodPattern, 1, i));
                }
                logic.setInventorySlotContents(13, new ItemStack(TRepo.woodPattern, 1, 22));
            }
        }
        super.onBlockPlacedBy(world, x, y, z, entityliving, stack);
    }

    @Override
    public TileEntity createNewTileEntity (World var1, int metadata)
    {
        switch (metadata % 8)
        {
        case 0:
            return new CraftingStationLogic();
        case 1:
            return new ToolStationLogic();
        case 2:
            return new PartBuilderLogic();
        case 3:
            return new StencilTableLogic();
        case 4:
            return new PatternChestLogic();
        case 5:
            return new ToolForgeLogic();
        default:
            return null;
        }
    }
}
