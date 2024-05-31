package net.hmcts.arch.civil.access.service;

import net.hmcts.arch.civil.access.model.RoleAssignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RAS
{
	private static final Map<String, Set<RoleAssignment>> roleAssignmentsByProcessAndReference = new HashMap<>();

	public static void replace(String process, String reference, Set<RoleAssignment> roles)
	{
		String key = process + "//" + reference;
		roleAssignmentsByProcessAndReference.put(key, roles);
	}

	public static void add(String process, String reference, RoleAssignment roleAssignment)
	{
		String key = process + "//" + reference;
		Set<RoleAssignment> roleAssignments = roleAssignmentsByProcessAndReference.computeIfAbsent(key, k -> new HashSet<>());
		roleAssignments.add(roleAssignment);
	}

	public static void remove(String userId, String caseId)
	{
		for (Set<RoleAssignment> roleAssignments : roleAssignmentsByProcessAndReference.values())
		{
			roleAssignments.removeIf(ra -> userId.equals(ra.getUserId()) && caseId.equals(ra.getCaseId()));
		}
	}

	public static Stream<RoleAssignment> findForCase(String caseId)
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
