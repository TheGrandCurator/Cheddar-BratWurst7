/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.GUIRenderListener.GUIRenderEvent;

@Mixin(InGameHud.class)
public class IngameHudMixin extends DrawableHelper
{
	@Inject(
			at = {@At(value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V",
					ordinal = 4)},
			method = {"render(Lnet/minecraft/client/util/math/MatrixStack;F)V"})
	private void onRender(MatrixStack matrixStack, float partialTicks,
						  CallbackInfo ci)
	{
		if(WurstClient.MC.options.debugEnabled)
			return;

		GUIRenderEvent event = new GUIRenderEvent(matrixStack, partialTicks);
		EventManager.fire(event);
	}

	@Inject(at = {@At("HEAD")},
			method = {"renderOverlay(Lnet/minecraft/util/Identifier;F)V"},
			cancellable = true)
	private void onRenderOverlay(Identifier identifier, float scale,
								 CallbackInfo ci)
	{
		if(identifier == null || identifier.getPath() == null)
			return;

		if(!identifier.getPath().equals("textures/misc/pumpkinblur.png"))
			return;

		if(!WurstClient.INSTANCE.getHackRegistry().noPumpkinHack.isEnabled())
			return;

		ci.cancel();
	}
}
