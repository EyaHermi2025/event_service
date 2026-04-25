pipeline {
    agent any

    tools {
        maven 'maven-3'
    }

    environment {
        DOCKER_HUB_USER = 'eyahermi2025'
        IMAGE_NAME = 'event-service'
        PROJECT_KEY = 'EyaHermi2025_event-service' // Vérifie si c'est bien ta clé
        ORG_KEY = 'eyahermi2025'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    sh "mvn sonar:sonar -Dsonar.projectKey=${PROJECT_KEY} -Dsonar.host.url=http://192.168.1.100:9000 -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_HUB_USER}/${IMAGE_NAME}:${env.BUILD_ID}")
                    docker.withRegistry('', 'docker-hub-credentials') {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Kubernetes Deployment') {
            steps {
                withKubeConfig([credentialsId: 'kubeconfig-id']) {
                    sh "sed -i 's/\${DOCKER_HUB_USER}/${DOCKER_HUB_USER}/g' k8s/deployment.yaml"
                    sh "kubectl apply -f k8s/deployment.yaml"
                }
            }
        }
    }
}
