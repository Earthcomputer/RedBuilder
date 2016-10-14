package net.earthcomputer.redbuilder.network.handler;

import java.util.Map;

import com.google.common.collect.Maps;

import net.earthcomputer.redbuilder.IDelayedReturnSite;
import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.network.packet.CPacketRequestTileEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientSideRedBuilderHandler implements IUniformInstructionHandler {

	private final Map<BlockPos, IDelayedReturnSite<NBTTagCompound>> tileEntityDataRequests = Maps.newHashMap();

	public void onTileEntityDataReceived(BlockPos pos, NBTTagCompound tag) {
		tileEntityDataRequests.remove(pos).returnValue(tag);
	}

	private void unsupported() throws UnsupportedInstructionException {
		throw new UnsupportedInstructionException("This job should be done on the server");
	}

	@Override
	public void setBlock(BlockPos pos, IBlockState state) throws UnsupportedInstructionException {
		unsupported();
	}
	
	@Override
	public boolean canSetTileEntityData() {
		return false;
	}

	@Override
	public void setTileEntityData(BlockPos pos, NBTTagCompound tileEntityData) throws UnsupportedInstructionException {
		unsupported();
	}

	@Override
	public void getTileEntityData(BlockPos pos, IDelayedReturnSite<NBTTagCompound> returnSite) {
		tileEntityDataRequests.put(pos, returnSite);
		RedBuilder.instance().getNetwork().sendToServer(new CPacketRequestTileEntityData(pos));
	}

	@Override
	public void displayMessage(ITextComponent message) {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
	}

}
