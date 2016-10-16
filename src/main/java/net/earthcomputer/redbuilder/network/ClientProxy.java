package net.earthcomputer.redbuilder.network;

import net.earthcomputer.redbuilder.RedBuilder;
import net.earthcomputer.redbuilder.RedBuilderCreativeTab;
import net.earthcomputer.redbuilder.util.ClientChatUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(ClientChatUtils.instance());

		RedBuilder.instance().setCreativeTab(new RedBuilderCreativeTab());

		super.preinit(e);
	}

}
