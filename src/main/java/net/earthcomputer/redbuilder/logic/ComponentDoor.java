package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentDoor implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		powerInfo.canBePowered();
		if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
			powerInfo.canBePoweredBy(new BlockPos(0, 1, 0));
		} else {
			powerInfo.canBePoweredBy(new BlockPos(0, -1, 0));
		}
		return powerInfo;
	}

}
