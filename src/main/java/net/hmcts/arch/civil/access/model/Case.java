package net.hmcts.arch.civil.access.model;

import lombok.Getter;
import lombok.Value;

import java.util.Set;

@Getter
public abstract class Case
{
	private final String id;

	private final String type;


	public Case(String id, String type)
	{
		this.id = id;
		this.type = type;
	}
}
