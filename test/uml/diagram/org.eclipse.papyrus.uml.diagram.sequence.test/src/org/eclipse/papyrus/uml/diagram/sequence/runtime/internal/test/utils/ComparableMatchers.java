package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ComparableMatchers {
	public static <T extends Comparable<T>> Matcher<T> lessThan(T value) {
		return new LessThan<T>(value);
	}
	
	public static <T extends Comparable<T>> Matcher<T> lessEquals(T value) {
		return new LessEquals<T>(value);
	}
	
	public static <T extends Comparable<T>> Matcher<T> greaterThan(T value) {
		return new GreaterThan<T>(value);
	}

	public static <T extends Comparable<T>> Matcher<T> greaterEquals(T value) {
		return new GreaterEquals<T>(value);
	}
	
	private static class LessThan<T extends Comparable<T>> extends BaseMatcher<T> {
		public LessThan(T value) {
			super();
			this.value = value;
		}

		@Override
		public boolean matches(Object item) {
			T o = (T)item;
			int res = o.compareTo(value);
			return res < 0;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(" less than ").appendValue(value);
		}
		
		private T value;
	}

	private static class LessEquals<T extends Comparable<T>> extends BaseMatcher<T> {
		public LessEquals(T value) {
			super();
			this.value = value;
		}

		@Override
		public boolean matches(Object item) {
			T o = (T)item;
			int res = o.compareTo(value);
			return res <= 0;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(" less than ").appendValue(value);
		}
		
		private T value;
	}

	private static class GreaterThan<T extends Comparable<T>> extends BaseMatcher<T> {
		public GreaterThan(T value) {
			super();
			this.value = value;
		}

		@Override
		public boolean matches(Object item) {
			T o = (T)item;
			int res = o.compareTo(value);
			return res > 0;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(" less than ").appendValue(value);
		}
		
		private T value;
	}

	private static class GreaterEquals<T extends Comparable<T>> extends BaseMatcher<T> {
		public GreaterEquals(T value) {
			super();
			this.value = value;
		}

		@Override
		public boolean matches(Object item) {
			T o = (T)item;
			int res = o.compareTo(value);
			return res >= 0;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(" less than ").appendValue(value);
		}
		
		private T value;
	}
}
