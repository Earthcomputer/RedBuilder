package net.earthcomputer.redbuilder.network;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.earthcomputer.redbuilder.BrokenJsonToNBT;
import net.earthcomputer.redbuilder.IDelayedReturnSite;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientChatUtils {

	private static final ClientChatUtils INSTANCE = new ClientChatUtils();

	private ClientChatUtils() {
	}

	public static ClientChatUtils instance() {
		return INSTANCE;
	}

	private static final Set<Function<ITextComponent, ITextComponent>> chatFunctions = Sets.newHashSet();

	@SubscribeEvent
	public void chatReceived(ClientChatReceivedEvent e) {
		ITextComponent message = e.getMessage();
		ITextComponent newMessage = message;
		Function<ITextComponent, ITextComponent> functionToRemove = null;
		for (Function<ITextComponent, ITextComponent> function : chatFunctions) {
			newMessage = function.apply(message);
			if (newMessage != message) {
				functionToRemove = function;
				break;
			}
		}
		if (functionToRemove != null) {
			chatFunctions.remove(functionToRemove);
			if (newMessage == null) {
				e.setCanceled(true);
			} else {
				e.setMessage(newMessage);
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

	public static void getTileEntityData(BlockPos pos, final IDelayedReturnSite<List<NBTTagCompound>> handler) {
		addChatFunction(new Function<ITextComponent, ITextComponent>() {
			@Override
			public ITextComponent apply(ITextComponent message) {
				if (!(message instanceof TextComponentTranslation)) {
					return message;
				}
				TextComponentTranslation translation = (TextComponentTranslation) message;
				String translationKey = translation.getKey();
				if ("commands.generic.permission".equals(translationKey)) {
					return CommonChatUtils.buildErrorMessage("redbuilder.noCommandPermission");
				}
				if ("commands.blockdata.outOfWorld".equals(translationKey)
						|| "commands.blockdata.notValid".equals(translationKey)) {
					return null;
				}
				if ("commands.blockdata.failed".equals(translationKey)) {
					String rawTileEntityData = (String) translation.getFormatArgs()[0];
					List<NBTTagCompound> possibleTileEntityData = Lists.newArrayList();
					for (NBTBase nbt : BrokenJsonToNBT.getPossibleTagsFromBrokenJson(rawTileEntityData)) {
						if (nbt instanceof NBTTagCompound) {
							possibleTileEntityData.add((NBTTagCompound) nbt);
						}
					}
					handler.returnValue(possibleTileEntityData);
					return null;
				}
				return message;
			}
		});
		sendCommand("/blockdata %d %d %d {}", pos.getX(), pos.getY(), pos.getZ());
	}

	public static void setBlock(BlockPos pos, IBlockState state) {
		blockCommandFeedback("commands.setblock.success", "commands.setblock.outOfWorld", "commands.setblock.noChange");
		Block block = state.getBlock();
		sendCommand("/setblock %d %d %d %s %d", pos.getX(), pos.getY(), pos.getZ(), block.delegate.name(),
				block.getMetaFromState(state));
	}

	public static void sendCommand(String command, Object... formatArgs) {
		Minecraft.getMinecraft().thePlayer.sendChatMessage(String.format(command, formatArgs));
	}

}
