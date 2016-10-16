package net.earthcomputer.redbuilder.util;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class CommonChatUtils {

	private CommonChatUtils() {
	}
	
	public static TextComponentTranslation buildErrorMessage(String translationKey, Object... formatArgs) {
		TextComponentTranslation message = new TextComponentTranslation(translationKey, formatArgs);
		message.getStyle().setColor(TextFormatting.RED);
		return message;
	}
	
}
