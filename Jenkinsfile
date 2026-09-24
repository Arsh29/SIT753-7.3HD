pipeline {
    agent any

    tools {
        jdk 'Java'
        maven 'Maven'
    }

    stages {

        stage('Build') {
            steps {
                echo 'Building Spring Boot application...'
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running JUnit tests...'
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
                echo 'Preparing Maven dependencies for Trivy...'

                bat 'mvn dependency:resolve -Dmaven.repo.local="%WORKSPACE%\\.m2"'

                echo 'Running Trivy security scan...'

                bat '''
        docker run --rm ^
          -v "%WORKSPACE%:/project" ^
          -v "%WORKSPACE%\\.m2:/root/.m2" ^
          aquasec/trivy:latest fs /project --scanners vuln
        '''
            }
        }
    }

    post {
        success {
            echo 'Build, tests and code quality analysis completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the console output.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}