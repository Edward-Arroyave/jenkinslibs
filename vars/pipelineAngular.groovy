/**
 * Pipeline para construir y desplegar una aplicación Angular.
 * 
 * @param config Map con configuración requerida para el pipeline:
 *  - BUILD_FOLDER: Carpeta base para construcción y workspace.
 *  - REPO_PATH: Ruta local donde se clona el repositorio.
 *  - DIST_PATH: Ruta donde queda el build compilado.
 *  - SITE_URL: URL del sitio (si se usa en alguna parte).
 *  - REPO_URL: URL del repositorio Git para clonar.
 *  - SERVER: Servidor FTP o destino para despliegue.
 *  - BRANCH: Rama Git que se clona.
 *  - DIST_DIR: Directorio de distribución para deploy.
 *  - NODE_VERSION: Versión de Node.js a usar para la compilación.
 *  - FILE_ENV (opcional): Archivo .env para copiar si está presente.
 *  - NG_APP_VERSION (opcional): Versión de la app Angular para usar en .env.
 */
def call(Map config) {

    def requiredParams = ['BUILD_FOLDER', 'REPO_PATH', 'DIST_PATH', 'SITE_URL', 'REPO_URL', 'SERVER', 'BRANCH', 'DIST_DIR', 'NODE_VERSION']
    def missingParams = requiredParams.findAll { !config[it] }
    if (missingParams) {
        error("❌ Error de configuración: Faltan los siguientes parámetros obligatorios: ${missingParams.join(', ')}")
    }

    pipeline {
        agent any 

        tools {
            nodejs config.NODE_VERSION
        }

        stages {
            stage('Clone Repository') {
                steps {
                    script {
                        cloneRepo(
                            branch:  config.BRANCH,
                            repoPath: config.REPO_PATH,
                            repoUrl: config.REPO_URL
                        )
                    }
                }
            }

            stage('Copiar archivo .env si es enviado') {
                steps {
                    script {
                        copyDotenvIfPresent(
                            REPO_PATH: config.REPO_PATH,
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
                            repoPath: config.REPO_PATH,
                            distPath: config.DIST_PATH
                        )
                    }
                }
            }

            stage('Desplegar a FTP') {
                steps {
                    script {
                        deployAngular(
                            repoPath: config.REPO_PATH,
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

                  script {
                    sendSuccessNotification([
                        webhookUrl: 'https://ithealthannar.webhook.office.com/webhookb2/c54248da-6d3a-4414-92ca-c1dc7e652a8f@f032bfba-2704-407e-9848-b5e307857e2a/IncomingWebhook/717629d5740b4992aa52b1a7f1154784/c25693fe-5b81-416e-af0d-e0cfccdb6e77/V2KitHF-N0Y-QSJkaG0DSP6WHs7L95RUnvZhqBi637Jsc1',
                        productName: env.PRODUCT_NAME,
                        deployUser: env.DEPLOY_USER,
                        commitMessage: env.COMMIT_MESSAGE,
                        commitHash: env.COMMIT_HASH,
                        buildNumber: env.BUILD_NUMBER,
                        buildUrl: env.BUILD_URL
                    ])
                  }
            }
            failure {
                echo '💥 ERROR DURANTE EL DESPLIEGUE'
            }
            always {
                
                cleanWs()
            
            }
        }
    }
}
