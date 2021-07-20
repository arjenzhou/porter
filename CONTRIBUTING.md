# Contributing to Porter

Porter is released under the MIT license and follows a very standard Github development process, using Github tracker
for issues and merging pull requests into master. Contributions of all form to this repository is acceptable, as long as
it follows the prescribed community guidelines enumerated below.

## Reporting issue

Please follow the [ISSUE_TEMPLATE](ISSUE_TEMPLATE.md) for reporting any issues.

## Code Conventions

Our code style is almost in line with the standard java conventions (Popular IDE's default setting satisfy this), with
the following additional restricts:

If there are more than 120 characters in the current line, begin a new line.

Make sure all new .java files to have a simple Javadoc class comment with at least a @date tag identifying birth, and
preferably at least a paragraph on the intended purpose of the class.

Sufficient unit-tests should accompany new feature development or non-trivial bug fixes.

If no-one else is using your branch, please rebase it against the current master (or another target branch in the main
project).

When writing a commit message, please follow the following conventions: should your commit address an open issue, please
add Fixes #XXX at the end of the commit message (where XXX is the issue number).

## Contribution flow

A rough outline of an ideal contributors' workflow is as follows:

- Fork the current repository
- Create a topic branch from where to base the contribution. Mostly, it's the master branch.
- Make commits of logical units.
- Make sure the commit messages are in the proper format (see below).
- Push changes in a topic branch to your forked repository.
- Follow the checklist in the pull request template
- Before sending out the pull request, please sync your forked repository with the remote repository to ensure that your
  PR is elegant, concise. Reference the guide below:

```git
git remote add upstream git@github.com:arjenzhou/porter.git
git fetch upstream
git rebase upstream/master
git checkout -b your_awesome_patch
... add some work
git push origin your_awesome_patch
```

- Submit a pull request to arjenzhou/porter and wait for the reply.
- Thanks for contributing!

