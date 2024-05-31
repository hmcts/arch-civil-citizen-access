package net.hmcts.arch.civil.access.service;

import net.hmcts.arch.civil.access.model.RoleAssignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/*
 * Repository of role assignments, simulating the RAS.
 */
public class RAS
{
	// Since the main point of this demonstration is to use the "replace existing by process and reference"
	// feature of the RAS, we store role assignments in a form which makes that easy.
	private static final Map<String, Set<RoleAssignment>> roleAssignmentsByProcessAndReference = new HashMap<>();

	/*
	 * Removes all role assignments with the given process + reference, and inserts the provided set
	 * instead.  This is a feature of the role assignment service (note that the production RAS also
	 * has some optimisations, such as not 'churning' a role assignment by deleting it and re-creating
	 * a new, identical one).
	 */
	public static void replace(String process, String reference, Set<RoleAssignment> roles)
	{
		String key = process + "//" + reference;
		roleAssignmentsByProcessAndReference.put(key, roles);
	}

	/*
	 * Add a role assignment.
	 */
	public static void add(String process, String reference, RoleAssignment roleAssignment)
	{
		String key = process + "//" + reference;
		Set<RoleAssignment> roleAssignments = roleAssignmentsByProcessAndReference.computeIfAbsent(key, k -> new HashSet<>());
		roleAssignments.add(roleAssignment);
	}

	/*
	 * Remove all of a user's role assignments for a given case.
	 */
	public static void remove(String userId, String caseId)
	{
		for (Set<RoleAssignment> roleAssignments : roleAssignmentsByProcessAndReference.values())
		{
			roleAssignments.removeIf(ra -> userId.equals(ra.getUserId()) && caseId.equals(ra.getCaseId()));
		}
	}

	public static Stream<RoleAssignment> findByCase(String caseId)
	{
		return roleAssignmentsByProcessAndReference.values().stream()
				.flatMap(Set::stream)
				.filter(ra -> caseId.equals(ra.getCaseId()));
	}

	public static Stream<RoleAssignment> stream()
	{
		return roleAssignmentsByProcessAndReference.values().stream()
		.flatMap(Set::stream);
	}
}
