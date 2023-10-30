package org.devops

def PrintMsg(msg){
  println(msg)
}

// https://api.telegram.org/bot6230860729:AAE9Uj6JJYOap40Ie35UR-STW7ZKEYslNoI/sendMessage
// -919670551

// def ReqApprovalByUdpNotifyServer(admin,telegramId,envi,api) {    
//     sh """
//         #!/bin/sh -e\\\\\\n curl -k --location --request POST '${api}' \
//         --header 'Content-Type: application/json' \
//         --data '{
//         "env": "${envi}",
//         "admin": "${admin}",
//         "telegramId": "${telegramId}",
//         "builder": "${env.BUILD_USER}",
//         "jobName": "${env.JOB_NAME}",
//         "tag": "${env.tag}",
//         "token": "${env.randomToken}",
//         "approvalUrl": "${env.BUILD_URL}input/",
//         "diff": "${env.BUILD_URL}last-changes/",
//         "log": "${env.BUILD_URL}console",
//         "comment": "${env.comment}",
//         "imageName":"024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras:${env.servicename}_${env.tag}",
//         "serviceName":"${env.servicename}",
//         "projectName":"${env.projectname}"}'
//     """
// }

def ReqApprovalByUdpNotifyServer(admin,telegramId,envi,api) {    
    sh """
echo "
#!/bin/bash
MESSAGE='{\\"api\\":\\"${api}\\",\\"time\\":1691397277,\\"data\\":[\\"
### ${envi}环境发布,请审批 ###\\\\\\n
- 申请人: ${env.BUILD_USER}\\\\\\n
- 构建名称: ${env.JOB_NAME}\\\\\\n
- 构建分支: ${env.tag}\\\\\\n
- 验证码: ${env.randomToken}\\\\\\n
- 审批地址: ${env.BUILD_URL}input/\\\\\\n
- 构建差异: ${env.BUILD_URL}last-changes/\\\\\\n
- 构建日志: ${env.BUILD_URL}console\\\\\\n
- 镜像名称: 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras:${env.servicename}_${env.tag}\\\\\\n
- 发布地址: https://rancher.mimo.immo/dashboard/c/local/explorer/apps.deployment/${env.projectname}-${envi}/${env.servicename}-deployment?mode=edit#labels\\\\\\n
- 发版备注:${env.comment}\\\\\\n
\\"],\\"sign\\":\\"b68f5dcd4d2a3d778d282567208e8690\\"}'
echo -n \\\$MESSAGE | nc -u -w1 13.212.162.101 31164
" > ./send.sh && sed -i "s/http:\\/\\/jenkins:8080/https:\\/\\/jenkins.mimo.immo/g" ./send.sh && /bin/bash ./send.sh
    """
}

// def ReqPublishNotifyByUdpNotifyServer(admin,telegramId,envi,api,result) {    
//     sh """
//         #!/bin/sh -e\\\\\\n curl -k --location --request POST '${api}' \
//         --header 'Content-Type: application/json' \
//         --data '{
//         "env": "${envi}",
//         "admin": "${admin}",
//         "telegramId": "${telegramId}",
//         "builder": "${env.BUILD_USER}",
//         "jobName": "${env.JOB_NAME}",
//         "tag": "${env.tag}",
//         "diff": "${env.BUILD_URL}last-changes/",
//         "log": "${env.BUILD_URL}console",
//         "status":"${result}",
//         "comment": "${env.comment}",
//         "imageName":"024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras:${env.servicename}_${env.tag}",
//         "serviceName":"${env.servicename}",
//         "projectName":"${env.projectname}"}'
//     """
// }

def ReqPublishNotifyByUdpNotifyServer(admin,telegramId,envi,api,result) {    

    if ( envi == 'prod' ){
        text="生产环境镜像构建并推送完毕，请点击rancher链接手动替换镜像名称进行发版。"
    }else if ( envi == 'test' ){
        text="测试环境发布完毕。"
    }else{
        text="开发环境发布完毕。"
    }

    sh """
echo "
#!/bin/bash
MESSAGE='{\\"api\\":\\"${api}\\",\\"time\\":1691397277,\\"data\\":[\\"
### ${text} ###\\\\\\n
- 申请人: ${env.BUILD_USER}\\\\\\n
- 构建名称: ${env.JOB_NAME}\\\\\\n
- 构建分支: ${env.tag}\\\\\\n
- 构建差异: ${env.BUILD_URL}last-changes/\\\\\\n
- 构建日志: ${env.BUILD_URL}console\\\\\\n
- 镜像名称: 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/${env.servicename}:${env.tag}\\\\\\n
- 发布地址: https://rancher.mimo.immo/dashboard/c/local/explorer/apps.deployment/${env.projectname}-${envi}/${env.servicename}-deployment?mode=edit#labels\\\\\\n
- 发版备注:${env.comment}\\\\\\n
- 发版结果:${result}\\\\\\n
\\"],\\"sign\\":\\"b68f5dcd4d2a3d778d282567208e8690\\"}'
echo -n \\\$MESSAGE | nc -u -w1 notify-udp-service.mimo-prod.svc.cluster.local 8081
" > ./send.sh && sed -i "s/http:\\/\\/jenkins:8080/https:\\/\\/jenkins.mimo.immo/g" ./send.sh && /bin/bash ./send.sh
    """


}

// def NotifyV2(envi,result) {    

//     if ( envi == 'prod' ){
//         text="生产环境镜像构建并推送完毕，请点击rancher链接手动替换镜像名称进行发版。"
//     }else if ( envi == 'test' ){
//         text="测试环境发布完毕。"
//     }else{
//         text="开发环境发布完毕。"
//     }

//     sh """
// curl -H "Content-Type: application/json" -H "type: info" -X POST -d \$'{\"api\":\"m_1691395720\",\"data\":\"\\\\\\n
// ### ${text} ###\\\\\\n
// - 申请人: ${env.BUILD_USER}\\\\\\n
// - 构建名称: ${env.JOB_NAME}\\\\\\n
// - 构建分支: ${env.tag}\\\\\\n
// - 构建差异: ${env.BUILD_URL}last-changes/\\\\\\n
// - 构建日志: ${env.BUILD_URL}console\\\\\\n
// - 镜像名称: 024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/${env.servicename}:${env.tag}\\\\\\n
// - 发布地址: https://rancher.mimo.immo/dashboard/c/local/explorer/apps.deployment/${env.projectname}-${envi}/${env.servicename}-deployment?mode=edit#labels\\\\\\n
// - 发版备注:${env.comment}\\\\\\n
// - 发版结果:${result}\\\\\\n
// \"}' "https://web3.mimo.immo/notify/notify"
//     """


// }

def NotifyV2(envi,result) {    

    if ( envi == 'prod' ){
        text="生产环境镜像构建并推送完毕，请点击rancher链接手动替换镜像名称进行发版。"
    }else if ( envi == 'test' ){
        text="测试环境发布完毕。"
    }else{
        text="开发环境发布完毕。"
    }


    sh """
    build_url=\${${env.BUILD_URL}:9}
    echo \$build_url
    curl -X POST -H Content-Type:application/json -H type:info -d \'{"api":"m_1691395720","data":"${text}\\n申请人:${env.BUILD_USER}\\n构建名称:${env.JOB_NAME}\\n构建分支:${env.tag}\\n构建差异:`echo ${env.BUILD_URL}`last-changes/\\n构建日志:`echo ${env.BUILD_URL}`console\\n镜像名称:024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/${env.servicename}:${env.tag}\\n发布地址:https://rancher.mimo.immo/dashboard/c/local/explorer/apps.deployment/${env.projectname}-${envi}/${env.servicename}-deployment?mode=edit#labels\\n发版备注:${env.comment}\\n发版结果:${result}"}\' https://web3.mimo.immo/notify/notify
    """

}



def LastChanges(){

    lastChanges since: 'LAST_SUCCESSFUL_BUILD', format:'SIDE',matching: 'LINE'

}

def GitCheckOut(tag, url){

    if ( env.option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( env.option == 'Expand' ){
        echo "expand does not need this."
    }else{
        // checkout scmGit(branches: [[name: "${tag}"]], extensions: [], userRemoteConfigs: [[credentialsId: '554844dc-46f5-4975-a50e-00dba9745e87', url: "${url}"]])
        checkout scmGit(branches: [[name: "${tag}"]], extensions: [], userRemoteConfigs: [[credentialsId: 'cyberflowtyler', url: "${url}"]])
    }

}

def Approval(envi){

    echo "判断是否需要进入审批."

    //获取当前登录用户账户、姓名
    Applier_id = "${env.BUILD_USER_ID}"
    Applier_name = "${env.BUILD_USER}"

    // 开发环境和管理人员都不需要审批
    // 测试环境去掉审批 2023.8.29
    // 生产环境去掉审批
    if ( envi == "dev" ){
        approval = "NO"
    } else if ( Applier_name == "zhangkai"){
        approval = "NO"
    } else if ( Applier_name == "xiangbo"){
        approval = "NO"
    } else if ( Applier_name == "longhaijian"){
        approval = "NO"
    } else if ( envi == "test"){
        approval = "NO"
    }else if ( envi == "prod"){
        approval = "NO"
    }else{
        approval = "YES"
        // 测试和生产环境发版审批人
        if ( envi == "test" ){
            adminUser = "xiangbo"
            approvalDD = "txiangbo"
        }else{
            adminUser = "longhaijian"
            approvalDD = "Tyler"
        }
        approvalDD = "sample"
    }

    if ( approval == "YES"){

        echo "需要走审批流程,审批通过后才可发版."

        // echo "${adminUser}"

        // 判断审批人是否为本人
        if (Applier_name == "${adminUser}"){
            error '审批人不能为本人，任务已终止'
        } 
        // 推送消息到telegram
        input_message = "$Applier_name 申请发布项目 ${env.JOB_NAME} 到 ${envi} 环境"
        if (envi == "test"){
            udpNotifyAPI = "m_1691395718"
        }else{
            udpNotifyAPI = "m_1691395719"
        }
        ReqApprovalByUdpNotifyServer(adminUser,approvalDD,envi,udpNotifyAPI)

        // 生成待审批界面,阻塞
        def isAbort  = false   //取消按钮
        timeout(time:1800, unit:'HOURS'){  //等待审批人审批，并通过timeout设置任务过期时间，防止任务永远挂起
            try {
                def token = input(
                    id: 'inputap', message: "$input_message", ok:"同意", submitter:"zhangkai,longhaijian", parameters: [
                    [$class: 'StringParameterDefinition',defaultValue: "", name: 'token',description: '请输入发布的秘钥' ]
                    ])
                    if ( "${token}" == env.randomToken) {
                        echo "密钥校验正确,开始发版"
                    }else{
                        error '密钥校验错误,终止任务'
                    }
            }catch(e) { // input false
                throw e
            }
        }
    }else{
        echo "dev/test环境发版或管理人员发版不需要走审批流程."
    }
}


def Sonarqube(){
    echo "sonarqube"
}

def PostmanAPItest(){
    echo "postman API test"
}

def Notify(envi,result) {

    Applier_name = "${env.BUILD_USER}"

    // telegram

    if ( envi == "dev" ){
        approval = "NO"
    } else if ( Applier_name == "linyuecheng"){
        approval = "NO"
    } else if ( Applier_name == "zhangkai"){
        approval = "NO"
    } else if ( Applier_name == "xiangbo"){
        approval = "NO"
    } else if ( Applier_name == "longhaijian"){
        approval = "NO"
    }else{
        approval = "YES"
    }

    if ( approval == "YES"){
        if ( envi == "test" ){
            adminUser = "zhangkai"
        }else{
            adminUser = "longhaijian"
        }
    }else{
        adminUser = "nobody"
    }

    approvalDD = "sample"

    // 推送消息到telegram
    udpNotifyAPI = "m_1691395720"
    ReqPublishNotifyByUdpNotifyServer(adminUser,approvalDD,envi,udpNotifyAPI,result)


}

def PushImageToEcr(option, imageAddr, imageRepo, imageTag, originalImageName, imageTarName, path){

    if ( option == 'Rollback' ){
        echo "rollback does not need this."
    }else if ( option == 'Expand' ){
        echo "expand does not need this."
    }else{
        sh """
        docker logout
        docker login --username AWS ${imageAddr} -p `aws ecr --profile mmdevops get-login-password --region ap-southeast-1`

        docker load -i ${path}/${imageTarName}

        docker tag ${originalImageName} ${imageAddr}/${imageRepo}:${imageTag}
        docker push ${imageAddr}/${imageRepo}:${imageTag}
        """
    }
}

