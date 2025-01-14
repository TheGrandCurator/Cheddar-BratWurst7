/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin extends Block implements FluidDrainable {
    private FluidBlockMixin(WurstClient wurst, Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState blockState_1,
                                        BlockView blockView_1, BlockPos blockPos_1,
                                        ShapeContext entityContext_1) {
        HackRegistry hax = WurstClient.INSTANCE.getHackRegistry();
        if (hax != null && hax.jesusHack.shouldBeSolid())
            return VoxelShapes.fullCube();

        return super.getCollisionShape(blockState_1, blockView_1, blockPos_1,
                entityContext_1);
    }
}
