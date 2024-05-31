package net.hmcts.arch.civil.access.service;

import net.hmcts.arch.civil.access.model.Case;
import net.hmcts.arch.civil.access.model.ClaimCase;
import net.hmcts.arch.civil.access.model.GeneralApplicationCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CCD
{
	private static final Map<String, Case> cases = new HashMap<>();

	public static void add(Case aCase)
	{
		cases.put(aCase.getId(), aCase);
	}

	public static Case getCase(String id)
	{
		return cases.get(id);
	}

	public static ClaimCase getClaimCase(String id)
	{
		return (ClaimCase)(cases.get(id));
	}

	public static GeneralApplicationCase getGeneralApplicationCase(String id)
	{
		return (GeneralApplicationCase)(cases.get(id));
	}

	public static <T> Stream<T> find(Predicate<Case> filter, Class<T> caseClass)
	{
		return
				cases.values().stream()
				.filter(filter)
				.map(c -> (T)c);
	}
}
