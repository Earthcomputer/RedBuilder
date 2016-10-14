package net.earthcomputer.redbuilder.network.handler;

import net.earthcomputer.redbuilder.IDelayedReturnSite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class ServerSideNonRedBuilderHandler implements IUniformInstructionHandler {

	private EntityPlayerMP player;

	public ServerSideNonRedBuilderHandler(EntityPlayerMP player) {
		this.player = player;
	}

	private void unsupported() throws UnsupportedInstructionException {
		throw new UnsupportedInstructionException("This should never happen for non-RedBuilder clients");
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
	public void getTileEntityData(BlockPos pos, IDelayedReturnSite<NBTTagCompound> returnSite)
			throws UnsupportedInstructionException {
		unsupported();
	}

	@Override
	public void displayMessage(ITextComponent message) {
		player.addChatMessage(message);
	}

}
