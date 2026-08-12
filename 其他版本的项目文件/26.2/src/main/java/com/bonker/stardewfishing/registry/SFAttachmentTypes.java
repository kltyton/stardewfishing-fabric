package com.bonker.stardewfishing.registry;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.fishing.FishingHookAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class SFAttachmentTypes {
    public static final AttachmentType<FishingHookAttachment> HOOK =
            AttachmentRegistry.createDefaulted(StardewFishing.identifier("hook"), FishingHookAttachment::new);

    /** Triggers class initialization so all attachment types register. */
    public static void register() {
    }
}
