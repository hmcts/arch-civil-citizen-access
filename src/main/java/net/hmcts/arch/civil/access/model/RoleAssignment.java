package net.hmcts.arch.civil.access.model;

import lombok.Value;

/*
 * A simple model of a case role: a user has a named role on a specific case.
 */
@Value
public class RoleAssignment
{
	String userId;
	String caseId;
	String roleName;
}
