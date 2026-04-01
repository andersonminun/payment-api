pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        PROJECT_ID = 'sistema-pagamentos-491823'
        REGION = 'us-central1'
        CLUSTER = 'pagamentos-cluster'
        IMAGE = "us-central1-docker.pkg.dev/${PROJECT_ID}/pagamentos/payment-api"
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
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

        stage('Docker Build e Push') {
            steps {
                echo 'Construindo e publicando imagem Docker...'
                sh """
                    docker build -t ${IMAGE}:${BUILD_NUMBER} .
                    docker push ${IMAGE}:${BUILD_NUMBER}
                    docker tag ${IMAGE}:${BUILD_NUMBER} ${IMAGE}:latest
                    docker push ${IMAGE}:latest
                """
            }
        }

        stage('Deploy no GKE') {
            steps {
                echo 'Fazendo deploy no GKE...'
                sh """
                    gcloud container clusters get-credentials ${CLUSTER} \
                        --zone ${REGION}-a \
                        --project ${PROJECT_ID}

                    kubectl set image deployment/payment-api \
                        payment-api=${IMAGE}:${BUILD_NUMBER}

                    kubectl rollout status deployment/payment-api
                """
            }
        }
    }

    post {
        success {
            echo "Deploy realizado! Imagem: ${IMAGE}:${BUILD_NUMBER}"
        }
        failure {
            echo 'Pipeline falhou — verifique os logs.'
        }
    }
}