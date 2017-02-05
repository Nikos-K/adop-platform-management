// Constants
def platformManagementFolderName= "/Platform_Management"
def platformManagementFolder = folder(platformManagementFolderName) { displayName('Platform Management') }

// Jobs
def generateExampleWorkspaceJob = workflowJob(platformManagementFolderName + "/Generate_Example_Workspace")
 
generateExampleWorkspaceJob.with{
    parameters{
        stringParam("projectName","ExampleProject","")
        stringParam("workspaceName","ExampleWorkspace","")
        stringParam("cartridgeURL","ssh://jenkins@${ADOP_GERRIT_HOST}:29418/cartridges/adop-cartridge-java.git","")
    }
    properties {
        rebuild {
            autoRebuild(false)
            rebuildDisabled(false)
        }
    }
    definition {
        cps {
            script('''// Setup Workspace
build job: 'Workspace_Management/Generate_Workspace', parameters: [[$class: 'StringParameterValue', name: 'WORKSPACE_NAME', value: "${workspaceName}"]]

// Setup Faculty
build job: "${workspaceName}/Project_Management/Generate_Project", parameters: [[$class: 'StringParameterValue', name: 'PROJECT_NAME', value: "${projectName}"]]
retry(5) {
    build job: "${workspaceName}/${projectName}/Cartridge_Management/Load_Cartridge", parameters: [[$class: 'StringParameterValue', name: 'CARTRIDGE_CLONE_URL', value: "${cartridgeURL}"]]
}''')
sandbox()
        }
    }
} 
