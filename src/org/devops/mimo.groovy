package org.devops

// 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras

def BuildImageAndPush(option, env, imageAddr, imageRepo, serviceName, tag){


    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{   

        if (env == "dev"){
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/test/
            """
        }else{
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/${env}/
            """
        }

        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`

        """

        if (env == "prod"){
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/DockerfileProd .
                docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
            """
        }else{
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/Dockerfile .
                docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
            """
        }
    }
}



def BuildTaskImageAndPush(option, env, imageAddr, imageRepo, serviceName, tag){


    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{  

        if (env == "dev"){
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/test/
            """
        }else{
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/${env}/
            """
        } 
        
        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`

        """

        if (env == "prod"){
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/task/DockerfileProd .
                docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
            """
        }else{
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/task/Dockerfile .
                docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
            """
        }
    }


}



def BuildImImageAndPush(option, env, imageAddr, imageRepo, serviceName, tag){
    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{  
        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
        docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/Dockerfile .
        docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
        """
    }
}

// 生产作废
def Publish(option, env, imageAddr, imageRepo, servicename, projectname, tag, servicepath, hostname, jobname, arn) {
    if ( option == 'Rollback' ){
        command = """
        /home/RD.Center/eks/genDeploy/genDeploy aws-ecr-key ${imageAddr}/${imageRepo} ${env} ${tag} ${replicas} ${hostname} ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname} ${servicepath} ${arn}
        cd ./jenkins/${jobname}
        kubectl apply -f deployment.yaml
        """
        sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
        execCommand: command, 
        execTimeout: 12000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
        sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
    }else if ( option == 'Expand' ){
        command = """
        kubectl -n ${projectname}-${env} scale deployment `kubectl get deployment -n ${projectname}-${env} |grep ${servicename}-deployment|awk '{print \$1}'` --replicas=${replicas}
        """
        sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
        execCommand: command, 
        execTimeout: 12000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
        sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
    }else{
        if ( env == "prod"){
            command = """
            # 生产环境每次tag都不同,直接apply
            /home/RD.Center/eks/genDeploy/genDeploy aws-ecr-key ${imageAddr}/${imageRepo} ${env} ${tag} ${replicas} ${hostname} ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname} ${servicepath} ${arn}
            cd ./jenkins/${jobname}
            kubectl apply -f deployment.yaml
            kubectl apply -f service.yaml
            # 重启日志服务
            sleep 1
            kubectl -n monitoring delete daemonset loki-promtail && cd /home/RD.Center/eks/promtail && kubectl apply -f promtail-daemonset.yaml
        """
        }else{
            command = """
            /home/RD.Center/eks/genDeploy/genDeploy aws-ecr-key ${imageAddr}/${imageRepo} ${env} ${tag} ${replicas} ${hostname} ${projectname} ${servicename} /home/RD.Center/jenkins/${jobname} ${servicepath} ${arn}
            kubectl -n ${projectname}-${env} delete deployment `kubectl get deployment -n ${projectname}-${env} |grep ${servicename}-deployment|awk '{print \$1}'`
            cd ./jenkins/${jobname}
            kubectl apply -f deployment.yaml
            kubectl apply -f service.yaml
            # 重启日志服务
            sleep 1
            kubectl -n monitoring delete daemonset loki-promtail && cd /home/RD.Center/eks/promtail && kubectl apply -f promtail-daemonset.yaml
        """
        }
        sshPublisher(publishers: [sshPublisherDesc(configName: 'AwsEksJumpServer', transfers: [sshTransfer(cleanRemote: false, excludes: '', 
        execCommand: command, execTimeout: 12000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/jenkins/${env.JOB_NAME}', remoteDirectorySDF: false, removePrefix: '', 
        sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
    }
}

def Sonarqube(Version,url){
    sh """
        ssh root@172.16.13.208 /root/script/sonar.sh ${JOB_NAME}  ${Version} ${url}
       """
}

// 以下为生产专用

def BuildMimoImage(option, env, imageAddr, imageRepo, serviceName, tag){


    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{   

        if (env == "dev"){
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/test/
            """
        }else{
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/${env}/
            """
        }

        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`

        """

        if (env == "prod"){
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/DockerfileProd .
            """
        }else{
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/Dockerfile .
            """
        }
    }
}

def BuildTaskImage(option, env, imageAddr, imageRepo, serviceName, tag){


    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{  

        if (env == "dev"){
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/test/
            """
        }else{
            sh """
            cp /home/jenkins/dbConfig/mimo/${env}/* ./config/${env}/
            """
        } 
        
        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`

        """

        if (env == "prod"){
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/task/DockerfileProd .
            """
        }else{
            sh """
                docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/task/Dockerfile .
            """
        }
    }


}


def BuildImImage(option, env, imageAddr, imageRepo, serviceName, tag){
    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{  
        sh """
        # relogin
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`
        docker build -t ${imageAddr}/${imageRepo}:${serviceName}_${tag} -f deploy/docker/Dockerfile .
        """
    }
}

def PushImageToEcr(option, env, imageAddr, imageRepo, serviceName, tag){
    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{  
        sh """
        # relogin
        docker push ${imageAddr}/${imageRepo}:${serviceName}_${tag}
        """
    }
}

// seoul

def BuildImage(option, env, imageAddr, serviceName, tag){
    sh """
    cp /home/jenkins/dbConfig/mimo/seoul_test/* ./config/test/
    # relogin
    docker logout
    docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southnorth-1`
    docker build -t ${imageAddr}/${serviceName}:${tag} -f deploy/docker/Dockerfile .
    """
}

def Push(option, env, imageAddr, serviceName, tag){

    sh """
    # relogin
    docker push ${imageAddr}/${serviceName}:${tag}
    """

}
