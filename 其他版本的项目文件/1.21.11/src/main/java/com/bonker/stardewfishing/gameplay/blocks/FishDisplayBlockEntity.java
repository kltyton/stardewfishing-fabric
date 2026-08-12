package com.bonker.stardewfishing.gameplay.blocks;

import com.bonker.stardewfishing.registry.SFBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;

/** Holds the single displayed fish for {@link FishDisplayBlock}. */
public class FishDisplayBlockEntity extends BlockEntity implements Clearable {
    private ItemStack item = ItemStack.EMPTY;

    public FishDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(SFBlockEntities.FISH_DISPLAY, pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
        markDirty();
        if (getWorld() != null) {
            getWorld().updateListeners(getPos(), getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        item = view.read("displayed_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putNullable("displayed_item", ItemStack.CODEC, item);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbtWithIdentifyingData(registries);
    }

    @Override
    public void clear() {
        item = ItemStack.EMPTY;
    }
}
