// Constants
def platformToolsGitURL = "ssh://jenkins@${ADOP_GERRIT_HOST}:29418/platform-management"

// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def workspaceFolder = folder(workspaceFolderName)

def projectManagementFolderName= workspaceFolderName + "/Project_Management"
def projectManagementFolder = folder(projectManagementFolderName) { displayName('Project Management') }

// Jobs
def generateProjectJob = freeStyleJob(projectManagementFolderName + "/Generate_Project")

// Setup Generate_Project
generateProjectJob.with{
    parameters{
        stringParam("PROJECT_NAME","","The name of the project to be generated.")
    }
    environmentVariables {
        env('WORKSPACE_NAME',workspaceFolderName)
    }
    wrappers {
        preBuildCleanup()
        injectPasswords()
        maskPasswords()
        sshAgent("adop-jenkins-master")
    }
    steps {
        shell('''#!/bin/bash -e

# Validate Variables
pattern=" |'"
if [[ "${PROJECT_NAME}" =~ ${pattern} ]]; then
	echo "PROJECT_NAME contains a space, please replace with an underscore - exiting..."
	exit 1
fi''')
        dsl {
            external("projects/jobs/**/*.groovy")
        }
    }
    scm {
        git {
            remote {
                name("origin")
                url("${platformToolsGitURL}")
                credentials("adop-jenkins-master")
            }
            branch("*/master")
        }
    }
}
