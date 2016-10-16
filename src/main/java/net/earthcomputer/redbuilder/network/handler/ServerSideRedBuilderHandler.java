package net.earthcomputer.redbuilder.network.handler;

import net.earthcomputer.redbuilder.util.IDelayedReturnSite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ServerSideRedBuilderHandler implements IUniformInstructionHandler {

	private EntityPlayerMP player;

	public ServerSideRedBuilderHandler(EntityPlayerMP player) {
		this.player = player;
	}

	@Override
	public void setBlock(BlockPos pos, IBlockState state) {
		World world = player.worldObj;
		if (world.setBlockState(pos, state, 2)) {
			world.notifyNeighborsRespectDebug(pos, state.getBlock());
		}
	}

	@Override
	public boolean canSetTileEntityData() {
		return true;
	}

	@Override
	public void setTileEntityData(BlockPos pos, NBTTagCompound tileEntityData) {
		TileEntity tileEntity = player.worldObj.getTileEntity(pos);
		if (tileEntity != null) {
			tileEntity.readFromNBT(tileEntityData);
		}
	}

	@Override
	public void getTileEntityData(BlockPos pos, IDelayedReturnSite<NBTTagCompound> returnSite) {
		TileEntity tileEntity = player.worldObj.getTileEntity(pos);
		if (tileEntity == null) {
			returnSite.returnValue(null);
		} else {
			returnSite.returnValue(tileEntity.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void displayMessage(ITextComponent message) {
		player.addChatMessage(message);
	}

}
