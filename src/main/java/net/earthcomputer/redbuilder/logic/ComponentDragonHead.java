package net.earthcomputer.redbuilder.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentDragonHead implements IRedstoneComponent {

	private static final int SKULL_TYPE_DRAGON = 5;

	@Override
	public PowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		PowerInfo powerInfo = new PowerInfo();

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntitySkull) {
			if (((TileEntitySkull) tileEntity).getSkullType() == SKULL_TYPE_DRAGON) {
				powerInfo.canBePowered();
			}
		}

		return powerInfo;
	}

}
