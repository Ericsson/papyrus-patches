package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.internal.SelfDescribingValue;

public class CollectionMatchers {
	public static <T> Matcher<Iterable<T>> inAnyOrder(T... items) {
		return new InAnyOrder<Iterable<T>, T>(items);
	}
	
	private static class InAnyOrder<T extends Iterable<S>,S> extends BaseMatcher<T> {
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
}
