# Gerrit
# Check if permission repo name has been passed, if not, defaults to permissions
unset permissions_name
unset permissions_config_name
while getopts ":r:" opt; do
  case $opt in
    r)  permissions_name="$OPTARG"
        permissions_config_name="-$permissions_name"
    ;;  
  esac
done
if [ -z "$permissions_name" ]; then
    permissions_name="permissions"
fi

permissions_repo_name="${WORKSPACE_NAME}/${PROJECT_NAME}/${permissions_name}"

## Create Project Repo
mkdir ${WORKSPACE}/tmp
cd ${WORKSPACE}/tmp

# Check if the permissions repository already exists or not
permissions_repo_exists=0
list_of_repos=$(ssh -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit ls-projects --type permissions)

for repo in ${list_of_repos}
do
    if [ ${repo} = ${permissions_repo_name} ]; then
        echo "Found: ${repo}"
        permissions_repo_exists=1
        break
    fi
done

# If not, create it
if [ ${permissions_repo_exists} -eq 0 ]; then
    ssh -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit create-project --parent "All-Projects" --permissions-only "${permissions_repo_name}"
else
    echo "Repository already exists: ${permissions_repo_name}"
fi

## Setup Access Control
git clone ssh://jenkins@gerrit:29418/"${permissions_repo_name}"
cd ${permissions_name}/
git fetch origin refs/meta/config:refs/remotes/origin/meta/config
git checkout meta/config
perl -p -i -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg' < "${WORKSPACE}/projects/gerrit/project${permissions_config_name}.config" 2> /dev/null 1> "./project.config"
perl -p -i -e 's/\$\{([^}]+)\}/defined $ENV{$1} ? $ENV{$1} : $&/eg' < "${WORKSPACE}/projects/gerrit/groups" 2> /dev/null 1> "./groups"
if [ $(git status --porcelain | wc -l) -gt 0 ]; then
    git add project.config groups
    git commit -m "Generating parent project permissions"
    git push origin meta/config:meta/config
else
    echo "Nothing to commit"
fi

# Remove the temp file directory
rm -rf ${WORKSPACE}/tmp