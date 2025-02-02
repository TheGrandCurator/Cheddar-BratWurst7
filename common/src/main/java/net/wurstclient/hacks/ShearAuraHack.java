/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SearchTags({"shear aura", "ShearAura", "shear aura", "AutoShearer",
	"auto shearer"})
public final class ShearAuraHack extends Hack
	implements UpdateListener, PostMotionListener, RenderListener
{
	private final SliderSetting range = new SliderSetting("Range",
		"Determines how far ShearAura will reach\n" + "to shear sheep.\n"
			+ "Anything that is further away than the\n"
			+ "specified value will not be fed.",
		5, 1, 10, 0.05, ValueDisplay.DECIMAL);

	private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
		"Determines which sheep will be sheared first.\n"
			+ "§lDistance§r - Shears the closest sheep.\n"
			+ "§lAngle§r - Shears the sheep that requires\n"
			+ "the least head movement.\n"
			+ "§lHealth§r - Shears the weakest sheep.",
		Priority.values(), Priority.ANGLE);

	private AnimalEntity target;
	private AnimalEntity renderTarget;

	public ShearAuraHack()
	{
		super("ShearAura");
		setCategory(Category.OTHER);
		addSetting(range);
		addSetting(priority);
	}
	
	@Override
	protected void onEnable()
	{
		// disable other auras
		WURST.getHackRegistry().clickAuraHack.setEnabled(false);
		WURST.getHackRegistry().fightBotHack.setEnabled(false);
		WURST.getHackRegistry().feedAuraHack.setEnabled(false);
		WURST.getHackRegistry().killauraLegitHack.setEnabled(false);
		WURST.getHackRegistry().multiAuraHack.setEnabled(false);
		WURST.getHackRegistry().protectHack.setEnabled(false);
		WURST.getHackRegistry().triggerBotHack.setEnabled(false);
		WURST.getHackRegistry().tpAuraHack.setEnabled(false);

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PostMotionListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		target = null;
		renderTarget = null;
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		ItemStack heldStack = player.getInventory().getMainHandStack();
		
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<AnimalEntity> stream = StreamSupport
			.stream(MC.world.getEntities().spliterator(), true)
			.filter(e -> !e.isRemoved()).filter(e -> e instanceof SheepEntity)
			.map(e -> (AnimalEntity)e).filter(e -> e.getHealth() > 0)
			.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
			.filter(e -> heldStack.getItem() instanceof ShearsItem)
			.filter(e -> ((SheepEntity) e).isShearable());
		
		target = stream.min(priority.getSelected().comparator).orElse(null);
		renderTarget = target;
		if(target == null)
			return;
		
		WURST.getRotationFaker()
			.faceVectorPacket(target.getBoundingBox().getCenter());
	}
	
	@Override
	public void onPostMotion()
	{
		if(target == null)
			return;
		
		ClientPlayerInteractionManager im = MC.interactionManager;
		ClientPlayerEntity player = MC.player;
		Hand hand = Hand.MAIN_HAND;
		
		EntityHitResult hitResult = new EntityHitResult(target);
		ActionResult actionResult =
			im.interactEntityAtLocation(player, target, hitResult, hand);
		
		if(!actionResult.isAccepted())
			actionResult = im.interactEntity(player, target, hand);
		
		if(actionResult.isAccepted() && actionResult.shouldSwingHand())
			player.swingHand(hand);
		
		target = null;
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(renderTarget == null)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		RenderUtils.applyRenderOffset(matrixStack);
		
		Box box = new Box(BlockPos.ORIGIN);
		float p = 1;
		LivingEntity le = renderTarget;
		p = (le.getMaxHealth() - le.getHealth()) / le.getMaxHealth();
		float red = p * 2F;
		float green = 2 - red;
		
		matrixStack.translate(
			renderTarget.prevX
				+ (renderTarget.getX() - renderTarget.prevX) * partialTicks,
			renderTarget.prevY
				+ (renderTarget.getY() - renderTarget.prevY) * partialTicks,
			renderTarget.prevZ
				+ (renderTarget.getZ() - renderTarget.prevZ) * partialTicks);
		matrixStack.translate(0, 0.05, 0);
		matrixStack.scale(renderTarget.getWidth(), renderTarget.getHeight(),
			renderTarget.getWidth());
		matrixStack.translate(-0.5, 0, -0.5);
		
		if(p < 1)
		{
			matrixStack.translate(0.5, 0.5, 0.5);
			matrixStack.scale(p, p, p);
			matrixStack.translate(-0.5, -0.5, -0.5);
		}
		
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		RenderSystem.setShaderColor(red, green, 0, 0.25F);
		RenderUtils.drawSolidBox(box, matrixStack);
		
		RenderSystem.setShaderColor(red, green, 0, 0.5F);
		RenderUtils.drawOutlinedBox(box, matrixStack);
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private enum Priority
	{
		DISTANCE("Distance", e -> MC.player.squaredDistanceTo(e)),
		
		ANGLE("Angle",
			e -> RotationUtils
				.getAngleToLookVec(e.getBoundingBox().getCenter())),
		
		HEALTH("Health", e -> e instanceof LivingEntity
			? ((LivingEntity)e).getHealth() : Integer.MAX_VALUE);
		
		private final String name;
		private final Comparator<Entity> comparator;
		
		private Priority(String name, ToDoubleFunction<Entity> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
