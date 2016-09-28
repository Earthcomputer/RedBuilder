package net.earthcomputer.redbuilder.logic;

import net.earthcomputer.redbuilder.RedBuilderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RedstoneLogicDisplayListener {

	@SubscribeEvent
	public void onRenderBlockOverlay(DrawBlockHighlightEvent e) {
		if (!RedBuilderSettings.enableRedstonePowerInfo) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.theWorld;
		EntityPlayer player = mc.thePlayer;
		RayTraceResult target = mc.objectMouseOver;
		if (world == null || player == null || target == null) {
			return;
		}

		if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = target.getBlockPos();
		RedstonePowerInfo powerInfo = RedstoneComponentRegistry.getPowerInfo(world, pos);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5 - player.posX, -player.posY, 0.5 - player.posZ);

		GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(8);
		for (PowerPath path : powerInfo.genPowerPaths(world, pos, world.getBlockState(pos))) {
			path.draw();
		}
		GlStateManager.enableBlend();
		GlStateManager.enableDepth();

		GlStateManager.popMatrix();
	}

}
