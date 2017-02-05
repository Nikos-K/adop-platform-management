// Constants
def workspaceManagementFolderName= "/Workspace_Management"
def workspaceManagementFolder = folder(workspaceManagementFolderName) { displayName('Workspace Management') }

// Jobs
def generateWorkspaceJob = freeStyleJob(workspaceManagementFolderName + "/Generate_Workspace")
 
// Setup generateWorkspaceJob
generateWorkspaceJob.with{
    parameters{
        stringParam("WORKSPACE_NAME","","The name of the project to be generated.")
    }
    wrappers {
        preBuildCleanup()
        injectPasswords()
        maskPasswords()
    }
    steps {
        shell('''#!/bin/bash

# Validate Variables
pattern=" |'"
if [[ "${WORKSPACE_NAME}" =~ ${pattern} ]]; then
    echo "WORKSPACE_NAME contains a space, please replace with an underscore - exiting..."
    exit 1
fi''')
        dsl {
            external("workspaces/jobs/**/*.groovy")
        }
    }
    scm {
        git {
            remote {
                name("origin")
                url("ssh://jenkins@${ADOP_GERRIT_HOST}:29418/platform-management")
                credentials("adop-jenkins-master")
            }
            branch("*/master")
        }
    }
} 
