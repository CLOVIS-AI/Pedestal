#!/usr/bin/env sh

################################################################################
#
#    Generates the review app URL for a given CI job.
#
#
#    Usage:
#      .gitlab/ci/review-url.sh ARTIFACT_PATH
#
################################################################################

# Yes, there are no predefined variables for this,
# so we use a ugly rewrite.
# https://gitlab.com/gitlab-org/gitlab/-/issues/450912

root=$(echo "$CI_PAGES_URL" | sed 's // XSTARTX ;s / /-/ ;s XSTARTX // ')

echo "$root/-/jobs/$CI_JOB_ID/artifacts/$1"
