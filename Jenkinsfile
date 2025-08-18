pipeline {
    agent any

    environment {
        AWS_DEFAULT_REGION = "ap-northeast-2"
        ECR_REPO = "177716289679.dkr.ecr.ap-northeast-2.amazonaws.com/izza/back"
        CD_REPO = "https://github.com/aws-izza/izza-cd.git"
        CD_BRANCH = "main"   // CD repo는 main 하나만 관리
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Login to ECR') {
            steps {
                sh '''
                  aws ecr get-login-password --region $AWS_DEFAULT_REGION \
                  | docker login --username AWS --password-stdin 177716289679.dkr.ecr.ap-northeast-2.amazonaws.com
                '''
            }
        }

        stage('Build & Push Image') {
            steps {
                script {
                    COMMIT_HASH = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    IMAGE_TAG = "dev-${COMMIT_HASH}"

                    sh """
                      docker build -t $ECR_REPO:$IMAGE_TAG .
                      docker push $ECR_REPO:$IMAGE_TAG
                    """
                }
            }
        }

        stage('Update CD Repo (Dev)') {
            steps {
                script {
                    sh '''
                      git config --global user.email "rlatndls113@gmail.com"
                      git config --global user.name "musclefrog"

                      git clone -b $CD_BRANCH $CD_REPO cd-repo

                      # dev 환경 배포 yaml 업데이트
                      yq -i ".spec.template.spec.containers[0].image = \\"$ECR_REPO:dev-$COMMIT_HASH\\"" cd-repo/environments/dev/app.yaml

                      cd cd-repo
                      git add .
                      git commit -m "update(dev): image to $ECR_REPO:dev-$COMMIT_HASH" || echo "No changes to commit"
                      git push origin $CD_BRANCH
                    '''
                }
            }
        }
    }
}