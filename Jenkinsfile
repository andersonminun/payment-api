pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Obtendo código fonte...'
                checkout scm
            }
        }

        stage('Compilar') {
            steps {
                echo 'Compilando o projeto...'
                sh './mvnw compile'
            }
        }

        stage('Testes') {
            steps {
                echo 'Rodando testes unitários...'
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Empacotar') {
            steps {
                echo 'Gerando o JAR...'
                sh './mvnw package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Construindo imagem Docker...'
                sh 'docker build -t payment-api:latest .'
            }
        }
    }

    post {
        success {
            echo 'Pipeline concluído com sucesso!'
        }
        failure {
            echo 'Pipeline falhou — verifique os logs.'
        }
    }
}