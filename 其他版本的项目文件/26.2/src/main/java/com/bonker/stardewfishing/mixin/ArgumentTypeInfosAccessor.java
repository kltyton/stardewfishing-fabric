package com.bonker.stardewfishing.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Vanilla 26.2 removed the public {@code ArgumentTypeInfos.registerByClass}; NeoForge re-adds it.
 * This invokes the private vanilla registration method so custom argument types are both put into
 * the command argument type registry and mapped by class for command tree sync.
 */
@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeInfosAccessor {
    @Invoker("register")
    static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> registerArgumentTypeInfo(
            Registry<ArgumentTypeInfo<?, ?>> registry, String id, Class<? extends A> brigadierType, ArgumentTypeInfo<A, T> info) {
        throw new AssertionError();
    }
}
