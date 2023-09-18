package org.devops

// 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras

def BuildImageAndPush(option, env, imageAddr, serviceName, tag){

    if (env == "dev"){
        sh """
        Branch=`echo \$Tag | sed 's/\\//_/g'`
        docker build -t ${imageAddr}/${serviceName}:\$Branch -f Dockerfile_dev .
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
        docker push ${imageAddr}/${serviceName}:\$Branch
        docker rmi ${imageAddr}/${serviceName}:\$Branch
        """
    }else{
        sh """
        docker build -t ${imageAddr}/${serviceName}:\$Branch -f Dockerfile .
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
        docker push ${imageAddr}/${serviceName}:\$Branch
        docker rmi ${imageAddr}/${serviceName}:\$Branch
        """
    }
}

def BuildAPIImageAndPush(option, env, imageAddr, serviceName, tag){

    sh """
    Branch=`echo \$Tag | sed 's/\\//_/g'`
    docker build -t ${imageAddr}/${serviceName}:\$Branch -f ./Deploy/Dockerfile .
    docker logout
    docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
    docker push ${imageAddr}/${serviceName}:\$Branch
    docker rmi ${imageAddr}/${serviceName}:\$Branch
    """
}

def BuildTaskImageAndPush(option, env, imageAddr, serviceName, tag){

    sh """
    Branch=`echo \$Tag | sed 's/\\//_/g'`
    rm -rf ./main.go
    cp -rf ./Job/task ./main.go
    docker build -t ${imageAddr}/${serviceName}:\$Branch -f ./Deploy/Dockerfile .
    docker logout
    docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
    docker push ${imageAddr}/${serviceName}:\$Branch
    docker rmi ${imageAddr}/${serviceName}:\$Branch
    """
}



def Publish(option, env, imageAddr, servicename, projectname, tag, jobname) {
    command = """
        cd /home/RD.Center/eks/genDeploy && git pull
        Branch=`echo \$Tag | sed 's/\\//_/g'`
        /usr/local/go/bin/go run /home/RD.Center/eks/genDeploy/genDeploy.go aws-ecr-key ${imageAddr} ${env} \$Branch 1 ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname}
        kubectl -n ${projectname}-${env} delete deployment `kubectl get deployment -n ${projectname}-${env} |grep ${servicename}-deployment|awk '{print \$1}'`
        cd /home/RD.Center/jenkins/${jobname}
        kubectl apply -f deployment.yaml
        kubectl apply -f service.yaml
    """
    sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
    execCommand: command, execTimeout: 22000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
    sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

}
