package net.earthcomputer.redbuilder;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class BrokenJsonToNBT {

	private static final Pattern NUMBER = Pattern.compile("[+-]?(?:[0-9]+[bBlLsS]?)|(?:[0-9]*\\.?[0-9]+[DdFf]?)");
	private static final Pattern INT_ARRAY_CONTENTS = Pattern.compile("[-+\\d,]+");

	private List<String> tokens;
	private int index = 0;

	private BrokenJsonToNBT(List<String> tokens) {
		this.tokens = tokens;
	}

	public static List<NBTBase> getPossibleTagsFromBrokenJson(String brokenJson) {
		// Tokenize
		List<List<String>> possibleTokens = new Tokenizer(brokenJson).tokenize();

		// Parse
		List<NBTBase> possibleTags = Lists.newArrayList();
		for (List<String> tokens : possibleTokens) {
			try {
				possibleTags.add(new BrokenJsonToNBT(tokens).parse());
			} catch (NBTException e) {
				// ignore
			}
		}
		return possibleTags;
	}

	private NBTBase parse() throws NBTException {
		// Assure we haven't reached the end of the string
		if (index == tokens.size()) {
			throw new NBTException("Empty string");
		}

		// Parse differently based on this token
		String token = tokens.get(index);
		index++;
		if ("{".equals(token)) {
			return parseCompound();
		} else if ("[".equals(token)) {
			if (isListIntArray()) {
				return parseIntArray();
			} else {
				return parseList();
			}
		} else if (Tokenizer.isStandaloneCharacter(token.charAt(0))) {
			throw new NBTException("Unexpected token");
		} else {
			return parseStringOrNumber(token);
		}
	}

	private NBTTagCompound parseCompound() throws NBTException {
		NBTTagCompound compound = new NBTTagCompound();
		try {
			while (true) {
				String token = tokens.get(index);

				// Check for end of compound
				if ("}".equals(token)) {
					index++;
					break;
				}

				// Check for , separator
				if (",".equals(token)) {
					if (compound.hasNoTags()) {
						throw new NBTException("Cannot start an NBTTagCompound with a ,");
					}
					index++;
					token = tokens.get(index);
				} else {
					if (!compound.hasNoTags()) {
						throw new NBTException("Unable to locate , separator between NBTTagCompound tags");
					}
				}

				// Name
				String name = token;
				if (Tokenizer.isStandaloneCharacter(name.charAt(0))) {
					throw new NBTException("Invalid NBTTagCompound name: " + name);
				}

				index++;
				token = tokens.get(index);

				// Check for : separator
				if (!":".equals(token)) {
					throw new NBTException("Unable to locate : name/value separator in NBTTagCompound");
				}

				index++;

				// Value
				NBTBase value = parse();

				// Add name/value pair
				compound.setTag(name, value);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new NBTException("Reached the end of the string unexpectedly");
		}
		return compound;
	}

	private boolean isListIntArray() {
		boolean isIntArray = true;
		int lookahead = 0;

		while (index + lookahead < tokens.size()) {
			String token = tokens.get(index + lookahead);

			// Check for end of list
			if ("]".equals(token)) {
				if (lookahead == 0) {
					// "[]" is not an int array (as opposed to "[,]")
					isIntArray = false;
				}
				break;
			}
			if (!INT_ARRAY_CONTENTS.matcher(token).matches()) {
				// If not a number or , separator, this can't be an int array
				isIntArray = false;
				break;
			}
			lookahead++;
		}
		return isIntArray;
	}

	private NBTTagList parseList() throws NBTException {
		NBTTagList list = new NBTTagList();
		try {
			while (true) {
				String token = tokens.get(index);

				// Check for end of list
				if ("]".equals(token)) {
					index++;
					break;
				}

				// Check for , separator
				if (",".equals(token)) {
					if (list.hasNoTags()) {
						throw new NBTException("Cannot start an NBTTagList with a ,");
					}
					index++;
					token = tokens.get(index);
				} else {
					if (!list.hasNoTags()) {
						throw new NBTException("Unable to find , separator in NBTTagList");
					}
				}

				// Check for list indexes and ignore them if present
				if (":".equals(tokens.get(index + 1))) {
					// Make sure the list index is a number (extra validation)
					try {
						int listIndex = Integer.parseInt(token);
						if (listIndex < 0) {
							throw new NBTException("Invalid list index in NBTTagList");
						}
					} catch (NumberFormatException e) {
						throw new NBTException("Invalid list index in NBTTagList");
					}
					index += 2;
					token = tokens.get(index);
				}

				// Parse the actual element
				NBTBase element = parse();

				// Make sure the element matches the element type of the list
				if (!list.hasNoTags() && element.getId() != list.getTagType()) {
					throw new NBTException("Cannot add mismatching type to NBTTagList");
				}

				// Add the element to the list
				list.appendTag(element);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new NBTException("Unexpectedly reached the end of the string");
		}
		return list;
	}

	private NBTTagIntArray parseIntArray() throws NBTException {
		List<Integer> array = Lists.newLinkedList();
		try {
			while (true) {
				String token = tokens.get(index);

				// Check for , separator
				if (",".equals(token)) {
					index++;
					token = tokens.get(index);
					// "[,]" is the only valid time a , can be at the first
					// thing after [
					if (array.isEmpty()) {
						if (!"]".equals(token)) {
							throw new NBTException("Cannot start NBTTagIntArray with a ,");
						}
					}
				} else {
					if (!array.isEmpty()) {
						throw new NBTException("Unable to find , separator between elements of NBTTagIntArray");
					}
				}

				// Check for end of list. This has to be after checking for ,
				// due to the "[,]" case
				if ("]".equals(token)) {
					index++;
					break;
				}

				// Parse the element
				try {
					array.add(Integer.parseInt(token));
				} catch (NumberFormatException e) {
					throw new NBTException("Number format of element in NBTTagIntArray");
				}
				index++;
			}
		} catch (IndexOutOfBoundsException e) {
			throw new NBTException("Unexpectedly reached the end of the string");
		}

		// Convert the list of Integers to an NBTTagIntArray
		int[] intArray = new int[array.size()];
		int i = 0;
		for (int val : array) {
			intArray[i++] = val;
		}
		return new NBTTagIntArray(intArray);
	}

	private NBTBase parseStringOrNumber(String token) {
		if (NUMBER.matcher(token).matches()) {
			// If the data type is specified, go by that
			char type = Character.toLowerCase(token.charAt(token.length() - 1));
			try {
				switch (type) {
				case 'b':
					return new NBTTagByte(Byte.parseByte(token.substring(0, token.length() - 1)));
				case 'd':
					return new NBTTagDouble(Double.parseDouble(token.substring(0, token.length() - 1)));
				case 'f':
					return new NBTTagFloat(Float.parseFloat(token.substring(0, token.length() - 1)));
				case 'l':
					return new NBTTagLong(Long.parseLong(token.substring(0, token.length() - 1)));
				case 's':
					return new NBTTagShort(Short.parseShort(token.substring(0, token.length() - 1)));
				default:
					// If no data type specified, then assume double if there is
					// a decimal point, int otherwise
					if (token.contains(".")) {
						return new NBTTagDouble(Double.parseDouble(token));
					} else {
						return new NBTTagInt(Integer.parseInt(token));
					}
				}
			} catch (NumberFormatException e) {
				// If parsing failed (e.g. due to integer overflow), then return
				// a string
				return new NBTTagString(token);
			}
		} else {
			// Check for boolean types
			if ("true".equals(token)) {
				return new NBTTagByte((byte) 1);
			} else if ("false".equals(token)) {
				return new NBTTagByte((byte) 0);
			} else {
				// Strings could be with or without quotation marks. \s have
				// already been handled by the tokenizer.
				if (token.startsWith("\"")) {
					return new NBTTagString(token.substring(1, token.length() - 1));
				} else {
					return new NBTTagString(token);
				}
			}
		}
	}

	private static class Tokenizer {
		private String input;
		private int index;

		public Tokenizer(String input) {
			this.input = input;
			this.index = 0;
		}

		private Tokenizer(String input, int index) {
			this.input = input;
			this.index = index;
		}

		public List<List<String>> tokenize() {
			List<String> tokens = Lists.newArrayList();

			while (true) {
				// Skip whitespace
				skipWhitespace();

				// Check if we've reached the end of the input
				if (index == input.length()) {
					return Collections.singletonList(tokens);
				}

				char charAt = currentChar();
				// Check for standalone character
				if (isStandaloneCharacter(charAt)) {
					tokens.add(String.valueOf(charAt));
					index++;
				} else {
					// One number is a token
					Matcher numberMatcher = NUMBER.matcher(input);
					if (numberMatcher.find(index) && numberMatcher.start() == index) {
						tokens.add(numberMatcher.group());
						index = numberMatcher.end();
					} else if (charAt == '"') {
						// Do the complex handling of quoted strings
						return parseQuotedStringAndRemainingInput(tokens);
					} else {
						tokens.add(parseUnquotedString());
					}
				}
			}
		}

		private List<List<String>> parseQuotedStringAndRemainingInput(List<String> tokensSoFar) {
			List<List<String>> possibilities = Lists.newArrayList();

			// Vanilla servers do not convert \ to \\ in quoted strings, Forge
			// servers do. This is what most of this complex crap is about.
			if (RedBuilder.instance().isForgeServer()) {
				// Parse the string the easy way. The \" dilemma was handled for
				// us by the Forge server.
				StringBuilder token = new StringBuilder("\"");
				boolean isEscaped = false;
				char charAt;
				index++;

				while ((charAt = currentChar()) != '"' || isEscaped) {
					if (charAt == '\\' && !isEscaped) {
						isEscaped = true;
					} else {
						isEscaped = false;
						token.append(charAt);
					}
					index++;
				}
				// Finish the token with a "

				token.append('"');
				index++;
				tokensSoFar.add(token.toString());
			} else {
				// We have a vanilla server. \" is ambiguous so we return all
				// possibilities and leave it to the parsing stage to test which
				// possibilities are valid.
				StringBuilder token = new StringBuilder("\"");
				boolean isEscaped = false;
				char charAt;
				index++;

				// This tokenizer assumes all \" escape the quotation mark and
				// creates another tokenizer for the other possibility.
				while ((charAt = currentChar()) != '"' || isEscaped) {
					// Check for \ escapes
					if (charAt == '\\' && !isEscaped) {
						isEscaped = true;
					} else {
						isEscaped = false;
					}

					// Test for possibly escaped " and create another tokenizer
					// to handle the other possibility.
					if (charAt == '"' && isEscaped) {
						Tokenizer tokenizer = new Tokenizer(input, index + 1);
						for (List<String> tokensFound : tokenizer.tokenize()) {
							List<String> otherPossibility = Lists.newArrayList();
							otherPossibility.addAll(tokensSoFar);
							otherPossibility.add(token.substring(0, token.length() - 1));
							otherPossibility.addAll(tokensFound);
							possibilities.add(tokensFound);
						}
					}

					// Append the current character to the token we're building.
					token.append(charAt);
					index++;
				}

				// Finish the token with a "
				token.append('"');

				index++;
				tokensSoFar.add(token.toString());
			}

			// Add the parsing of this tokenizer to the list of possibilities
			// from the other tokenizers (this list will be empty in the case of
			// a Forge server), and return it.
			Tokenizer tokenizer = new Tokenizer(input, index);
			for (List<String> tokensFound : tokenizer.tokenize()) {
				List<String> possibility = Lists.newArrayList();
				possibility.addAll(tokensSoFar);
				possibility.addAll(tokensFound);
				possibilities.add(possibility);
			}
			return possibilities;
		}

		private String parseUnquotedString() {
			// First remove everything before the start of this token
			String token = input.substring(index);

			// Get possible ends of this token, and find which one comes first
			int closeListIndex = token.indexOf(']');
			int closeCompoundIndex = token.indexOf('}');
			int colonIndex = token.indexOf(':');
			int commaIndex = token.indexOf(',');
			int closestIndex = closeListIndex;
			if (closeCompoundIndex != -1 && (closeCompoundIndex < closestIndex || closestIndex == -1)) {
				closestIndex = closeCompoundIndex;
			}
			if (colonIndex != -1 && (colonIndex < closestIndex || closestIndex == -1)) {
				closestIndex = colonIndex;
			}
			if (commaIndex != -1 && (commaIndex < closestIndex || closestIndex == -1)) {
				closestIndex = commaIndex;
			}
			if (closestIndex != -1) {
				token = token.substring(0, closestIndex);
			}

			// Remove trailing whitespace
			while (Character.isWhitespace(token.charAt(token.length() - 1))) {
				token = token.substring(0, token.length() - 1);
			}

			// Increment index now we've found the token
			index += token.length();
			return token;
		}

		private void skipWhitespace() {
			while (index != input.length() && Character.isWhitespace(currentChar())) {
				index++;
			}
		}

		private char currentChar() {
			return input.charAt(index);
		}

		public static boolean isStandaloneCharacter(char character) {
			return character == '{' || character == '}' || character == '[' || character == ']' || character == ':'
					|| character == ',';
		}
	}
}
