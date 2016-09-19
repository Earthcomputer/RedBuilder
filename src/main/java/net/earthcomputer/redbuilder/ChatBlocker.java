package net.earthcomputer.redbuilder;

import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatBlocker {

	public static final ChatBlocker INSTANCE = new ChatBlocker();

	private ChatBlocker() {
	}

	private static final Set<Predicate<ITextComponent>> blockedMessages = Sets.newHashSet();

	@SubscribeEvent
	public void chatReceived(ClientChatReceivedEvent e) {
		ITextComponent message = e.getMessage();
		Iterator<Predicate<ITextComponent>> blockedMessageItr = blockedMessages.iterator();
		while (blockedMessageItr.hasNext()) {
			if (blockedMessageItr.next().apply(message)) {
				e.setCanceled(true);
				blockedMessageItr.remove();
			}
		}
	}

	public static void blockNext(Predicate<ITextComponent> predicate) {
		if (Minecraft.getMinecraft().gameSettings.chatVisibility != EnumChatVisibility.HIDDEN) {
			blockedMessages.add(predicate);
		}
	}

	public static void blockNext(ITextComponent message) {
		blockNext(Predicates.equalTo(message));
	}

	public static void blockNext(String message) {
		blockNext(new TextComponentString(message));
	}

	public static void blockNextTranslation(final String translationKey) {
		blockNext(new Predicate<ITextComponent>() {
			@Override
			public boolean apply(ITextComponent message) {
				if (!(message instanceof TextComponentTranslation)) {
					return false;
				}
				return translationKey.equals(((TextComponentTranslation) message).getKey());
			}
		});
	}

	public static void blockNextTranslation(String translationKey, Object... formatArgs) {
		blockNext(new TextComponentTranslation(translationKey, formatArgs));
	}

}
