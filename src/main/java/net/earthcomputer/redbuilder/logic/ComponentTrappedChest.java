package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentTrappedChest implements IRedstoneComponent {

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();
		if (!state.canProvidePower()) {
			return powerInfo;
		}

		int power = 0;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityChest) {
			power = ((TileEntityChest) tileEntity).numPlayersUsing;
			if (power > PowerInfo.MAX_POWER) {
				power = PowerInfo.MAX_POWER;
			}
		}

		if (power > 0) {
			powerInfo.powerNeighborsWeakly();
			powerInfo.powerStrong(EnumFacing.DOWN, power);
		}
		
		return powerInfo;
	}

}
