package hopperOptimizations.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

//Don't store instances of InventoryOptimizer, unless you sync with the corresponding inventory!
//DoubleInventoryOptimizer actually handles that in DoubleInventoryMixin
public class DoubleInventoryOptimizer extends InventoryOptimizer {
    private final OptimizedInventory first;
    private final InventoryOptimizer firstOpt;
    private final OptimizedInventory second;
    private final InventoryOptimizer secondOpt;

    public DoubleInventoryOptimizer(OptimizedInventory first, OptimizedInventory second) {
        super(null);
        this.first = first;
        this.second = second;
        this.firstOpt = first.getOptimizer();
        this.secondOpt = second.getOptimizer();
    }

    public boolean isInvalid() {
        return super.isInvalid() || firstOpt == null || firstOpt.isInvalid() || secondOpt == null || secondOpt.isInvalid();
    }


    @Override
    public void onItemStackCountChanged(int index, int countChange) {
        int firstSize = first.getInvSize();
        if (index >= firstSize) {
            if (secondOpt != null) secondOpt.onItemStackCountChanged(index - firstSize, countChange);
        } else {
            if (firstOpt != null) firstOpt.onItemStackCountChanged(index, countChange);
        }
    }

    public int indexOf_extractable_endIndex(ItemStack stack, int stop) {
        int ret = firstOpt.indexOf_extractable_endIndex(stack, stop);
        if (ret == -1) {
            ret = secondOpt.indexOf_extractable_endIndex(stack, stop);
            if (ret != -1)
                ret += first.getInvSize();
        }
        return ret;
    }

    public boolean hasFreeSlots_insertable() {
        return firstOpt.hasFreeSlots_insertable() || secondOpt.hasFreeSlots_insertable();
    }

    public int findInsertSlot(ItemStack stack, Direction fromDirection) {
        int ret = firstOpt.findInsertSlot(stack, fromDirection);
        if (ret == -1) {
            ret = secondOpt.findInsertSlot(stack, fromDirection);
            if (ret != -1)
                ret += first.getInvSize();
        }
        return ret;
    }

    @Override
    public void recalculate() {
        throw new UnsupportedOperationException("InventoryOptimizer parts have to be calculated individually.");
    }

    @Override
    public int getFirstFreeSlot() {
        int ret = firstOpt.getFirstFreeSlot();
        if (ret == -1) {
            ret = secondOpt.getFirstFreeSlot();
            if (ret != -1)
                ret += first.getInvSize();
        }
        return ret;
    }

    @Override
    public boolean isFull_insertable(Direction fromDirection) {
        return firstOpt.isFull_insertable(fromDirection) && secondOpt.isFull_insertable(fromDirection);
    }

    @Override
    protected ItemStack getSlot(int index) {
        if (index < 0) return ItemStack.EMPTY;
        int firstSize = first.getInvSize();
        if (index < firstSize) return first.getInvStack(index);
        return second.getInvStack(index - firstSize);
    }

    @Override
    protected int size() {
        return first.getInvSize() + second.getInvSize();
    }

    @Override
    public int getOccupiedSlots() {
        return firstOpt.getOccupiedSlots() + secondOpt.getOccupiedSlots();
    }

    public int getFirstOccupiedSlot_extractable() {
        int ret = firstOpt.getFirstOccupiedSlot_extractable();
        if (ret == -1) {
            ret = secondOpt.getFirstOccupiedSlot_extractable();
            if (ret != -1)
                ret += first.getInvSize();
        }
        return ret;
    }

    /*public boolean equals(Object other) {
        return other == this;
        //if(!(other instanceof DoubleInventoryOptimizer)) return false;
        //return this.first == ((DoubleInventoryOptimizer) other).first && this.second == ((DoubleInventoryOptimizer) other).second;
    }*/

    public int getInventoryChangeCount() {
        return firstOpt.getInventoryChangeCount() + secondOpt.getInventoryChangeCount();
    }

}
