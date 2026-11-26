package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.fishing.FishingHookAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class SFAttachmentTypes {
    public static final AttachmentType<FishingHookAttachment> HOOK = AttachmentRegistry.createDefaulted(
            StardewFishing.identifier("hook"), FishingHookAttachment::new);

    private SFAttachmentTypes() {
    }
}
