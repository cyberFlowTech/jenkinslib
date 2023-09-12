package org.devops

// 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras

def BuildImageAndPush(option, env, imageAddr, serviceName, tag){

    if (env == "dev"){
        sh """
        docker build -t ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'` -f Dockerfile_dev .
        docker push ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'`
        docker rmi ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'`
        """
    }else{
        sh """
        docker build -t ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'` -f Dockerfile .
        docker push ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'`
        docker rmi ${imageAddr}/${serviceName}:`echo \${tag} | sed 's/\\//_/g'`
        """
    }
}



def Publish(option, env, imageAddr, servicename, projectname, tag, servicepath, hostname, jobname, arn) {
    command = """
        cd /home/RD.Center/eks/genDeploy && git pull
        /usr/local/go/bin/go run /home/RD.Center/eks/genDeploy/genDeploy.go aws-ecr-key ${imageAddr} ${env} `echo \${tag} | sed 's/\\//_/g'` 1 ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname}
        kubectl -n ${projectname}-${env} delete deployment `kubectl get deployment -n ${projectname}-${env} |grep ${servicename}-deployment|awk '{print \$1}'`
        cd /home/RD.Center/jenkins/${jobname}
        kubectl apply -f deployment.yaml
        kubectl apply -f service.yaml
    """
    sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
    execCommand: command, execTimeout: 22000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
    sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

}
