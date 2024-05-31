# Citizen role assignment update for Civil claims and general applications

**This is not production code, just a simulation to demonstrate potential access management logic.**

Quick start: run the `net.hmcts.arch.civil.access.Test.main()`method.  This is a Maven project, not Gradle.

This is a very quick simulation of the structure of logic which Civil can use to maintain citizen access correctly to claim and general application cases, including considerations of with / without notice.

## Model

**ClaimCase:** a simple model of a claim - contains a case ID and defines 4 roles (2 claimants, 2 defendants).

**GeneralApplicationCase:** a simple model of a general application - also defines 4 roles (different from the roles in the claim case type), contains a case ID, the ID for its claim case, a flag indicating whether the application is with or without notice, and a mapping of the roles in the claim to the roles in the application (e.g. the application was made by the defendants of the claim, so the general application role `ga-applicant-01` is filled by the same user(s) who has the role `c-defendant-01` on the claim).

Note that the names of the roles used may not be realistic, and are prefixed `c-` and `ga-` just for clarity of which case type each role belongs to.  The key points are that each general application has data relating the roles in the general application to the roles in the main claim, and this mapping may be different depending on the type of the general application and how it was created.

**User:** simple model of a user - just an ID.

**RoleAssignment:** simple model of a *case* role assignment - just a user ID, case ID and role name.

## Services

**CCD:** Repository of cases, and methods to retrieve and search.

**RAS:** Repository of role assignments, and methods to retrieve and search.

**Civil:** Logic for managing claims and general applications.

## Role Assignment Logic

### Civil.refreshGeneralApplicationCitizenAccess

This is the key logic.  Given a general application case:

1. Find all the citizen role assignments for the *claim* case.  i.e. all the citizen users with some sort of access to the case.

2. Get a list of roles which should exist on the GA case.  This calls the GA case method `getRolesToGiveAccess`, which takes account of whether the case is without notice (returning just applicant roles) or with notice (returning both applicant and respondent roles).

3. Use the role mappings held in the GA case to determine which users should have which roles on the GA, and generate a role assignment for each (e.g. `user01` has role `c-claimant-01` on the claim, `c-claimant-01` maps to the role `ga-respondent-01` in the GA case, the GA case is *with* notice: conclusion is that `user01` should have role `ga-respondent-01` in the GA case).

4. Having established all of the citizen role assignments which should exist for the GA case, transactionally update them in the RAS using the "replace existing by process and reference" functionality.  This allows transactional replacement of a group of role assignments which are grouped together by the combination of:
   
   - `process`: a unique identifier for the business process responsible for managing this group of role assignments.  `civil-citizen-access` is used in this example.
   
   - `reference`: a unique identifier established by the owning business process for this specific group of role assignments.  Since this logic always updates all the citizen role assignments for a GA case at once, the GA case ID is an appropriate reference to use here.
     
     By updating the RAS using `process = civil-citizen-access`, `reference = <ga-case-id>`, the logic is saying "replace all the existing role assignments created by the `civil-citizen-access` process and having reference `<ga-case-id>` with this new set."

### Civil.refreshGeneralApplicationCitizenAccessForClaim

This method simply finds all the general applications for a given claim, and invokes the `refreshGeneralApplicationCitizenAccess` method on each in turn.

It is used when any change at the claim level may impact the set of citizen role assignments which should be present for one or more of the claim's general applications.

For example, if a new user is given a citizen role in the claim, then they should inherit any corresponding roles in the claim's GA cases, and if a user is removed from the claim they should lose their roles in the GA cases.

### Triggering refreshes

The refresh methods above can be called at any time, and will make citizen access to the GA cases for a claim correct according to the current roles held by citizens on the claim and the current data (role mappings and with/without notice flags) in the GA cases.  It is necessary to invoke one of these methods whenever something changes which affects citizen access to the claim or its GAs.  In this example, this include:

1. Any time a user is added to, or removed from, the main claim case (refresh all GA cases).

2. Any time the with / without notice status of a GA case is changed (refresh just that GA case).

## Test scenario

The `Test` class is a simple Java application (with a `main` method, not using a test framework) which performs a series of operations on a claim case and its general applications, separately tracking the expected changes to user roles and comparing the expected access with the actual access after each operation.
