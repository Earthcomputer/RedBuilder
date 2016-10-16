package net.earthcomputer.redbuilder.network.handler;

import net.earthcomputer.redbuilder.util.IDelayedReturnSite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public interface IUniformInstructionHandler {

	void setBlock(BlockPos pos, IBlockState state) throws UnsupportedInstructionException;

	boolean canSetTileEntityData();
	
	void setTileEntityData(BlockPos pos, NBTTagCompound tileEntityData) throws UnsupportedInstructionException;

	void getTileEntityData(BlockPos pos, IDelayedReturnSite<NBTTagCompound> returnSite)
			throws UnsupportedInstructionException;

	void displayMessage(ITextComponent message);

}
