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

    // Validar que se pasen todos los parámetros obligatorios
    def requiredParams = ['BUILD_FOLDER', 'REPO_PATH', 'DIST_PATH', 'SITE_URL', 'REPO_URL', 'SERVER', 'BRANCH', 'DIST_DIR', 'NODE_VERSION']
    def missingParams = requiredParams.findAll { !config[it] }
    if (missingParams) {
        error("❌ Error de configuración: Faltan los siguientes parámetros obligatorios: ${missingParams.join(', ')}")
    }

    pipeline {
        // Ejecutar en cualquier agente disponible
        agent any 

        // Definir la herramienta Node.js con la versión especificada en config
        tools {
            nodejs config.NODE_VERSION
        }

        stages {
            // Etapa para clonar el repositorio Git
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

            // Etapa opcional para copiar archivo .env si es enviado en configuración
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

            // Etapa para compilar la aplicación Angular usando Node.js
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

            // Etapa para desplegar la aplicación compilada a un servidor FTP
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
            // Mensaje si el pipeline termina con éxito
            success {
                echo '🎉 DESPLIEGUE FINALIZADO CON ÉXITO'
            }
            // Mensaje si el pipeline falla en alguna etapa
            failure {
                echo '💥 ERROR DURANTE EL DESPLIEGUE'
            }
            // Siempre limpiar el workspace al final del pipeline para no dejar archivos temporales
            always {
                cleanWs()
            }
        }
    }
}
