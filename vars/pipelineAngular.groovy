
def call(Map config) {
   
def missingParams = []

if (!config.BUILD_FOLDER)  missingParams << 'BUILD_FOLDER'
if (!config.REPO_PATH)     missingParams << 'REPO_PATH'
if (!config.DIST_PATH)     missingParams << 'DIST_PATH'
if (!config.SITE_URL)      missingParams << 'SITE_URL'
if (!config.REPO_URL)      missingParams << 'REPO_URL'
if (!config.SERVER)        missingParams << 'SERVER'
if (!config.BRANCH)        missingParams << 'BRANCH'
if (!config.DIST_DIR)      missingParams << 'DIST_DIR'
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
    steps {
        script {
            echo "🔍 Verificando si se recibió el parámetro FILE_ENV: '${config.FILE_ENV}'"
            echo "📁 Ruta del repositorio: '${config.REPO_PATH}'"

            // Verificar si FILE_ENV está definido y no está vacío
            if (config.FILE_ENV?.trim()) {
                echo "🔐 Se recibió FILE_ENV con valor: '${config.FILE_ENV}', se intentará copiar el archivo .env."

                try {
                    withCredentials([file(credentialsId: config.FILE_ENV, variable: 'ENV_SECRET_PATH')]) {
                        sh """
                            echo "📦 Copiando archivo .env desde la credencial..."
                            cp \$ENV_SECRET_PATH ${config.REPO_PATH}/.env

                            echo "🔍 Contenido del archivo .env:"
                            cat ${config.REPO_PATH}/.env

                            echo "✅ Variables disponibles en el entorno:"
                            env
                        """
                    }
                } catch (e) {
                    error "❌ No se pudo encontrar o copiar el archivo .env desde la credencial '${config.FILE_ENV}'. Error: ${e.getMessage()}"
                }

            } else {
                echo "⚠️ No se recibió FILE_ENV. Se omite la copia del archivo .env."
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