package net.hmcts.arch.civil.access.model;

import lombok.Getter;
import lombok.Value;

import java.util.Set;

public abstract class Case
{
	@Getter
	private final String id;

	@Getter
	private final String type;

	@Getter
	private final String name;

	public Case(String id, String type, String name)
	{
		this.id = id;
		this.type = type;
		this.name = name;
	}

	public abstract Set<String> getCitizenRoles();
}
