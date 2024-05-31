package net.hmcts.arch.civil.access.model;

import lombok.Value;

@Value
public class RoleAssignment
{
	String userId;
	String caseId;
	String roleName;
}
