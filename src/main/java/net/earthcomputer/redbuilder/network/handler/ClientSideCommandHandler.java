package net.earthcomputer.redbuilder.network.handler;

import java.util.List;

import net.earthcomputer.redbuilder.util.ClientChatUtils;
import net.earthcomputer.redbuilder.util.CommonChatUtils;
import net.earthcomputer.redbuilder.util.IDelayedReturnSite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientSideCommandHandler implements IUniformInstructionHandler {

	@Override
	public void setBlock(BlockPos pos, IBlockState state) {
		ClientChatUtils.setBlock(pos, state);
	}
	
	@Override
	public boolean canSetTileEntityData() {
		return false;
	}

	@Override
	public void setTileEntityData(BlockPos pos, NBTTagCompound tileEntityData) throws UnsupportedInstructionException {
		throw new UnsupportedInstructionException("This operation is impossible through the use of chat commands only");
	}

	@Override
	public void getTileEntityData(BlockPos pos, final IDelayedReturnSite<NBTTagCompound> returnSite) {
		ClientChatUtils.getTileEntityData(pos, new IDelayedReturnSite<List<NBTTagCompound>>() {
			@Override
			public void returnValue(List<NBTTagCompound> value) {
				if (value.isEmpty()) {
					displayMessage(CommonChatUtils.buildErrorMessage("redbuilder.getTileEntityData.noPossibilities"));
				} else if (value.size() != 1) {
					displayMessage(
							CommonChatUtils.buildErrorMessage("redbuilder.getTileEntityData.tooManyPossibilities"));
				} else {
					returnSite.returnValue(value.get(0));
				}
			}
		});
	}

	@Override
	public void displayMessage(ITextComponent message) {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
	}

}
