#!/usr/bin/env bash

# Examines the diff of git commits and uploads the changes to the Chef Server.
# This script is currently used in Jenkins.
#
# Warning: The disadvantage of the diff-based approach is that failures during
# the upload will be noticed as a failed build, but not automatically picked
# up by the next script execution.
#
#
# Usage:
# $ upload-changes.sh [REFS]
# with REFS being either one or a series of git refs, e.g. HEAD (default) or
# HEAD~5..HEAD

set -euo pipefail

REFS="${1:=HEAD}"
GIT=$(which git)
PATTERN_ADDED="A"
PATTERN_MODIFIED="CMRTUXB"
PATTERN_DELETED="D"

changes() {
  TYPE=$1 # supplied to --diff-filter. = added / modified / deleted
  PATH=${2:=.} # directory, to narrow down

  if [ -z "$TYPE" ]; then
    echo "Parameter TYPE missing"
    exit 1
  fi

  $GIT diff --color=always --diff-filter=$TYPE --name-only $REFS -- $PATH
}

COL_DEFAULT="\e[0m"
COL_ADD="\e[92m"
COL_DEL="\e[93m"
COL_HEAD="\e[44m"
header() {
  echo -e $COL_HEAD
  echo
  echo "  $1"
  echo -e $COL_DEFAULT
} 

header "data_bags"
for item in $(changes "$PATTERN_ADDED$PATTERN_MODIFIED" "data_bags/"); do
  databag=$(basename $(dirname "$item"))
  filename=$(basename $item)
  db_item=${filename%.*}
  echo -e "${COL_ADD}Uploading data bag $databag/$db_item${COL_DEFAULT}"
  git diff $REFS -- $item
  knife data bag from file $databag $item
done

for item in $(changes "$PATTERN_DELETED" "data_bags/"); do
  databag=$(basename $(dirname "$item"))
  filename=$(basename $item)
  db_item=${filename%.*}
  echo -e "${COL_DEL}Deleting data bag $databag/$db_item${COL_DEFAULT}"
  knife data bag delete -y $databag $db_item
done


header "environments"
for item in $(changes "$PATTERN_ADDED$PATTERN_MODIFIED" "environments/"); do
  echo -e "${COL_ADD}Uploading environment from $item"
  git diff $REFS -- $item
  knife environment from file $item
done

for item in $(changes "$PATTERN_DELETED" "environments/"); do
  filename=$(basename $item)
  env_name=${filename%.*}
  echo -e "${COL_DEL}Deleting environment $env_name${COL_DEFAULT}"
  knife environment delete -y $env_name
done


header "roles"
for item in $(changes "$PATTERN_ADDED$PATTERN_MODIFIED" "roles/"); do
  echo -e "${COL_ADD}Uploading role from $item"
  git diff $REFS -- $item
  knife role from file $item
done

for item in $(changes "$PATTERN_DELETED" "roles/"); do
  filename=$(basename $item)
  role_name=${filename%.*}
  echo -e "${COL_DEL}Deleting role $role_name${COL_DEFAULT}"
  knife role delete -y $role_name
done

