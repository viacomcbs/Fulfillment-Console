import groovy.transform.Field

ddescription = '''
               Project owner:<br><br>
               Repo:https://github.com/viacomcbs/MIP_App.git <br>
               Tickets:<br> https://paramount.atlassian.net/browse/master <br>
               '''
@Field def SEND_REPORT_EMAIL_ADDRESS = [
        'vishnupriya.arumugam@paramount.com',
        'tejaswini.shinde@paramount.com',
        'qa_automation_status_-aaaagmcoe366b6jbhgse77jupu@viacomcbs.org.slack.com' 
]. join(', ')


pipeline {
    agent {
        label 'generic_v3&& (64GB || 16GB || 4GB)'
    }

    options {
        buildDiscarder logRotator(daysToKeepStr: '15', numToKeepStr: '15')
        disableConcurrentBuilds()
        timeout(150)
    }

    parameters {
        string              defaultValue: 'master',
                            description: 'branch name',
                            name: 'CODE_BRANCH',
                            trim: true          
        
        string              defaultValue: 'TestNGSuiteConfig.xml',
                            description: '',
                            name: 'TestNGSuiteConfig',
                            trim: true          
        
        string              defaultValue: 'MIP',
                            description: '',
                            name: 'APPLICATION_TITLE',
                            trim: true          
        
        choice              choices: [
                                'DEV',
                                'UAT',
                                'PROD',
                                'PR',
                                'QA'
                            ],
                            description: 'Target test environment.',
                            name: 'TEST_ENVIRONMENT'
         

        string              defaultValue: '',
                            name: 'BUILD_NUMBER',
                            description: 'The build number to be tested against.',
                            trim: true

        validatingString    defaultValue: '5',
                            failedValidationMessage: 'Please provide decimal value',
                            name: 'THREADS',
                            regex: '\\d+'

       validatingString    defaultValue: SEND_REPORT_EMAIL_ADDRESS,
                            failedValidationMessage: 'Recipients of the test reports.',
                            name: 'SEND_REPORT_EMAIL_ADDRESS',
                            regex: '([\\w+\\.\\ \\-\\_@]+\\W?)+'            

    }

environment {
    MAVEN   = credentials('svc.bvc_map')
    MVN_SET = credentials('custom-settingsxml')
  }
    triggers {
        snapshotDependencies()
        parameterizedCron """\
                0 8 * * *  %TEST_ENVIRONMENT=UAT
        """
    }
        
    stages {
        stage ('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage ('Clone') {
            steps {
                checkout scm
            }
        }

        stage('Pre Step') {
            steps {
                buildDescription "<b>Branch:</b> $CODE_BRANCH<br><b>test_environment:</b> $TEST_ENVIRONMENT"
            }
        }

        stage('Build') {
            steps {
                withMaven(options: [dependenciesFingerprintPublisher(disabled: true)],
                    // Maven installation declared in the Jenkins "Global Tool Configuration"
                    maven: '3.8.6',
                    jdk: 'JDK 11'
                ) {
                    sh "mvn -s $MVN_SET wrapper:wrapper"
                    sh "./mvnw -s $MVN_SET clean test-compile -am"
                    
                }
            }
        }

        stage('run tests') {
            environment {
            EC2_SUBNET = 'AWS'
            }
            steps {
                withMaven(options: [dependenciesFingerprintPublisher(disabled: true)],
                    // Maven installation declared in the Jenkins "Global Tool Configuration"
                    maven: '3.8.6',
                    jdk: 'JDK 11'
                ) {
                
                  
                  sh "mvn -s $MVN_SET clean test -DsuiteXmlFile=src/test/resources/$TestNGSuiteConfig -Dsystem.test.testenvironment=$TEST_ENVIRONMENT -Dsystem.test.applicationtitle=$APPLICATION_TITLE -Dsystem.test.sendchatreport=true -Dsystem.test.rerunonfailure=true -Dsystem.test.runasfactory=true -Dsystem.test.uploadreporttojenkins=true -Dsystem.test.codebranch=$CODE_BRANCH -Dparallel=methods -DthreadCount=$THREADS -Dsystem.test.sendreportautoemails=true -Dsystem.test.sendreportemailaddress='tejaswini.shinde@paramount.com, qa_automation_status_-aaaagmcoe366b6jbhgse77jupu@viacomcbs.org.slack.com' -Dsystem.test.sethuelights=true -Dsystem.test.hueweblightid=14"
                  } 
                }
              }

        stage('Report') {
            steps {
                allure includeProperties: false, results: [[path: 'allure-results']]
                testNG()
            }
        }
    }

    post {
        notBuilt {
            emailext body: '''${SCRIPT, template="groovy-html.template"}''',
              mimeType: 'text/html',
              subject: "[Jenkins] ${BUILD_URL}",
              to: "SEND_REPORT_EMAIL_ADDRESS"
        }
        success {
            emailext body: '''${SCRIPT, template="groovy-html.template"}''',
              mimeType: 'text/html',
              subject: "[Jenkins] ${BUILD_URL}",
              to: "SEND_REPORT_EMAIL_ADDRESS"
        }
       always{
            mail to: "tejaswini.shinde@paramount.com",
            subject: "Jenkins build: $APPLICATION_TITLE",
            body: "[Jenkins] ${BUILD_URL}"
        }
    }

}
        
