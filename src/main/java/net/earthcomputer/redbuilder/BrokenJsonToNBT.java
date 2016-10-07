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

	private int index = 0;

	private BrokenJsonToNBT() {
	}

	public static List<NBTBase> getPossibleTagsFromBrokenJson(String brokenJson) {
		List<List<String>> possibleTokens = new Tokenizer(brokenJson).tokenize();
		List<NBTBase> possibleTags = Lists.newArrayList();
		for (List<String> tokens : possibleTokens) {
			try {
				possibleTags.add(new BrokenJsonToNBT().parse(tokens));
			} catch (NBTException e) {
				// ignore
			}
		}
		return possibleTags;
	}

	private NBTBase parse(List<String> tokens) throws NBTException {
		if (index == tokens.size()) {
			throw new NBTException("Empty string");
		}
		String token = tokens.get(index);
		index++;
		if ("{".equals(token)) {
			NBTTagCompound compound = new NBTTagCompound();
			try {
				while (true) {
					token = tokens.get(index);
					if ("}".equals(token)) {
						index++;
						break;
					}
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
					String name = token;
					if ("{".equals(name) || "[".equals(name) || "}".equals(name) || "]".equals(name) || ":".equals(name)
							|| ",".equals(name)) {
						throw new NBTException("Invalid NBTTagCompound name: " + name);
					}
					index++;
					token = tokens.get(index);
					if (!":".equals(token)) {
						throw new NBTException("Unable to locate : name/value separator in NBTTagCompound");
					}
					index++;
					NBTBase value = parse(tokens);
					compound.setTag(name, value);
				}
			} catch (IndexOutOfBoundsException e) {
				throw new NBTException("Reached the end of the string unexpectedly");
			}
			return compound;
		} else if ("[".equals(token)) {
			boolean isIntArray = true;
			int lookahead = 0;
			while (index + lookahead < tokens.size()) {
				token = tokens.get(index + lookahead);
				if ("]".equals(token)) {
					if (lookahead == 0) {
						isIntArray = false;
					}
					break;
				}
				if (!INT_ARRAY_CONTENTS.matcher(token).matches()) {
					isIntArray = false;
					break;
				}
				lookahead++;
			}
			if (isIntArray) {
				List<Integer> array = Lists.newLinkedList();
				try {
					while (true) {
						token = tokens.get(index);
						if (",".equals(token)) {
							index++;
							token = tokens.get(index);
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
						if ("]".equals(token)) {
							index++;
							break;
						}
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
				int[] intArray = new int[array.size()];
				int i = 0;
				for (int val : array) {
					intArray[i++] = val;
				}
				return new NBTTagIntArray(intArray);
			} else {
				NBTTagList list = new NBTTagList();
				try {
					while (true) {
						token = tokens.get(index);
						if ("]".equals(token)) {
							index++;
							break;
						}
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
						if (":".equals(tokens.get(index + 1))) {
							// This means we have the stupid indexes. Expect an
							// integer
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
						NBTBase element = parse(tokens);
						if (!list.hasNoTags() && element.getId() != list.getTagType()) {
							throw new NBTException("Cannot add mismatching type to NBTTagList");
						}
						list.appendTag(element);
					}
				} catch (IndexOutOfBoundsException e) {
					throw new NBTException("Unexpectedly reached the end of the string");
				}
				return list;
			}
		} else if ("}".equals(token) || "]".equals(token) || ":".equals(token) || ",".equals(token)) {
			throw new NBTException("Unexpected token");
		} else {
			if (NUMBER.matcher(token).matches()) {
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
						if (token.contains(".")) {
							return new NBTTagDouble(Double.parseDouble(token));
						} else {
							return new NBTTagInt(Integer.parseInt(token));
						}
					}
				} catch (NumberFormatException e) {
					return new NBTTagString(token);
				}
			} else {
				if ("true".equals(token)) {
					return new NBTTagByte((byte) 1);
				} else if ("false".equals(token)) {
					return new NBTTagByte((byte) 0);
				} else {
					if (token.startsWith("\"")) {
						return new NBTTagString(token.substring(1, token.length() - 1));
					} else {
						return new NBTTagString(token);
					}
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
				skipWhitespace();
				if (index == input.length()) {
					return Collections.singletonList(tokens);
				}
				char charAt = currentChar();
				if (charAt == '[' || charAt == ']' || charAt == '{' || charAt == '}' || charAt == ':'
						|| charAt == ',') {
					tokens.add(String.valueOf(charAt));
					index++;
				} else {
					Matcher numberMatcher = NUMBER.matcher(input);
					if (numberMatcher.find(index) && numberMatcher.start() == index) {
						tokens.add(numberMatcher.group());
						index = numberMatcher.end();
					} else if (charAt == '"') {
						if (RedBuilder.getInstance().forgeServer) {
							StringBuilder token = new StringBuilder("\"");
							boolean isEscaped = false;
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
							token.append('"');
							index++;
							tokens.add(token.toString());
						} else {
							List<List<String>> otherPossibilities = Lists.newArrayList();
							StringBuilder token = new StringBuilder("\"");
							boolean isEscaped = false;
							index++;
							while ((charAt = currentChar()) != '"' || isEscaped) {
								if (charAt == '\\' && !isEscaped) {
									isEscaped = true;
								} else {
									isEscaped = false;
								}
								if (charAt == '"' && isEscaped) {
									Tokenizer tokenizer = new Tokenizer(input, index + 1);
									for (List<String> tokensFound : tokenizer.tokenize()) {
										List<String> otherPossibility = Lists.newArrayList();
										otherPossibility.addAll(tokens);
										otherPossibility.add(token.substring(0, token.length() - 1));
										otherPossibility.addAll(tokensFound);
										otherPossibilities.add(tokensFound);
									}
								}
								token.append(charAt);
								index++;
							}
							token.append('"');
							index++;
							tokens.add(token.toString());
							if (!otherPossibilities.isEmpty()) {
								Tokenizer tokenizer = new Tokenizer(input, index);
								for (List<String> tokensFound : tokenizer.tokenize()) {
									List<String> possibility = Lists.newArrayList();
									possibility.addAll(tokens);
									possibility.addAll(tokensFound);
									otherPossibilities.add(possibility);
								}
								return otherPossibilities;
							}
						}
					} else {
						String token = input.substring(index);
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
						while (Character.isWhitespace(token.charAt(token.length() - 1))) {
							token = token.substring(0, token.length() - 1);
						}
						tokens.add(token);
						index += token.length();
					}
				}
			}
		}

		private void skipWhitespace() {
			while (index != input.length() && Character.isWhitespace(currentChar())) {
				index++;
			}
		}

		private char currentChar() {
			return input.charAt(index);
		}
	}
}
