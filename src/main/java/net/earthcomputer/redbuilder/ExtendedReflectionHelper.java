package net.earthcomputer.redbuilder;

import com.google.common.base.Throwables;

public class ExtendedReflectionHelper {

	private ExtendedReflectionHelper() {
	}

	public static <T> T newInstance(Class<T> clazz, Object... args) {
		Class<?>[] argTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = args[i].getClass();
		}
		try {
			return clazz.getConstructor(argTypes).newInstance(args);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

}
