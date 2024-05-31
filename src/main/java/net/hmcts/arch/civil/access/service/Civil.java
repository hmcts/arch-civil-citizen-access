package net.hmcts.arch.civil.access.service;

import net.hmcts.arch.civil.access.model.ClaimCase;
import net.hmcts.arch.civil.access.model.GeneralApplicationCase;
import net.hmcts.arch.civil.access.model.RoleAssignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Civil
{
	public static String RAS_PROCESS_ID = "civil-citizen-access";

	public static ClaimCase createClaim(String id, String name)
	{
		ClaimCase claimCase = new ClaimCase(id, name);
		CCD.add(claimCase);
		return claimCase;
	}

	public static GeneralApplicationCase createGeneralApplication(String id, String claimId, String name, boolean withNotice, String ... roleMappings)
	{
		Map<String, String> roleMap = new HashMap<>();
		for (int i = 0; i < roleMappings.length; i += 2)
		{
			roleMap.put(roleMappings[i], roleMappings[i + 1]);
		}
		return createGeneralApplication(id, claimId, name, withNotice, roleMap);
	}


	public static GeneralApplicationCase createGeneralApplication(String id, String claimId, String name, boolean withNotice, Map<String, String> roleMappings)
	{
		GeneralApplicationCase generalApplicationCase = new GeneralApplicationCase(id, name, claimId, withNotice, roleMappings);
		CCD.add(generalApplicationCase);
		// Set the initial role assignments after creating a GA
		refreshGeneralApplicationCitizenAccess(generalApplicationCase);
		return generalApplicationCase;
	}

	public static void addUserToClaim(String userId, String claimCaseId, String roleName)
	{
		RoleAssignment roleAssignment = new RoleAssignment(userId, claimCaseId, roleName);
		String reference = claimCaseId + "/" + userId + "/" + roleName;
		RAS.add(RAS_PROCESS_ID, reference, roleAssignment);
		// Refresh role assignments after adding or removing a user from a claim
		refreshGeneralApplicationCitizenAccessForClaim(claimCaseId);
	}

	public static void removeUserFromClaim(String userId, String claimCaseId)
	{
		RAS.remove(userId, claimCaseId);
		// Refresh role assignments after adding or removing a user from a claim
		refreshGeneralApplicationCitizenAccessForClaim(claimCaseId);
	}

	public static void setWithNotice(String generalApplicationCaseId, boolean withNotice)
	{
		GeneralApplicationCase generalApplicationCase = CCD.getGeneralApplicationCase(generalApplicationCaseId);
		generalApplicationCase.setWithNotice(withNotice);
		// REFRESH role assignments after changing the with / without notice status of a GA
		refreshGeneralApplicationCitizenAccess(generalApplicationCase);
	}

	public static void refreshGeneralApplicationCitizenAccessForClaim(String claimCaseId)
	{
		CCD.find(c -> c instanceof GeneralApplicationCase && ((GeneralApplicationCase)c).getClaimId().equals(claimCaseId), GeneralApplicationCase.class)
		.forEach(Civil::refreshGeneralApplicationCitizenAccess);
	}

	public static void refreshGeneralApplicationCitizenAccess(GeneralApplicationCase generalApplicationCase)
	{
		String claimCaseId = generalApplicationCase.getClaimId();
		// List all the users with each role on the claim case
		Map<String, Set<String>> userIdsFromRole = new HashMap<>();
		RAS.findForCase(claimCaseId).forEach(ra -> userIdsFromRole.computeIfAbsent(ra.getRoleName(), r -> new HashSet<String>()).add(ra.getUserId()));
		// To collect the role assignments which should exist for this GA case
		Set<RoleAssignment> roleAssignments = new HashSet<>();
		// Find the roles in the GA case which should have access to it (takes account of with / without notice)
		for (String gaRoleName : generalApplicationCase.getRolesToGiveAccess())
		{
			// What claim role maps to this GA role?
			String claimRoleName = generalApplicationCase.getRoleMappings().get(gaRoleName);
			// All the users with that role on the claim case
			Set<String> userIds = userIdsFromRole.get(claimRoleName);
			// Create a GA role assignment for each user
			userIds.forEach(uid -> roleAssignments.add(new RoleAssignment(uid, generalApplicationCase.getId(), gaRoleName)));
		}
		// Update the citizen role assignments in the RAS for this GA case
		RAS.replace(RAS_PROCESS_ID, generalApplicationCase.getId(), roleAssignments);
	}
}
