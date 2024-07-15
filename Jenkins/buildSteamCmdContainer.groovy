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
        dockerFilePrefix = "${DockerFilePrefix}" // this is the path from the base directory
    }
    
    stage('build and push') {
        dockerfiles = sh(returnStdout: true, script: "ls ${dockerFilePrefix}*").trim().split('\n')

        for (dockerfile in dockerfiles) {
            def dockerfileName = dockerfile.split('/')[-1]
            def tagName = dockerfileName.replace("${dockerFilePrefix}", '')
            def fullImageName = "${dockerRepo}/${appName}:${tagName}"

            withCredentials([usernamePassword(usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS', credentialsId: dockerCredId)]) {
                def loginout = sh(returnStdout: true, script: "echo ${DOCKER_PASS} | docker login ${dockerRepo} --username ${DOCKER_USER} --password-stdin")
                println loginout

                def buildout = sh(returnStdout: true, script: "docker build -t ${appName} -f ${dockerfile} .")
                println buildout

                def tagout = sh(returnStdout: true, script: "docker tag ${appName} ${fullImageName}")
                println tagout

                def pushout = sh(returnStdout: true, script: "docker push ${fullImageName}")
                println pushout
            }
        }
    }
}
