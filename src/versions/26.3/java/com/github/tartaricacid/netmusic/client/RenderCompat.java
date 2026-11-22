package com.github.tartaricacid.netmusic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Model submission overload for this game version. */
public final class RenderCompat {
    private RenderCompat() {}
    public static <S> void submitModel(SubmitNodeCollector collector, Model<S> model, S state,
            PoseStack pose, Identifier texture, int light, int overlay, int outline,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        collector.submitModel(model, state, pose, texture, light, overlay, outline);
    }
}
