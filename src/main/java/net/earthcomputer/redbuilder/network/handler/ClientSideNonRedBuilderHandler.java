package net.earthcomputer.redbuilder.network.handler;

import net.earthcomputer.redbuilder.util.IDelayedReturnSite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientSideNonRedBuilderHandler implements IUniformInstructionHandler {

	private void unsupported() throws UnsupportedInstructionException {
		throw new UnsupportedInstructionException("The server will not allow you to perform this operation");
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
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
	}

}
