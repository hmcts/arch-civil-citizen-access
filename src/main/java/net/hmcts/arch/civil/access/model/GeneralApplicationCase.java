package net.hmcts.arch.civil.access.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class GeneralApplicationCase extends Case
{
	public static String GA_APPLICANT_1_ROLE = "ga-applicant-01";
	public static String GA_APPLICANT_2_ROLE = "ga-applicant-02";
	public static String GA_RESPONDENT_1_ROLE = "ga-respondent-01";
	public static String GA_RESPONDENT_2_ROLE = "ga-respondent-02";

	private final String claimId;

	@Setter
	private boolean withNotice;

	/*
	 * A map of GA Role -> Claim Role.
	 * An entry mapping G -> C means "any users with role C in the claim should get role G in this application"
	 * However, whether the role is actually given to the user is further modified by the with / without notice
	 * status of the application.
	 */
	private final Map<String, String> roleMappings;

	public GeneralApplicationCase(String id, String claimId, boolean withNotice, Map<String,String> roleMappings)
	{
		super(id, "GeneralApplication");
		this.claimId = claimId;
		this.withNotice = withNotice;
		this.roleMappings = new HashMap<>(roleMappings);
	}

	/*
	 * Returns a list of the citizen roles which should be given access to this application.
	 * This is all the possible roles if the application is with notice, but just the
	 * applicant roles if the application is without notice.
	 */
	public Set<String> getRolesToGiveAccess()
	{
		return
				withNotice ?
				Set.of(GA_APPLICANT_1_ROLE, GA_APPLICANT_2_ROLE, GA_RESPONDENT_1_ROLE, GA_RESPONDENT_2_ROLE) :
				Set.of(GA_APPLICANT_1_ROLE, GA_APPLICANT_2_ROLE);
	}
}
