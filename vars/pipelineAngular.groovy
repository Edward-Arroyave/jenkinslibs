
def call(Map config) {
   
def missingParams = []
if (!config.BUILD_FOLDER) missingParams << 'BUILD_FOLDER'
if (!config.REPO_PATH) missingParams << 'REPO_PATH'
if (!config.DIST_PATH) missingParams << 'DIST_PATH'
if (!config.SITE_URL) missingParams << 'SITE_URL'
if (!config.REPO_URL) missingParams << 'REPO_URL'
if (!config.SERVER)   missingParams << 'SERVER'
if (!config.BRANCH)   missingParams << 'BRANCH'
if (!config.DIST_DIR)   missingParams << 'DIST_DIR'
if (!config.NODE_VERSION)   missingParams << 'NODE_VERSION'


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

        stage('Copiar archivo .env si existe') {
            when {
                expression { return config.ENV_FILE != null && config.ENV_FILE?.trim() }
            }
            steps {
                script {
                    withCredentials([file(credentialsId: config.ENV_FILE, variable: 'ENV_SECRET_PATH')]) {
                sh """
                    echo "📦 Copiando archivo .env desde la credencial..."
                    cp \$ENV_SECRET_PATH ${config.REPO_PATH}/.env

                    echo "🔍 Contenido del archivo .env:"
                    cat ${config.REPO_PATH}/.env

                    echo "✅ Variables disponibles en el entorno:"
                    env
                """

                    }
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
            cleanWs()
        }
    }
}
}