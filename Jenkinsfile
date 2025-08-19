pipeline {
    agent any
    
    environment {
        ECR_REGISTRY = "177716289679.dkr.ecr.ap-northeast-2.amazonaws.com"
        ECR_REPOSITORY = "izza/backend"
        AWS_DEFAULT_REGION = "ap-northeast-2"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "✅ 코드 체크아웃 완료"
            }
        }
        
        stage('Checkout GitOps Repository') {
            steps {
                script {
                    // GitOps 저장소 클론
                    sh '''
                        if [ -d "izza-cd" ]; then
                            rm -rf izza-cd
                        fi
                        git clone https://github.com/aws-izza/izza-cd.git
                    '''
                }
            }
        }

        stage('Generate Image Tag') {
            steps {
                script {
                    def timestamp = new Date().format('yyyyMMdd-HHmmss')
                    def shortCommit = env.GIT_COMMIT.take(7)
                    env.IMAGE_TAG = "${env.BRANCH_NAME}-${timestamp}-${shortCommit}"
                    env.FULL_IMAGE_NAME = "${ECR_REGISTRY}/${ECR_REPOSITORY}:${env.IMAGE_TAG}"
                    
                    echo "🏷️ Generated image tag: ${env.IMAGE_TAG}"
                    echo "📍 Full image name: ${env.FULL_IMAGE_NAME}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "🔧 도커 이미지 빌드 시작..."
                    sh "docker build -t ${env.FULL_IMAGE_NAME} ."
                    echo "✅ 도커 이미지 빌드 완료"
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    echo "📤 ECR에 이미지 푸시 시작..."
                    sh """
                        # ECR 로그인
                        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        
                        # 이미지 푸시
                        docker push ${env.FULL_IMAGE_NAME}
                    """
                    echo "✅ ECR 푸시 완료: ${env.FULL_IMAGE_NAME}"
                }
            }
        }
        stage('Update Deployment YAML') {
            steps {
                script {
                    dir('izza-cd') {
                        sh '''
                            git config user.name "jenkins"
                            git config user.email "jenkins@company.com"
                        '''
                        
                        sh """
                            sed -i 's|image: .*|image: ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}|' environments/dev/app.yaml
                        """
                        
                        sh """
                            git add environments/dev/app.yaml
                            git commit -m "Update dev image tag to ${IMAGE_TAG}"
                            git push origin main
                        """
                    }
                }
            }
        }

    post {
        success {
            echo """
            🎉 빌드 성공!
            
            📊 빌드 정보:
            - 브랜치: ${env.BRANCH_NAME}
            - 이미지: ${env.FULL_IMAGE_NAME}
            - 커밋: ${env.GIT_COMMIT}
            """
        }
        
        failure {
            echo "❌ 빌드 실패! 로그를 확인하세요."
        }
        
        always {
            // 로컬 이미지 정리
            sh "docker rmi ${env.FULL_IMAGE_NAME} || true"
        }
    }
}