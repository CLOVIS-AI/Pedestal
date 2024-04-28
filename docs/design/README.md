# Design decisions

When we create software, we often have to decide between multiple ways to realize our vision. When maintaining a project, we may think of a "better" way to do things. At this point, we must be able to quickly decide whether:
- The new idea was one of the considered options but was discarded for some reason,
- The new idea was not considered, but some constraint makes it an incorrect solution,
- The new idea was not considered, and it is indeed better than the implemented solution.

Deciding between these situations can be very hard when no context is available, especially in teams which make decisions during meetings, which leave no trail. There are multiple ways to increase available context for future decisions: writing more comprehensive documentation comments, adding details in the Git history, and writing detailed documents describing the decisions.

This directory implements the latter pattern, also called Architecture Decision Records ([learn more](https://adr.github.io/)). Each document describes a single decision and considered alternatives.

[TOC]

## Which decisions should be recorded?

Whenever there is a chance that multiple solutions fit a problem, or when the plan has to change because the originally-imagined solution turned out to be unfitting. As a rule of thumb, if a reviewer asks you why you did something one way instead of another, it's probably worth recording this decision.

However, not all decisions are worth adding to this directory:
- If a decision is specifically localized to one file (e.g. to one function, to one class), a regular comment at that place is more useful because it is better co-located.
- If a decision is a one-time thing and has no consequences for the future, describing it in the commit description (or merge request description) is more efficient. For example, putting in place a new workflow, which has its own documentation file in the repository, doesn't require a record, as it would otherwise mostly be a duplicate of the chosen workflow. 

These kinds of decisions likely require a new record:
- Deciding between two alternative ways to do things that are very similar.
- Decisions that affect more than one file (e.g. an entire package, an entire module, the entire project…).
- Decisions that have consequences on the future (e.g. future maintainers should continue following them to keep coherence).

## Creating a new record

Here is a template for new records:
```markdown
# <Title>

|               |          |
|---------------|----------|
| Status        | <Status> |
| Discussion    | <Link>   |
| Superseded by | <link·s> |
| Relates to    | <Link·s> |

<Short description>

[TOC]

## The problem

## Constraints

## Considered solutions

## Selected solution
```

Useful external links: [How to write ADRs](https://www.ozimmer.ch/practices/2023/04/03/ADRCreation.html), [How to review ADRs](https://www.ozimmer.ch/practices/2023/04/05/ADRReview.html).

The following describes all the fields of the template and what they should be filled in with.

**Status:**

[![Status: draft](https://badgen.net/static/Status/draft/purple)](https://gitlab.com/opensavvy/playgrounds/baseline/-/blob/main/docs/design/README.md#creating-a-new-record) • This decision is a draft. It should not yet be followed. The field "Discussion" should be present.

[![Status: active](https://badgen.net/static/Status/active/purple)](https://gitlab.com/opensavvy/playgrounds/baseline/-/blob/main/docs/design/README.md#creating-a-new-record) • This decision is active and should be followed in all new code. When editing existing code which doesn't follow this decision, consider rewriting it so it does.

[![Status: archived](https://badgen.net/static/Status/archived/purple)](https://gitlab.com/opensavvy/playgrounds/baseline/-/blob/main/docs/design/README.md#creating-a-new-record) • This decision was active, but isn't anymore. New code isn't required to follow it. Existing code may or may not still follow it, and no particular migration efforts are required.

[![Status: superseded](https://badgen.net/static/Status/superseded/purple)](https://gitlab.com/opensavvy/playgrounds/baseline/-/blob/main/docs/design/README.md#creating-a-new-record) • This decision was active, but was superseded by another decision. New code should follow the new decision. Existing code, when possible, should be migrated to follow the new decision. The field "Superseded by" should be present.

**Discussion:**
When in "draft" status, links to an issue, a merge request, or some other type of document that allows comments. Any questions from the discussion that isn't answered by the record should be added to the record. When a record graduates and becomes active, the discussion link may remain for archival purposes (an active record is not subject to discussion, except when creating a new superseding record). 

**Superseded by:**
Links to one or more record which overturn the current record. Only presents when the status is "superseded".

**Related to:**
Links to other records which have a related subject.

**The problem.**
Exhaustive and precise description of the problem·s to solve.

**Constraints.**
Additional constraints that are not intrinsic to the problem but may still impact decision-making. For example, a constraint on development time.

**Considered solutions.**
Each considered solution should have its own section, containing:
- A description of what it would entail,
- A list of advantages compared to other solutions,
- A list of disadvantages compared to other solutions.

The advantages and disadvantages may be subjective, as long as they are appropriate in the context of the problem. For example, "no one in the team has used this technology" is an appropriate disadvantage to using it, even if it seems completely appropriate otherwise.

The selected solution should be included in this section, with the same content as all other alternatives.

**Selected solution.**
Describes the selected solution in more details, and explains why it was selected (especially if other solutions have advantages that this one doesn't).

## How are decision records modified?

Unless to edit their status, records should almost never be edited.

## What happens when a decision is overturned?

If a decision is overturned, a new record should be created with the new decision, linking to the previous one. The previous one should be marked as Superseded.

## When are decision records deleted?

Decision records may only be deleted when **no content from to the repository whatsoever** uses them anymore, and **no future content should follow them**. At this point, and only at this point, are they truly useless, and thus worthy of deletion.

As long as a single piece of code, static resources or anything else committed to the repository follows a design decision, it should remain in the repository as well.
