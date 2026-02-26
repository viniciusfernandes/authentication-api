pipeline {
    agent any

    environment {
        GRADLE_OPTS = "-Dorg.gradle.daemon=false"
    }

    parameters {
        string(
            name: 'VERSION',
            description: 'Version tag for the Docker image (e.g., 1.0.0)',
            defaultValue: ''
        )
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/viniciusfernandes/authentication-api.git'
            }
        }

        stage('Build') {
            steps {
                echo "Building version: ${params.VERSION}"
                sh './gradlew build -x test'
            }
        }

        /* -----------------------------------------------------------
           Dependency Validation using Trivy (via Docker container - no installation needed!)
           ----------------------------------------------------------- */
        stage('Dependency Validation') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo "Running Trivy dependency scan using Docker container..."

                    sh '''
                        mkdir -p build/reports/trivy

                        docker run --rm -v "$PWD:/workspace" -w /workspace \
                            aquasec/trivy:latest fs \
                            --format json --output build/reports/trivy/scan.json \
                            build/libs/ || true

                        docker run --rm -v "$PWD:/workspace" -w /workspace \
                            aquasec/trivy:latest fs \
                            --format table --output build/reports/trivy/scan.txt \
                            build/libs/ || true
                    '''

                    script {
                        def trivyReports = sh(
                            script: 'ls -1 build/reports/trivy/* 2>/dev/null || echo ""',
                            returnStdout: true
                        ).trim()

                        if (trivyReports) {
                            archiveArtifacts artifacts: 'build/reports/trivy/*', fingerprint: true
                        } else {
                            echo "Warning: No Trivy reports generated, skipping artifact archival"
                        }
                    }

                    sh 'cat build/reports/trivy/scan.txt || echo "No vulnerabilities found or scan completed"'

                    echo "Dependency security analysis completed."
                }
            }
        }
        /* ----------------------------------------------------------- */

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Package') {
            steps {
                sh './gradlew bootJar -x test'
                archiveArtifacts artifacts: 'build/libs/authentication-api-0.0.1-SNAPSHOT.jar', fingerprint: true
            }
        }

        stage('Docker Build & Tag') {
            steps {
                script {
                    def version = (params.VERSION ?: '').trim()
                    if (!version) {
                        def gitSha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        version = "${env.BUILD_NUMBER}-${gitSha}"
                        echo "VERSION not provided. Auto-generated version: ${version}"
                    }

                    env.IMAGE_TAG = "authentication-api:${version}"
                    echo "Building Docker image: ${env.IMAGE_TAG}"

                    sh "docker build -t ${env.IMAGE_TAG} ."
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )]) {
                        def dockerhubUsername = env.DOCKERHUB_USERNAME?.trim()

                        if (!dockerhubUsername || dockerhubUsername.contains('@')) {
                            error "DockerHub username is invalid ('${dockerhubUsername}'). Use your Docker Hub USERNAME (not email) in Jenkins credentials."
                        }

                        def targetImage = "${dockerhubUsername}/authentication-api:${env.IMAGE_TAG.split(':')[1]}"

                        sh "docker tag ${env.IMAGE_TAG} ${targetImage}"

                        sh """
                            echo "\$DOCKERHUB_TOKEN" | docker login -u "\$DOCKERHUB_USERNAME" --password-stdin
                            docker push ${targetImage}
                            docker logout
                        """
                    }
                }
            }
        }
    }
}
