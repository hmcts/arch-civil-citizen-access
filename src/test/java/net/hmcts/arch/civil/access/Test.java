package net.hmcts.arch.civil.access;

import static net.hmcts.arch.civil.access.model.GeneralApplicationCase.*;
import static net.hmcts.arch.civil.access.model.ClaimCase.*;


import lombok.Value;
import net.hmcts.arch.civil.access.model.ClaimCase;
import net.hmcts.arch.civil.access.model.GeneralApplicationCase;
import net.hmcts.arch.civil.access.model.User;
import net.hmcts.arch.civil.access.service.Civil;
import net.hmcts.arch.civil.access.service.RAS;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class Test
{

	public static void main(String[] args)
	{
		// The expected access (modified below after each change to the roles and cases)
		Access expected = new Access();

		// A set of users to play with
		User user01 = new User("user01");
		User user02 = new User("user02");
		User user03 = new User("user03");
		User user04 = new User("user04");
		User user05 = new User("user05");
		User user06 = new User("user06");
		User user07 = new User("user07");
		// A claim case
		ClaimCase claimCase01 = Civil.createClaim("claim01");
		//=======================================================================================================
		// Give some users roles on the case
		Civil.addUserToClaim(user01.getId(), claimCase01.getId(), C_CLAIMANT_1_ROLE);
		Civil.addUserToClaim(user02.getId(), claimCase01.getId(), C_CLAIMANT_2_ROLE);
		Civil.addUserToClaim(user03.getId(), claimCase01.getId(), C_DEFENDANT_1_ROLE);
		Civil.addUserToClaim(user04.getId(), claimCase01.getId(), C_DEFENDANT_2_ROLE);
		Civil.addUserToClaim(user05.getId(), claimCase01.getId(), C_CLAIMANT_1_ROLE);
		// How does this change the expected access?
		expected.add(user01.getId(), claimCase01.getId(), C_CLAIMANT_1_ROLE);
		expected.add(user02.getId(), claimCase01.getId(), C_CLAIMANT_2_ROLE);
		expected.add(user03.getId(), claimCase01.getId(), C_DEFENDANT_1_ROLE);
		expected.add(user04.getId(), claimCase01.getId(), C_DEFENDANT_2_ROLE);
		expected.add(user05.getId(), claimCase01.getId(), C_CLAIMANT_1_ROLE);
		// Check that access is correct
		checkAccess(expected);
		// A set of general applications
		//=======================================================================================================
		//   with notice, all roles mapped
		GeneralApplicationCase generalApplicationCase01 = Civil.createGeneralApplication(
				"ga01", "claim01", true,
				GA_APPLICANT_1_ROLE, C_CLAIMANT_1_ROLE, GA_APPLICANT_2_ROLE, C_CLAIMANT_2_ROLE,
				GA_RESPONDENT_1_ROLE, C_DEFENDANT_1_ROLE, GA_RESPONDENT_2_ROLE, C_DEFENDANT_2_ROLE);
		// How does this change the expected access?
		expected.add(user01.getId(), generalApplicationCase01.getId(), GA_APPLICANT_1_ROLE);
		expected.add(user02.getId(), generalApplicationCase01.getId(), GA_APPLICANT_2_ROLE);
		expected.add(user03.getId(), generalApplicationCase01.getId(), GA_RESPONDENT_1_ROLE);
		expected.add(user04.getId(), generalApplicationCase01.getId(), GA_RESPONDENT_2_ROLE);
		expected.add(user05.getId(), generalApplicationCase01.getId(), GA_APPLICANT_1_ROLE);
		// Check that access is correct
		checkAccess(expected);
		//=======================================================================================================
		//   without notice, all roles mapped
		GeneralApplicationCase generalApplicationCase02 = Civil.createGeneralApplication(
				"ga02", "claim01", false,
				GA_APPLICANT_1_ROLE, C_CLAIMANT_1_ROLE, GA_APPLICANT_2_ROLE, C_CLAIMANT_2_ROLE,
				GA_RESPONDENT_1_ROLE, C_DEFENDANT_1_ROLE, GA_RESPONDENT_2_ROLE, C_DEFENDANT_2_ROLE);
		// How does this change the expected access?
		expected.add(user01.getId(), generalApplicationCase02.getId(), GA_APPLICANT_1_ROLE);
		expected.add(user02.getId(), generalApplicationCase02.getId(), GA_APPLICANT_2_ROLE);
		expected.add(user05.getId(), generalApplicationCase02.getId(), GA_APPLICANT_1_ROLE);
		// Check that access is correct
		checkAccess(expected);
		//=======================================================================================================
		//   change notice for GA 02
		Civil.setWithNotice(generalApplicationCase02.getId(),true);
		// How does this change the expected access?
		expected.add(user03.getId(), generalApplicationCase02.getId(), GA_RESPONDENT_1_ROLE);
		expected.add(user04.getId(), generalApplicationCase02.getId(), GA_RESPONDENT_2_ROLE);
		// Check that access is correct
		checkAccess(expected);
		//=======================================================================================================
		//   add some users to the claim
		Civil.addUserToClaim(user06.getId(), claimCase01.getId(), C_CLAIMANT_2_ROLE);
		Civil.addUserToClaim(user07.getId(), claimCase01.getId(), C_DEFENDANT_1_ROLE);
		// How does this change the expected access?
		expected.add(user06.getId(), claimCase01.getId(), C_CLAIMANT_2_ROLE);
		expected.add(user06.getId(), generalApplicationCase01.getId(), GA_APPLICANT_2_ROLE);
		expected.add(user06.getId(), generalApplicationCase02.getId(), GA_APPLICANT_2_ROLE);
		expected.add(user07.getId(), claimCase01.getId(), C_DEFENDANT_1_ROLE);
		expected.add(user07.getId(), generalApplicationCase01.getId(), GA_RESPONDENT_1_ROLE);
		expected.add(user07.getId(), generalApplicationCase02.getId(), GA_RESPONDENT_1_ROLE);
		// Check that access is correct
		checkAccess(expected);
		//=======================================================================================================
		//   remove some users from the claim
		Civil.removeUserFromClaim(user01.getId(), claimCase01.getId());
		Civil.removeUserFromClaim(user03.getId(), claimCase01.getId());
		// How does this change the expected access?
		expected.remove(user01.getId(), claimCase01.getId());
		expected.remove(user01.getId(), generalApplicationCase01.getId());
		expected.remove(user01.getId(), generalApplicationCase02.getId());
		expected.remove(user03.getId(), claimCase01.getId());
		expected.remove(user03.getId(), generalApplicationCase01.getId());
		expected.remove(user03.getId(), generalApplicationCase02.getId());
		// Check that access is correct
		checkAccess(expected);
	}

	public static void checkAccess(Access expected)
	{
		Access actual = getCurrentAccess();
		System.out.println("Checking access.  Actual access : ");
		showHeader(System.out);
		show(actual, System.out);
		if (!actual.equals(expected))
		{
			System.err.println("Expected access:");
			show(expected, System.err);
			System.err.println("Actual access:");
			show(actual, System.err);
			throw new RuntimeException("Access not as expected.");
		}
	}

	public static Access getCurrentAccess()
	{
		Access access = new Access();
		RAS.stream().forEach(ra -> access.add(ra.getUserId(), ra.getCaseId(), ra.getRoleName()));
		return access;
	}

	/*
	 * A collection of entries showing which users have which roles on which cases.
	 * Provided to allow separate collections to be built for expected and actual access, and compared.
	 */
	@Value
	public static class Access
	{
		Set<Entry> entries = new HashSet<>();

		public Access add(String userId, String caseId, String roleName)
		{
			entries.add(new Entry(userId, caseId, roleName));
			return this;
		}

		public Access remove(String userId, String caseId)
		{
			entries.removeIf(e -> userId.equals(e.getUserId()) && caseId.equals(e.getCaseId()));
			return this;
		}

		@Value
		public static class Entry implements Comparable<Entry>
		{
			String userId;
			String caseId;
			String roleName;

			@Override
			public int compareTo(@NotNull Entry that)
			{
				int result = caseId.compareTo(that.caseId);
				if (result == 0) result = userId.compareTo(that.userId);
				if (result == 0) result = roleName.compareTo(that.roleName);
				return result;
			}
		}
	}

	public static void showHeader(PrintStream printStream)
	{
		printStream.println(pad("Case", 10) + " " + pad("User", 10) + " " + pad("Role Name", 18));
		printStream.println(pad("", 10, '=') + " " + pad("", 10, '=') + " " + pad("", 18, '='));
	}

	public static void show(Access access, PrintStream printStream)
	{
		access.getEntries().stream().sorted().forEach(e -> printStream.println(pad(e.getCaseId(), 10) + " " + pad(e.getUserId(), 10) + " " + pad(e.getRoleName(),18)));
	}

	public static String pad(String text, int length)
	{
		return pad(text, length, ' ');
	}

	public static String pad(String text, int length, char pad)
	{
		while (text.length() < length) text = text + pad;
		return text;
	}
}
