package tconstruct.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tconstruct.items.armor.HeartCanister;

public class SlotCanister extends Slot
{
    public SlotCanister(IInventory par2IInventory, int par3, int par4, int par5)
    {
        super(par2IInventory, par3, par4, par5);
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as
     * getInventoryStackLimit(), but 1 in the case of armor slots)
     */
    @Override
    public int getSlotStackLimit ()
    {
        return 10;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for
     * the armor slots.
     */
    @Override
    public boolean isItemValid (ItemStack par1ItemStack)
    {
        Item item = (par1ItemStack == null ? null : par1ItemStack.getItem());
        return item != null && (item instanceof HeartCanister);
    }
}