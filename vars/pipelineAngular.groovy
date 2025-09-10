def call(Map config) {

    def requiredParams = ['SITE_URL', 'REPO_URL', 'SERVER', 'BRANCH', 'DIST_DIR', 'NODE_VERSION']
    def missingParams = requiredParams.findAll { !config[it] }
    if (missingParams) {
        error("❌ Error de configuración: Faltan los siguientes parámetros obligatorios: ${missingParams.join(', ')}")
    }

    pipeline {
        agent any

        tools {
            nodejs "${config.NODE_VERSION}"
        }

        environment {
            BUILD_FOLDER = "${env.WORKSPACE}/${config.BRANCH}"
            REPO_PATH    = "${BUILD_FOLDER}/repo"
            DIST_PATH    = "${REPO_PATH}/${config.DIST_DIR}"
        }

        stages {
            stage('Clone Repository') {
                steps {
                    script {
                        echo "📥 [INICIO] Clonando repositorio desde ${config.REPO_URL} (branch: ${config.BRANCH})"
                        cloneRepo(
                            branch:  config.BRANCH,
                            repoPath: env.REPO_PATH,
                            repoUrl: config.REPO_URL
                        )
                        echo "✅ [FIN] Repositorio clonado en ${env.REPO_PATH}"
                    }
                }
            }

            stage('Copiar archivo .env si es enviado') {
                steps {
                    script {
                        echo "📄 [INICIO] Verificando archivo .env..."
                        copyDotenvIfPresent(
                            REPO_PATH: env.REPO_PATH,
                            FILE_ENV: config.FILE_ENV,
                            NG_APP_VERSION: config.NG_APP_VERSION
                        )
                        echo "✅ [FIN] Archivo .env verificado/copied"
                    }
                }
            }

            stage('Compilar Angular') {
                steps {
                    script {
                        echo "⚙️ [INICIO] Compilando Angular en ${env.REPO_PATH}..."
                        compileAngular(
                            repoPath: env.REPO_PATH,
                            distPath: env.DIST_PATH
                        )
                        echo "✅ [FIN] Compilación de Angular completada, artefactos en ${env.DIST_PATH}"
                    }
                }
            }

            stage('Desplegar a FTP') {
                steps {
                    script {
                        echo "🚀 [INICIO] Desplegando aplicación a servidor: ${config.SERVER}"
                        deployAngular(
                            repoPath: env.REPO_PATH,
                            server: config.SERVER,
                            distDir: config.DIST_DIR
                        )
                        echo "✅ [FIN] Aplicación desplegada en ${config.SERVER}"
                    }
                }
            }

            stage('Verificar despliegue') {
                steps {
                    script {
                        echo "🔎 [INICIO] Verificando despliegue en ${config.SITE_URL}"
                        validateSite(config.SITE_URL)
                        echo "✅ [FIN] Verificación de despliegue exitosa en ${config.SITE_URL}"
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
                    echo "📢 Enviando notificación a Teams..."
                    sendNotificationTeams([
                        webhookUrl: 'https://ithealthannar.webhook.office.com/webhookb2/c54248da-6d3a-4414-92ca-c1dc7e652a8f@f032bfba-2704-407e-9848-b5e307857e2a/IncomingWebhook/717629d5740b4992aa52b1a7f1154784/c25693fe-5b81-416e-af0d-e0cfccdb6e77/V2KitHF-N0Y-QSJkaG0DSP6WHs7L95RUnvZhqBi637Jsc1',
                        productName: 'Angular App',
                        ENVIRONMENT: config.BRANCH
                    ])
                  
                }
            }
        }
    }
}
