properties([pipelineTriggers([githubPush()])])
node {
    jenkinsGitCredentials = 'jenkinsGitHubSvc'
    git url: 'https://github.com/Demonslyr/docker-steamcmd.git', branch: 'atriarch', credentialsId: jenkinsGitCredentials
    stage('setup') {
        checkout scm
        currentBuild.description = "${Branch}"
        appName = "${ImageName}"
        dockerRepo = "${DockerRepo}"
        dockerCredId = "${DockerCredentials}"
        dockerfilePathFromRoot = "${DockerFilePathAndFilename}" // this is the path from the base directory
        fullImageName = "${dockerRepo}/${appName}:v1.0.${BUILD_NUMBER}"
    }
    stage('build') {
        withCredentials([usernamePassword(usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS', credentialsId: dockerCredId)]) {
            loginout = sh(returnStdout: true, script: "echo ${DOCKER_PASS} | docker login ${dockerRepo} --username ${DOCKER_USER} --password-stdin")
            println loginout
            buildout = sh(returnStdout: true, script: "docker build -t ${appName} -f ${dockerfilePathFromRoot} .")
            println buildout
        }
    }
    stage('push') {
        tagout = sh(returnStdout: true, script: "docker tag ${appName} ${fullImageName}")
        println tagout
        withCredentials([usernamePassword(usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS', credentialsId: dockerCredId)]) {
            loginout = sh(returnStdout: true, script: "echo ${DOCKER_PASS} | docker login ${dockerRepo} --username ${DOCKER_USER} --password-stdin")
            println loginout
            pushout = sh(returnStdout: true, script: "docker push ${fullImageName}")
            println pushout
        }
    }
}
