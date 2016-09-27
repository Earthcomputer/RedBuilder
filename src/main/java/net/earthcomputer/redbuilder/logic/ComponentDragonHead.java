package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentDragonHead implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntitySkull) {
			if (((TileEntitySkull) tileEntity).getSkullType() == 5) {
				powerInfo.canBePowered();
			}
		}

		return powerInfo;
	}

}
