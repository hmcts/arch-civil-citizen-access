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


	public Case(String id, String type)
	{
		this.id = id;
		this.type = type;
	}
}
