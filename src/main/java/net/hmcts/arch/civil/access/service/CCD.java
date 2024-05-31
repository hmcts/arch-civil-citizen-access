package net.hmcts.arch.civil.access.service;

import net.hmcts.arch.civil.access.model.Case;
import net.hmcts.arch.civil.access.model.GeneralApplicationCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/*
 * Simple repository of cases, simulating CCD.
 */
public class CCD
{
	private static final Map<String, Case> cases = new HashMap<>();

	public static void add(Case aCase)
	{
		cases.put(aCase.getId(), aCase);
	}

	public static GeneralApplicationCase getGeneralApplicationCase(String id)
	{
		return (GeneralApplicationCase)(cases.get(id));
	}

	public static <T> Stream<T> find(Predicate<Case> filter, Class<T> caseClass)
	{
		@SuppressWarnings("unchecked")
		Stream<T> tStream = cases.values().stream()
				.filter(filter)
				.map(c -> (T) c);
		return
				tStream;
	}
}
