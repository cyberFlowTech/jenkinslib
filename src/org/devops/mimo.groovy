package org.devops

// py-mimo
def BuildPyMimoImageAndPush(option, env, imageAddr, serviceName, tag){


    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{   
        sh """
        cp -rf /home/jenkins/dbConfig/env ./
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
        docker build -t ${imageAddr}/${serviceName}:${tag} -f Dockerfile .
        docker push ${imageAddr}/${serviceName}:${tag}
        docker rmi ${imageAddr}/${serviceName}:${tag}
        """

    }
}

// py-mimo发布
def PublishPyMimo(option, env, imageAddr, servicename, projectname, tag, servicepath, hostname, jobname, arn) {
    command = """
        #cd /home/RD.Center/eks/genDeploy && git pull
        #/usr/local/go/bin/go run /home/RD.Center/eks/genDeploy/genDeploy.go aws-ecr-key ${imageAddr} ${env} ${tag} 1 ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname}
        kubectl -n ${projectname}-${env} delete deployment `kubectl get deployment -n ${projectname}-${env} |grep ${servicename}-deployment|awk '{print \$1}'`
        cd /home/RD.Center/jenkins/${jobname}
        kubectl apply -f deployment.yaml
        kubectl apply -f service.yaml
    """
    sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
    execCommand: command, execTimeout: 22000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
    sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
}