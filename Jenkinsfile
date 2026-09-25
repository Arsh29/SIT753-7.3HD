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
                                script: 'powershell -NoProfile -Command "try { (Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8081/api/students -TimeoutSec 5).StatusCode } catch { 0 }"',
                                returnStdout: true
                            ).readLines().last().trim()

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
                echo 'Promoting tested image to production...'

                bat '''
        docker tag student-api:%BUILD_NUMBER% student-api:release-%BUILD_NUMBER%

        docker rm -f student-api-production 2>NUL || exit /B 0

        docker run -d ^
          --name student-api-production ^
          --network student-network ^
          -p 8082:8099 ^
          student-api:release-%BUILD_NUMBER%
        '''

                echo 'Waiting for production application...'

                timeout(time: 60, unit: 'SECONDS') {
                    waitUntil {
                        script {
                            def result = bat(
                                script: 'powershell -NoProfile -Command "try { (Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8082/api/students -TimeoutSec 5).StatusCode } catch { 0 }"',
                                returnStdout: true
                            ).readLines().last().trim()

                            echo "Production API response: ${result}"

                            return result == '200'
                        }
                    }
                }

                echo "Production release successful: release-${BUILD_NUMBER}"
            }
        }

        stage('Monitoring') {
            steps {
                echo 'Starting Prometheus monitoring...'

                bat '''
        docker rm -f prometheus 2>NUL || exit /B 0

        docker run -d ^
          --name prometheus ^
          --network student-network ^
          -p 9090:9090 ^
          -v "%WORKSPACE%\\prometheus.yml:/etc/prometheus/prometheus.yml" ^
          prom/prometheus
        '''

                echo 'Prometheus started.'
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