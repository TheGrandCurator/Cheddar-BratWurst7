/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hud;

import net.minecraft.client.font.TextRenderer;
import net.wurstclient.hacks.WaypointsHack;
import net.wurstclient.waypoints.Waypoint;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.util.math.MatrixStack;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.screens.ClickGuiScreen;
import net.wurstclient.events.GUIRenderListener;

import java.text.DecimalFormat;

public final class IngameHUD implements GUIRenderListener
{
	private final WurstLogo wurstLogo = new WurstLogo();
	private final HackListHUD hackList = new HackListHUD();
	private final InfoHUD infoHUD = new InfoHUD();
	private TabGui tabGui;
	
	@Override
	public void onRenderGUI(MatrixStack matrixStack, float partialTicks)
	{
		if(!WurstClient.INSTANCE.isEnabled())
			return;
		
		if(tabGui == null)
			tabGui = new TabGui();
		
		boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);
		ClickGui clickGui = WurstClient.INSTANCE.getGui();
		
		// GL settings
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		clickGui.updateColors();
		
		wurstLogo.render(matrixStack);
		hackList.render(matrixStack, partialTicks);
		tabGui.render(matrixStack, partialTicks);

		if(WurstClient.INSTANCE.getHax().hudHack.isEnabled())
			infoHUD.render(matrixStack);

		// pinned windows
		if(!(WurstClient.MC.currentScreen instanceof ClickGuiScreen))
			clickGui.renderPinnedWindows(matrixStack, partialTicks);

		//waypoint
		drawWaypoint(matrixStack);
		
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		if(blend)
			GL11.glEnable(GL11.GL_BLEND);
		else
			GL11.glDisable(GL11.GL_BLEND);
	}

	public void drawWaypoint(MatrixStack matrixStack) {
		WaypointsHack waypoints = WurstClient.INSTANCE.getHax().waypointsHack;
		if(waypoints.isEnabled() && waypoints.selectedWaypoint != null) {
			int screenWidth = WurstClient.MC.getWindow().getScaledWidth();
			TextRenderer tr = WurstClient.MC.textRenderer;
			String str = "Active Waypoint: " + waypoints.selectedWaypoint.getName();
			String dist = "Distance: " + new DecimalFormat("##,###,###").format(waypoints.selectedWaypoint.getPos().
					distanceTo(WurstClient.MC.player.getPos()));
			tr.drawWithShadow(matrixStack, str, (screenWidth/2)-(tr.getWidth(str)/2), 2, 0xFFFFFF);
			tr.drawWithShadow(matrixStack, dist, (screenWidth/2)-(tr.getWidth(dist)/2), 12, 0xFFFFFF);
		}
	}
	
	public HackListHUD getHackList()
	{
		return hackList;
	}
}
