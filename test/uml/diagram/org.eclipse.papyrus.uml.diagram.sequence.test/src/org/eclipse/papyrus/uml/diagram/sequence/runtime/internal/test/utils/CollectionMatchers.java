package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.internal.SelfDescribingValue;

public class CollectionMatchers {
	public static <T> Matcher<Iterable<? extends T>> inAnyOrder(T... items) {
		return new InAnyOrder<Iterable<? extends T>, T>(items);
	}
	
	public static <T> Matcher<Iterable<? extends T>> equalInOrder(T... items) {
		return new InOrder<Iterable<? extends T>, T>(false, items);
	}

	public static <T> Matcher<Iterable<? extends T>> sameInOrder(T... items) {
		return new InOrder<Iterable<? extends T>, T>(true, items);
	}

	private static class InAnyOrder<T extends Iterable<? extends S>,S> extends BaseMatcher<T> {
		public InAnyOrder(S ...items) {
			this.items = Arrays.asList(items);
		}
		
		@Override
		public boolean matches(Object item) {
			int nCount = 0;
			Iterable<S> it = (Iterable<S>)item;
			for (S s : it) {
				if (!items.contains(s))
					return false;
				nCount++;
			}
			return nCount == items.size();			
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("a iterable with ")
				.appendList("[", ",", "]", Arrays.asList(items).stream().
						map(d -> new SelfDescribingValue(d)).collect(Collectors.toList()))
				.appendText("");
			
		}
		
		private List<S> items;
	}

	private static class InOrder<T extends Iterable<? extends S>,S> extends BaseMatcher<T> {
		public InOrder(S ...items) {
			this(true, items);
		}
		
		public InOrder(boolean same, S ...items) {
			this.items = items;
			this.same = same;
		}

		@Override
		public boolean matches(Object item) {
			Iterable<S> it = (Iterable<S>)item;
			int index = 0;
			for (S s : it) {
				S o = items[index++];
				if (same && !CoreMatchers.sameInstance(o).matches(s))
					return false;
				else if (!same && !CoreMatchers.is(o).matches(s))
					return false;
			}
			return true;
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("a iterable with ")
				.appendList("[", ",", "]", Arrays.asList(items).stream().
						map(d -> new SelfDescribingValue(d)).collect(Collectors.toList()))
				.appendText("");
			
		}
		
		private S[] items;
		private boolean same;
	}
}
