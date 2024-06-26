package net.hmcts.arch.civil.access.model;

import lombok.Getter;

import java.util.Set;

@Getter
public class ClaimCase extends Case
{
	public static String C_CLAIMANT_1_ROLE = "c-claimant-01";
	public static String C_CLAIMANT_2_ROLE = "c-claimant-02";
	public static String C_DEFENDANT_1_ROLE = "c-defendant-01";
	public static String C_DEFENDANT_2_ROLE = "c-defendant-02";

	public ClaimCase(String id)
	{
		super(id, "Claim");
	}
}
