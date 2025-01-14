package hopperOptimizations.mixins;


import hopperOptimizations.utils.EntityHopperInteraction;
import hopperOptimizations.utils.InventoryListOptimized;
import hopperOptimizations.utils.InventoryOptimizer;
import hopperOptimizations.utils.OptimizedInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;


@Mixin(StorageMinecartEntity.class)
public abstract class StorageMinecartEntityMixin extends AbstractMinecartEntity implements OptimizedInventory {

    private boolean initialized;
    //Redirects and Injects to replace the inventory with an optimized Inventory
    @Shadow
    private DefaultedList<ItemStack> inventory;
    private int viewerCount;

    protected StorageMinecartEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
        //todo check what has to be here!!
        throw new AssertionError();
    }

    @Redirect(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/DefaultedList;"))
    private DefaultedList<ItemStack> createInventory(int int_1, Object object_1) {
        DefaultedList<ItemStack> ret = InventoryListOptimized.ofSize(int_1, (ItemStack) object_1);
        ((InventoryListOptimized) ret).setSize(this.getInvSize());
        return ret;
    }

    @Redirect(method = "<init>(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/DefaultedList;"))
    private DefaultedList<ItemStack> createInventory1(int int_1, Object object_1) {
        DefaultedList<ItemStack> ret = InventoryListOptimized.ofSize(int_1, (ItemStack) object_1);
        ((InventoryListOptimized) ret).setSize(this.getInvSize());
        return ret;
    }

    @Redirect(method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/DefaultedList;"))
    private DefaultedList<ItemStack> createInventory2(int int_1, Object object_1) {
        DefaultedList<ItemStack> ret = InventoryListOptimized.ofSize(int_1, (ItemStack) object_1);
        ((InventoryListOptimized) ret).setSize(this.getInvSize());
        return ret;
    }

    @Nullable
    public InventoryOptimizer getOptimizer() {
        return mayHaveOptimizer() && inventory instanceof InventoryListOptimized ? ((InventoryListOptimized) inventory).getCreateOrRemoveOptimizer() : null;
    }

    @Override
    public void invalidateOptimizer() {
        if (inventory instanceof InventoryListOptimized) ((InventoryListOptimized) inventory).invalidateOptimizer();
    }

    public void onInvOpen(PlayerEntity playerEntity_1) {
        if (!playerEntity_1.isSpectator())
            invalidateOptimizer();
        viewerCount++;
    }

    public void onInvClose(PlayerEntity playerEntity_1) {
        viewerCount--;
        if (viewerCount < 0) {
            System.out.println("StorageMinecartEntityMixin: (Inventory-)viewerCount inconsistency detected, might affect performance of optimizedInventories!");
            viewerCount = 0;
        }
    }

    @Override
    public boolean mayHaveOptimizer() {
        return viewerCount <= 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isClient && (this.prevX != this.getX() || this.prevY != this.getY() || this.prevZ != this.getZ() || !initialized)) {
            EntityHopperInteraction.findAndNotifyHoppers(this);
            initialized = true;
        }
    }
}
