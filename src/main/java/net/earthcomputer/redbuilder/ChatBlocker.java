package net.earthcomputer.redbuilder;

import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ChatBlocker {

	public static final ChatBlocker INSTANCE = new ChatBlocker();

	private ChatBlocker() {
	}

	private static final Set<Function<ITextComponent, ITextComponent>> chatFunctions = Sets.newHashSet();

	@SubscribeEvent
	public void chatReceived(ClientChatReceivedEvent e) {
		ITextComponent message = e.getMessage();
		Iterator<Function<ITextComponent, ITextComponent>> chatFunctionItr = chatFunctions.iterator();
		while (chatFunctionItr.hasNext()) {
			ITextComponent newMessage = chatFunctionItr.next().apply(message);
			if (newMessage == null) {
				e.setCanceled(true);
				return;
			} else if (newMessage != message) {
				e.setMessage(newMessage);
				return;
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedOut(PlayerLoggedOutEvent e) {
		if (e.player != Minecraft.getMinecraft().thePlayer) {
			return;
		}
		chatFunctions.clear();
	}

	public static void addChatFunction(Function<ITextComponent, ITextComponent> chatFunction,
			EnumChatVisibility minimumVisibilityDisplayedOn) {
		if (Minecraft.getMinecraft().gameSettings.chatVisibility.getChatVisibility() <= minimumVisibilityDisplayedOn
				.getChatVisibility()) {
			chatFunctions.add(chatFunction);
		}
	}

	public static void addChatFunction(Function<ITextComponent, ITextComponent> chatFunction) {
		addChatFunction(chatFunction, EnumChatVisibility.SYSTEM);
	}

	public static void blockNext(final Predicate<ITextComponent> predicate,
			EnumChatVisibility minimumVisibilityDisplayedOn) {
		addChatFunction(new Function<ITextComponent, ITextComponent>() {
			@Override
			public ITextComponent apply(ITextComponent message) {
				return predicate.apply(message) ? null : message;
			}
		}, minimumVisibilityDisplayedOn);
	}

	public static void blockNext(Predicate<ITextComponent> predicate) {
		blockNext(predicate, EnumChatVisibility.SYSTEM);
	}

	public static void blockNext(ITextComponent message, EnumChatVisibility minimumVisibilityDisplayedOn) {
		blockNext(Predicates.equalTo(message), minimumVisibilityDisplayedOn);
	}

	public static void blockNext(ITextComponent message) {
		blockNext(message, EnumChatVisibility.SYSTEM);
	}

	public static void blockNext(String message, EnumChatVisibility minimumVisibilityDisplayedOn) {
		blockNext(new TextComponentString(message), minimumVisibilityDisplayedOn);
	}

	public static void blockNext(String message) {
		blockNext(message, EnumChatVisibility.SYSTEM);
	}

	public static void blockNextTranslation(final String translationKey,
			EnumChatVisibility minimumVisibilityDisplayedOn) {
		blockNext(new Predicate<ITextComponent>() {
			@Override
			public boolean apply(ITextComponent message) {
				if (!(message instanceof TextComponentTranslation)) {
					return false;
				}
				return translationKey.equals(((TextComponentTranslation) message).getKey());
			}
		}, minimumVisibilityDisplayedOn);
	}

	public static void addTranslationFunction(final Function<String, String> translationFunction,
			EnumChatVisibility minimumVisibilityDisplayedOn) {
		addChatFunction(new Function<ITextComponent, ITextComponent>() {
			@Override
			public ITextComponent apply(ITextComponent message) {
				if (!(message instanceof TextComponentTranslation)) {
					return message;
				}
				String translationKey = ((TextComponentTranslation) message).getKey();
				String newTranslationKey = translationFunction.apply(translationKey);
				if (newTranslationKey == null) {
					return null;
				} else if (newTranslationKey != translationKey) {
					TextComponentTranslation copied = (TextComponentTranslation) message.createCopy();
					TextComponentTranslation newMessage = new TextComponentTranslation(newTranslationKey,
							copied.getFormatArgs());
					for (ITextComponent sibling : copied.getSiblings()) {
						newMessage.appendSibling(sibling);
					}
					newMessage.setStyle(copied.getStyle());
					return newMessage;
				} else {
					return message;
				}
			}
		});
	}

	public static void addTranslationFunction(Function<String, String> translationFunction) {
		addTranslationFunction(translationFunction, EnumChatVisibility.SYSTEM);
	}

	public static void blockNextTranslation(String translationKey) {
		blockNextTranslation(translationKey, EnumChatVisibility.SYSTEM);
	}

	public static void blockNextTranslation(EnumChatVisibility minimumVisibilityDisplayedOn, String translationKey,
			Object... formatArgs) {
		blockNext(new TextComponentTranslation(translationKey, formatArgs), minimumVisibilityDisplayedOn);
	}

	public static void blockNextTranslation(String translationKey, Object... formatArgs) {
		blockNextTranslation(EnumChatVisibility.SYSTEM, translationKey, formatArgs);
	}

	public static void blockCommandFeedback(final String... possibleFeedbacks) {
		addTranslationFunction(new Function<String, String>() {
			@Override
			public String apply(String translationKey) {
				if ("commands.generic.permission".equals(translationKey)) {
					return "redbuilder.noCommandPermission";
				}
				for (String possibleFeedback : possibleFeedbacks) {
					if (possibleFeedback.equals(translationKey)) {
						return null;
					}
				}
				return translationKey;
			}
		});
	}

	public static void setBlock(BlockPos pos, IBlockState state) {
		setBlock(pos, state, null);
	}

	public static void setBlock(BlockPos pos, IBlockState state, NBTTagCompound tileEntityData) {
		blockCommandFeedback("commands.setblock.success", "commands.setblock.outOfWorld", "commands.setblock.noChange");
		Block block = state.getBlock();
		Minecraft.getMinecraft().thePlayer.sendChatMessage(String.format("/setblock %d %d %d %s %d %s", pos.getX(),
				pos.getY(), pos.getZ(), ForgeRegistries.BLOCKS.getKey(block), block.getMetaFromState(state),
				tileEntityData == null ? "" : NBTToJson.getJsonFromTag(tileEntityData)));
	}

}
