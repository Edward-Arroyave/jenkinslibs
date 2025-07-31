def call(Map config) {

    def missingParams = []
    if (!config.BUILD_FOLDER) missingParams << 'BUILD_FOLDER'
    if (!config.REPO_PATH) missingParams << 'REPO_PATH'
    if (!config.DIST_PATH) missingParams << 'DIST_PATH'
    if (!config.SITE_URL) missingParams << 'SITE_URL'
    if (!config.REPO_URL) missingParams << 'REPO_URL'
    if (!config.AMBIENTE) missingParams << 'AMBIENTE'

    if (missingParams) {
        error("❌ Error de configuración: Faltan los siguientes parámetros obligatorios: ${missingParams.join(', ')}")
    }

    pipeline {
        agent any

        tools {
            nodejs '18.19.0'
        }

        stages {

            stage('Validar ambiente') {
                steps {
                    script {
                        echo "🔍 Validando ambiente: ${config.AMBIENTE}"

                        switch (config.AMBIENTE) {
                            case 'Test':
                                config.SERVER = 'SERVER_QC_TEST'
                                config.BRANCH = 'Test'
                                break
                            case 'Demo':
                                config.SERVER = 'SERVER_QC_DEMO'
                                config.BRANCH = 'Demo'
                                break
                            case 'PRE_PRODUCCION':
                                config.SERVER = 'SERVER_QC_PRE_PRODUCCION'
                                config.BRANCH = 'main'
                                break
                            default:
                                error "❌ ERROR: Ambiente no soportado: ${config.AMBIENTE}"
                        }

                        echo "✅ Ambiente seleccionado: ${config.SERVER} | Rama: ${config.BRANCH}"
                    }
                }
            }

            stage('Clonar repositorio') {
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

            stage('Copiar archivo .env si existe') {
                when {
                    expression { return config.ENV_FILE != null && config.ENV_FILE?.trim() }
                }
                steps {
                    withCredentials([file(credentialsId: ${config.ENV_FILE}, variable: 'ENV_SECRET_PATH')]) {
                    sh '''
                        echo "📦 Copiando archivo .env desde la credencial..."
                        cp $ENV_SECRET_PATH .env

                        echo "🔍 Contenido del archivo .env:"
                        cat .env

                         echo "✅ Variables disponibles en el entorno:"
                     '''
                    }
                }
            }

            stage('Compilar Angular') {
                steps {
                    script {
                        angularBuildPipeline(
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
                            server: config.SERVER
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
                cleanWs()
            }
        }
    }
}
