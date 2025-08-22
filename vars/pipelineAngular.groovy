def call(Map config) {

    def requiredParams = ['SITE_URL', 'REPO_URL', 'SERVER', 'BRANCH', 'DIST_DIR', 'NODE_VERSION']
    def missingParams = requiredParams.findAll { !config[it] }
    if (missingParams) {
        error("❌ Error de configuración: Faltan los siguientes parámetros obligatorios: ${missingParams.join(', ')}")
    }

    pipeline {
        agent {
            docker {
                label 'docker-node'
                image "node:${config.NODE_VERSION}"
                args '-u root:root -v /var/run/docker.sock:/var/run/docker.sock -v /home/jenkins:/home/jenkins'
            }
        }

        environment {
            BUILD_FOLDER = "${env.WORKSPACE}/${env.BUILD_ID}"
            REPO_PATH    = "${BUILD_FOLDER}/repo"
            DIST_PATH    = "${REPO_PATH}/${config.DIST_DIR}"
        }

        tools {
            nodejs config.NODE_VERSION
        }

        stages {
            stage('Clone Repository') {
                steps {
                    script {
                        cloneRepo(
                            branch:  config.BRANCH,
                            repoPath: env.REPO_PATH,
                            repoUrl: config.REPO_URL
                        )
                    }
                }
            }

            stage('Copiar archivo .env si es enviado') {
                steps {
                    script {
                        copyDotenvIfPresent(
                            REPO_PATH: env.REPO_PATH,
                            FILE_ENV: config.FILE_ENV,
                            NG_APP_VERSION: config.NG_APP_VERSION
                        )
                    }
                }
            }

            stage('Compilar Angular') {
                steps {
                    script {
                        compileAngular(
                            repoPath: env.REPO_PATH,
                            distPath: env.DIST_PATH
                        )
                    }
                }
            }

            stage('Desplegar a FTP') {
                steps {
                    script {
                        deployAngular(
                            repoPath: env.REPO_PATH,
                            server: config.SERVER,
                            distDir: config.DIST_DIR
                        )
                    }
                }
            }
        }

        post {
            success {
                echo '🎉 DESPLIEGUE FINALIZADO CON ÉXITO'
            }
            failure {
                echo '💥 ERROR DURANTE EL DESPLIEGUE'
            }
            always {
                script {
                    sendNotificationTeams([
                        webhookUrl: 'https://ithealthannar.webhook.office.com/webhookb2/c54248da-6d3a-4414-92ca-c1dc7e652a8f@f032bfba-2704-407e-9848-b5e307857e2a/IncomingWebhook/717629d5740b4992aa52b1a7f1154784/c25693fe-5b81-416e-af0d-e0cfccdb6e77/V2KitHF-N0Y-QSJkaG0DSP6WHs7L95RUnvZhqBi637Jsc1',
                        productName: 'Angular App',
                        ENVIRONMENT: config.BRANCH
                    ])
                }

                cleanWs()
            }
        }
    }
}
