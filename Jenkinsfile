pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-config
      mountPath: /kaniko/.docker
    - name: workspace-volume
      mountPath: /workspace
  - name: aws-cli
    image: amazon/aws-cli:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-config
      mountPath: /kaniko/.docker
    - name: workspace-volume
      mountPath: /workspace
  - name: git-tools
    image: alpine/git:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - name: workspace-volume
      mountPath: /workspace
  volumes:
  - name: docker-config
    emptyDir: {}
  - name: workspace-volume
    emptyDir: {}
"""
        }
    }

    environment {
        AWS_DEFAULT_REGION = "ap-northeast-2"
        ECR_REPO = "177716289679.dkr.ecr.ap-northeast-2.amazonaws.com/izza/back"
        CD_REPO = "https://github.com/aws-izza/izza-cd.git"
        CD_BRANCH = "main"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup ECR Auth for Kaniko') {
            steps {
                container('aws-cli') {
                    sh '''
                        # ECR 로그인 토큰 생성
                        aws ecr get-login-password --region $AWS_DEFAULT_REGION > /tmp/ecr-token
                        
                        # Kaniko 디렉토리 생성 (중요!)
                        mkdir -p /kaniko/.docker
                        
                        # Kaniko용 Docker config 생성
                        echo "{\\"auths\\":{\\"177716289679.dkr.ecr.ap-northeast-2.amazonaws.com\\":{\\"auth\\":\\"$(echo -n AWS:$(cat /tmp/ecr-token) | base64 -w 0)\\"}}}" > /kaniko/.docker/config.json
                        
                        # 권한 설정
                        chmod 644 /kaniko/.docker/config.json
                    '''
                }
            }
        }

        stage('Build & Push Image with Kaniko') {
            steps {
                script {
                    COMMIT_HASH = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    IMAGE_TAG = "dev-${COMMIT_HASH}"

                    container('kaniko') {
                        sh """
                            /kaniko/executor \\
                                --dockerfile=Dockerfile \\
                                --context=dir:///home/jenkins/agent/workspace/izza-back-dev \\
                                --destination=$ECR_REPO:$IMAGE_TAG \\
                                --cache=true \\
                                --cache-dir=/cache \\
                                --skip-tls-verify-registry=177716289679.dkr.ecr.ap-northeast-2.amazonaws.com
                        """
                    }
                }
            }
        }

        stage('Update CD Repo (Dev)') {
            steps {
                container('git-tools') {
                    script {
                        sh '''
                            # git 설정
                            git config --global user.email "rlatndls113@gmail.com"
                            git config --global user.name "musclefrog"
                            
                            # 필요한 도구 설치
                            apk add --no-cache yq curl

                            # CD repo 클론
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
}