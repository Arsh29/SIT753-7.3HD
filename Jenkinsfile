pipeline {
    agent any

    tools {
        jdk 'Java'
        maven 'Maven'
    }

    environment {
        IMAGE_NAME = 'student-api'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building application JAR...'
                bat 'mvn clean package -DskipTests'

                echo 'Building Docker image...'
                bat 'docker build -t %IMAGE_NAME%:%IMAGE_TAG% .'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                bat 'mvn test'
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Running SonarQube analysis...'

                withSonarQubeEnv('SonarQube') {
                    bat 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=sit753-student-api'
                }
            }
        }

        stage('Security') {
            steps {
                echo 'Running Trivy security scan on Docker image...'
                bat 'trivy image --scanners vuln student-api:%BUILD_NUMBER%'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying Docker image...'

                bat '''
        docker rm -f student-api-container 2>NUL || exit /B 0

        docker run -d ^
          --name student-api-container ^
          -p 8081:8099 ^
          student-api:%BUILD_NUMBER%
        '''

                echo 'Waiting for application to start...'

                timeout(time: 60, unit: 'SECONDS') {
                    waitUntil {
                        script {
                            def result = bat(
                                script: 'powershell -NoProfile -Command "(Invoke-WebRequest -UseBasicParsing http://localhost:8081/api/students).StatusCode"',
                                returnStdout: true
                            ).trim()

                            echo "API response: ${result}"

                            return result == '200'
                        }
                    }
                }

                echo 'Deployment successful.'
            }
        }

        stage('Release') {
            steps {
                echo 'Release stage placeholder...'
                echo 'Release will use Docker image %IMAGE_NAME%:%IMAGE_TAG%.'
            }
        }

        stage('Monitoring') {
            steps {
                echo 'Monitoring stage placeholder...'
                echo 'Application monitoring will be configured here.'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check the stage logs above.'
        }

        always {
            echo "Build number: ${BUILD_NUMBER}"
            echo "Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
    }
}