package net.earthcomputer.redbuilder.wrench;

import net.earthcomputer.redbuilder.IDelayedReturnSite;
import net.earthcomputer.redbuilder.IRedBuilderFeature;
import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.network.ClientInstructionWrapper;
import net.earthcomputer.redbuilder.network.CommonChatUtils;
import net.earthcomputer.redbuilder.network.handler.Handlers;
import net.earthcomputer.redbuilder.network.handler.IUniformInstructionHandler;
import net.earthcomputer.redbuilder.network.handler.UnsupportedInstructionException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WrenchEventListener implements IRedBuilderFeature {

	@Override
	public void initialize() {
		MinecraftForge.EVENT_BUS.register(this);
		GameRegistry.register(ItemClickBlockHack.instance());

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			ModelResourceLocation hackLocation = new ModelResourceLocation(RedBuilder.MODID + ":hack_item",
					"inventory");
			ModelLoader.setCustomModelResourceLocation(ItemClickBlockHack.instance(), 0, hackLocation);

			ItemStack wrench = new ItemStack(Items.STICK);
			setTurnDir(wrench, EnumTurnDirection.Y_AXIS);
			RedBuilder.instance().getCreativeTab().addItem(wrench);
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickedEntity(EntityInteract e) {
		if (e.getWorld().isRemote) {
			if (getWrenchTurnDir(e) != null) {
				e.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityRightClicked(EntityInteractSpecific e) {
		if (e.getWorld().isRemote) {
			if (getWrenchTurnDir(e) != null) {
				e.setCanceled(true);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRightClickAir(RightClickItem e) {
		if (!e.getWorld().isRemote) {
			return;
		}

		if (Minecraft.getMinecraft().objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) {
			return;
		}

		EnumTurnDirection turnDir = getWrenchTurnDir(e);
		if (turnDir == null) {
			return;
		}

		ItemStack stack = e.getItemStack();
		setTurnDir(stack, turnDir.nextTurnDir());
		Minecraft.getMinecraft().playerController.sendSlotPacket(stack,
				36 + Minecraft.getMinecraft().thePlayer.inventory.currentItem);
	}

	@SubscribeEvent
	public void onLeftClicked(LeftClickBlock e) {
		if (!Handlers.isBestExecutionSide(e.getSide())) {
			return;
		}

		if (onWrenchUsed(e, true)) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRightClicked(final RightClickBlock e) {
		if (getWrenchTurnDir(e) != null) {
			ItemClickBlockHack.setupItemStackForHack(e.getItemStack());
			if (!Handlers.isBestExecutionSide(e.getSide())) {
				if (e.getSide() == Side.CLIENT) {
					BlockPos pos = e.getPos();
					Vec3d vec = ClientInstructionWrapper.getObjMouseOver().hitVec;
					ClientInstructionWrapper.sendVanillaPacketToServer(new CPacketPlayerTryUseItemOnBlock(pos,
							e.getFace(), e.getHand(), (float) (vec.xCoord - pos.getX()),
							(float) (vec.yCoord - pos.getY()), (float) (vec.zCoord - pos.getZ())));
				}
				return;
			}
			onWrenchUsed(e, false);
		}
	}

	private boolean onWrenchUsed(PlayerInteractEvent e, boolean reverse) {
		EnumTurnDirection turnDir = getWrenchTurnDir(e);
		if (turnDir == null) {
			return false;
		}

		EntityPlayer player = e.getEntityPlayer();

		EnumFacing horizontalPlayerFacing = player.getHorizontalFacing();
		World world = e.getWorld();
		final BlockPos pos = e.getPos();
		IBlockState state = world.getBlockState(pos);
		final IBlockState turnedState = WrenchTurnRegistry.turn(state, turnDir.getAxis(horizontalPlayerFacing),
				turnDir.getAxisDirection(horizontalPlayerFacing, reverse));
		// With reverse the server thinks the player has destroyed the block, we
		// need to tell the server the block hasn't changed
		if (e.getSide() == Side.CLIENT && turnedState.equals(state) && !reverse) {
			return true;
		}

		final IUniformInstructionHandler handler = Handlers.getInstructionHandler(e.getSide(), player);
		if (world.getTileEntity(pos) != null) {
			if (!handler.canSetTileEntityData()) {
				handler.displayMessage(CommonChatUtils.buildErrorMessage("redbuilder.wrench.tileentity"));
				return false;
			}
			try {
				handler.getTileEntityData(pos, new IDelayedReturnSite<NBTTagCompound>() {
					@Override
					public void returnValue(NBTTagCompound tag) {
						try {
							handler.setBlock(pos, turnedState);
							handler.setTileEntityData(pos, tag);
						} catch (UnsupportedInstructionException e) {
							RedBuilder.LOGGER.warn(e.getMessage());
						}
					}
				});
			} catch (UnsupportedInstructionException e1) {
				return false;
			}
		} else {
			try {
				handler.setBlock(pos, turnedState);
			} catch (UnsupportedInstructionException e1) {
				return false;
			}
		}

		return true;
	}

	private EnumTurnDirection getWrenchTurnDir(PlayerInteractEvent e) {
		EntityPlayer player = e.getEntityPlayer();
		if (!player.isCreative()) {
			return null;
		}

		ItemStack stack = e.getItemStack();
		if (stack == null) {
			return null;
		}

		NBTTagCompound stackTag = stack.getTagCompound();
		if (stackTag == null || !stackTag.hasKey("WrenchTurnDirection")) {
			return null;
		}

		EnumTurnDirection turnDir = EnumTurnDirection.getByName(stackTag.getString("WrenchTurnDirection"));
		return turnDir;
	}

	static void setTurnDir(ItemStack stack, EnumTurnDirection turnDir) {
		stack.setTagInfo("WrenchTurnDirection", new NBTTagString(turnDir.getName()));
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", TextFormatting.RESET.toString() + TextFormatting.AQUA
				+ I18n.format("redbuilder.wrench." + turnDir.getName()));
		stack.setTagInfo("display", display);
		stack.setTagInfo("ench", new NBTTagList());
	}

}
