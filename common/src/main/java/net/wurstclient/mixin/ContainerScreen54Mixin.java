/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoStealHack;

@Mixin(GenericContainerScreen.class)
public abstract class ContainerScreen54Mixin
        extends HandledScreen<GenericContainerScreenHandler>
        implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    @Shadow
    @Final
    private int rows;

    private final AutoStealHack autoSteal =
            WurstClient.INSTANCE.getHackRegistry().autoStealHack;
    private int mode;

    public ContainerScreen54Mixin(WurstClient wurst,
                                  GenericContainerScreenHandler container,
                                  PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    @Override
    protected void init() {
        super.init();

        if (!WurstClient.INSTANCE.isEnabled())
            return;

        if (autoSteal.areButtonsVisible()) {
            addDrawableChild(new ButtonWidget(x + backgroundWidth - 108, y + 4,
                    50, 12, new LiteralText("Steal"), b -> steal()));

            addDrawableChild(new ButtonWidget(x + backgroundWidth - 56, y + 4,
                    50, 12, new LiteralText("Store"), b -> store()));
        }

        if (autoSteal.isEnabled())
            steal();
    }

    private void steal() {
        runInThread(() -> shiftClickSlots(0, rows * 9, 1));
    }

    private void store() {
        runInThread(() -> shiftClickSlots(rows * 9, rows * 9 + 44, 2));
    }

    private void runInThread(Runnable r) {
        new Thread(() -> {
            try {
                r.run();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void shiftClickSlots(int from, int to, int mode) {
        this.mode = mode;

        for (int i = from; i < to; i++) {
            Slot slot = handler.slots.get(i);
            if (slot.getStack().isEmpty())
                continue;
            boolean can = false;
            for (int e = (mode == 1 ? rows * 9 : 0); e < (mode == 1 ? rows * 9 + 36 : rows * 9); e++) {
                if (handler.canInsertItemIntoSlot(handler.slots.get(e), slot.getStack(), true)) {
                    can = true;
                    break;
                }
            }

            if (!can) {
                continue;
            }
            waitForDelay();
            if (this.mode != mode || client.currentScreen == null)
                break;

            onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
        }
    }

    private void waitForDelay() {
        try {
            Thread.sleep(autoSteal.getDelay());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
