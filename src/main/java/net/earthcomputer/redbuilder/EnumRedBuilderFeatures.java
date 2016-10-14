package net.earthcomputer.redbuilder;

import com.google.common.base.Throwables;

import net.earthcomputer.redbuilder.antiwater.AntiWaterEventListener;
import net.earthcomputer.redbuilder.logic.RedstoneLogicDisplayListener;
import net.earthcomputer.redbuilder.midclick.BetterMiddleClickListener;
import net.earthcomputer.redbuilder.wrench.WrenchEventListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public enum EnumRedBuilderFeatures {

	ANTI_WATER(0, AntiWaterEventListener.class), REDSTONE_COMPONENT_INFO(1, RedstoneLogicDisplayListener.class,
			Side.CLIENT), BETTER_MIDDLE_CLICK(2, BetterMiddleClickListener.class), WRENCH(3, WrenchEventListener.class);

	private int id;
	private IRedBuilderFeature instance;
	private Side side;

	private EnumRedBuilderFeatures(int id, Class<? extends IRedBuilderFeature> featureClass) {
		this(id, featureClass, null);
	}

	private EnumRedBuilderFeatures(int id, Class<? extends IRedBuilderFeature> featureClass, Side side) {
		this.id = id;
		this.side = side;
		if (shouldRunInEnvironment()) {
			try {
				this.instance = featureClass.newInstance();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}
	}

	public int getId() {
		return id;
	}

	public IRedBuilderFeature getFeatureInstance() {
		return instance;
	}

	public boolean shouldRunInEnvironment() {
		return side == null || side == FMLCommonHandler.instance().getSide();
	}

	public static EnumRedBuilderFeatures getById(int id) {
		return id < 0 || id >= BY_ID.length ? null : BY_ID[id];
	}

	private static final EnumRedBuilderFeatures[] BY_ID;

	static {
		EnumRedBuilderFeatures[] values = values();
		BY_ID = new EnumRedBuilderFeatures[values.length];

		for (int i = 0; i < values.length; i++) {
			EnumRedBuilderFeatures feature = values[i];
			BY_ID[feature.getId()] = feature;
		}
	}

}
