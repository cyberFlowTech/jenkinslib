package org.devops

// 通用 通知方法
def Notify(envi,result) {
    // 兼容老的
    NotifyV2(envi,result)
}

// 差异对比
def LastChanges(){
    lastChanges since: 'LAST_SUCCESSFUL_BUILD', format:'SIDE',matching: 'LINE'
}

// 分支检出
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

// 代码质量检查
def Sonarqube(){
    echo "代码质量检查 实现"
}

// postman api 测试
def PostmanAPItest(){
    echo "postman api测试 实现"
}

// 通用前后端发布通知
def NotifyV2(envi,result) {    
    if ( envi == 'prod' ){
        text="### 生产环境镜像构建并推送完毕，请点击rancher链接手动替换镜像名称进行发版 ###"
    }else if ( envi == 'test' ){
        text="### 测试环境发布完毕 ###"
    }else{
        text="### 开发环境发布完毕 ###"
    }
    str = "${env.BUILD_URL}"
    replaced = str.replace("http://jenkins:8080", "https://jenkins.mimo.immo")
    sh """
    curl -X POST -H Content-Type:application/json -H type:info -d \'{"api":"m_1691395720","data":"${text}\\n申请人:${env.BUILD_USER}\\n构建名称:${env.JOB_NAME}\\n构建分支:${env.tag}\\n构建差异:${replaced}last-changes/\\n构建日志:${replaced}console\\n镜像名称:024905375334.dkr.ecr.ap-southeast-1.amazonaws.com/${env.servicename}:${env.tag}\\n发布地址:https://rancher.mimo.immo/dashboard/c/local/explorer/apps.deployment/${env.projectname}-${envi}/${env.servicename}-deployment?mode=edit#labels\\n发版备注:${env.comment}\\n发版结果:${result}"}\' https://web3.mimo.immo/notify/notify
    """
}

// uniapp打包通知
def UniappPackNotifyV2(envi,result) {    

    if ( env.isCustom == 'true' ){
        packageTag="Debug"
    }else{
        packageTag=env.tag
    }

    if ( envi == 'prod' ){
        text="### Uniapp 正式包 ###"
    }else if ( envi == 'test' ){
        text="### Uniapp 测试包 ###"
    }else{
        text="### Uniapp dev包 ###"
    }

    jenkinsAddr = "${env.BUILD_URL}"
    jenkinsAddrReplaced = jenkinsAddr.replace("http://jenkins:8080", "https://jenkins.mimo.immo")
    branch = "${env.branch}"
    branchReplaced = branch.replace("/", "_")
    sh """
    curl -X POST -H Content-Type:application/json -H type:info -d \'{"api":"m_1691395722","data":"${text}\\n构建名称:${env.JOB_NAME}\\n执行人:${env.BUILD_USER}\\n版本名称:${env.tag}\\n版本编号:${env.versioncode}\\n构建分支:${env.branch}\\n差异:${jenkinsAddrReplaced}last-changes/\\n日志:${jenkinsAddrReplaced}console\\n执行结果:${result}\\napk地址: http://res.mimo.immo/unrelease/test/${branchReplaced}${packageTag}.apk\\naab地址: http://res.mimo.immo/unrelease/test/${branchReplaced}${packageTag}.aab\\nipa地址: http://res.mimo.immo/unrelease/test/${branchReplaced}${packageTag}.ipa"}\' https://web3.mimo.immo/notify/notify
    """
}

// 原生IOS打包通知
def OriginIosAppPackNotifyV2(result) {    


    text="### 原生 IOS ${env.packageName}包 ###"

    jenkinsAddr = "${env.BUILD_URL}"
    jenkinsAddrReplaced = jenkinsAddr.replace("http://jenkins:8080", "https://jenkins.mimo.immo")
    branch = "${env.branch}"
    def packageName = sh(script: 'ls /Users/apple/Documents/git/jenkins/workspace/MIMO_iOS_\${Package}/ios_pack/', returnStdout: true).trim()
    sh """
    curl -X POST -H Content-Type:application/json -H type:info -d \'{"api":"m_1691395722","data":"${text}\\n包类型:${env.package}\\n构建名称:${env.JOB_NAME}\\n执行人:${env.BUILD_USER}\\n执行结果:${result}\\n分支:${env.branch}\\n版本号:${env.version}\\n差异:${jenkinsAddrReplaced}last-changes/\\n日志:${jenkinsAddrReplaced}console\\nipa地址: http://192.168.0.240:7050/Share/IOS_PACK/${env.packageName}/${packageName}"}\' https://web3.mimo.immo/notify/notify
    """
}



