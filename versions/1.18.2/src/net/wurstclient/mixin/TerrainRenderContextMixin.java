/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.TesselateBlockListener.TesselateBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TerrainRenderContext.class)
public class TerrainRenderContextMixin
{
	@Inject(at = {@At("HEAD")},
			method = {"tessellateBlock"},
			cancellable = true,
			remap = false)
	private void tessellateBlock(BlockState blockState, BlockPos blockPos,
								final BakedModel model, MatrixStack matrixStack,
								CallbackInfoReturnable<Boolean> cir)
	{
		TesselateBlockEvent event = new TesselateBlockEvent(blockState, blockPos);
		EventManager.fire(event);

		if(event.isCancelled())
			cir.cancel();
	}
}
