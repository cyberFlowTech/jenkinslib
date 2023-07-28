package org.devops

def PrintMsg(msg){
  println(msg)
}

// https://api.telegram.org/bot6230860729:AAE9Uj6JJYOap40Ie35UR-STW7ZKEYslNoI/sendMessage
// -919670551

def ReqApprovalByTelegramWebhook(admin,telegramId,envi,api) {    
    sh """
        #!/bin/sh -e\n curl -k --location --request POST '${api}' \
        --header 'Content-Type: application/json' \
        --data '{
        "env": "${envi}",
        "admin": "${admin}",
        "telegramId": "${telegramId}",
        "builder": "${env.BUILD_USER}",
        "jobName": "${env.JOB_NAME}",
        "tag": "${env.tag}",
        "token": "${env.randomToken}",
        "approvalUrl": "${env.BUILD_URL}input/",
        "diff": "${env.BUILD_URL}last-changes/",
        "log": "${env.BUILD_URL}console",
        "comment": "${env.comment}",
        "imageName":"024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras:${env.servicename}_${env.tag}",
        "serviceName":"${env.servicename}",
        "projectName":"${env.projectname}"}'
    """
}

def ReqPublishNotifyByTelegramWebhook(admin,telegramId,envi,api,result) {    
    sh """
        #!/bin/sh -e\n curl -k --location --request POST '${api}' \
        --header 'Content-Type: application/json' \
        --data '{
        "env": "${envi}",
        "admin": "${admin}",
        "telegramId": "${telegramId}",
        "builder": "${env.BUILD_USER}",
        "jobName": "${env.JOB_NAME}",
        "tag": "${env.tag}",
        "diff": "${env.BUILD_URL}last-changes/",
        "log": "${env.BUILD_URL}console",
        "status":"${result}",
        "comment": "${env.comment}",
        "imageName":"024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/infras:${env.servicename}_${env.tag}",
        "serviceName":"${env.servicename}",
        "projectName":"${env.projectname}"}'
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
        checkout scmGit(branches: [[name: "${tag}"]], extensions: [], userRemoteConfigs: [[credentialsId: '36af8cfe-cad6-456b-88d0-b7ee16c5e425', url: "${url}"]])
    }

}

def Approval(envi){

    echo "判断是否需要进入审批."

    //获取当前登录用户账户、姓名
    Applier_id = "${env.BUILD_USER_ID}"
    Applier_name = "${env.BUILD_USER}"

    // 开发环境和管理人员都不需要审批
    if ( envi == "dev" ){
        approval = "NO"
    } else if ( Applier_name == "zhangkai"){
        approval = "NO"
    } else if ( Applier_name == "xiangbo"){
        approval = "NO"
    } else if ( Applier_name == "longhaijian"){
        approval = "NO"
    }else{
        approval = "YES"
        // 测试和生产环境发版审批人
        if ( envi == "test" ){
            adminUser = "zhangkai"
            approvalDD = "carterzhk"
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
            telegramAPI = "http://172.16.13.30:5001/telegramGroupNotify/jenkins/approvalTest"
        }else{
            telegramAPI = "http://172.16.13.30:5001/telegramGroupNotify/jenkins/approvalProd"
        }
        ReqApprovalByTelegramWebhook(adminUser,approvalDD,envi,telegramAPI)

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
        echo "dev环境发版或管理人员发版不需要走审批流程."
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
    telegramAPI = "http://172.16.13.30:5001/telegramGroupNotify/jenkins/publishNotify"
    ReqPublishNotifyByTelegramWebhook(adminUser,approvalDD,envi,telegramAPI,result)


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

