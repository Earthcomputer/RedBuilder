package net.earthcomputer.redbuilder.logic;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComponentComparator implements IRedstoneComponent {

	@Override
	public RedstonePowerInfo getPowerInfo(World world, BlockPos pos, IBlockState state) {
		RedstonePowerInfo powerInfo = new RedstonePowerInfo();
		EnumFacing comparatorFacing = state.getValue(BlockRedstoneComparator.FACING);
		
		if (!hasInputOverride(world, pos, comparatorFacing)) {
			powerInfo.canBePoweredBy(comparatorFacing.getOpposite());
		}
		powerInfo.canBePoweredByStrongly(comparatorFacing.rotateY());
		powerInfo.canBePoweredByStrongly(comparatorFacing.rotateYCCW());

		int power = 0;
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityComparator) {
			power = ((TileEntityComparator) tileEntity).getOutputSignal();
		}
		if (power > 0) {
			powerInfo.powerStrong(comparatorFacing, power);
		}
		return powerInfo;
	}

	private boolean hasInputOverride(World world, BlockPos pos, final EnumFacing comparatorFacing) {
		EnumFacing backwardsFacing = comparatorFacing.getOpposite();
		BlockPos testingPos = pos.offset(backwardsFacing);
		IBlockState testingBlock = world.getBlockState(testingPos);
		if (testingBlock.hasComparatorInputOverride()) {
			return true;
		}

		if (testingBlock.isNormalCube()) {
			if (world.getRedstonePower(pos, comparatorFacing) >= 15) {
				return false;
			}
			
			testingPos = testingPos.offset(backwardsFacing);
			testingBlock = world.getBlockState(testingPos);
			if (testingBlock.hasComparatorInputOverride()) {
				return true;
			}
			
			List<EntityItemFrame> itemFrameList = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(testingPos), new Predicate<EntityItemFrame>() {
				@Override
				public boolean apply(EntityItemFrame itemFrame) {
					return itemFrame != null && itemFrame.getHorizontalFacing() == comparatorFacing;
				}
			});
			if (itemFrameList.size() == 1) {
				return true;
			}
		}
		return false;
	}

}
