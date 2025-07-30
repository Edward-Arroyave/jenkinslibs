
def call(Map config) {
   
 if (!config.BUILD_FOLDER || !config.REPO_PATH || !config.DIST_PATH || !config.SITE_URL || !config.REPO_URL) {
        error("❌ cloneRepo: 'branch', 'repoPath', and 'repoUrl' parameters are required.")
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
                    echo "🔍 Validando ambiente: ${params.Ambiente}"
                    
                    switch (params.Ambiente) {
                        case 'Test':
                            SERVER = 'SERVER_QC_TEST'
                            BRANCH = 'Test'
                            break
                        case 'Demo':
                            SERVER = 'SERVER_QC_DEMO'
                            BRANCH = 'Demo'
                            break
                        case 'PRE_PRODUCCION':
                            SERVER = 'SERVER_QC_PRE_PRODUCCION'
                            BRANCH = 'main'
                            break
                        default:
                            error "❌ ERROR: Ambiente no soportado: ${params.Ambiente}"
                    }

                    echo "✅ Ambiente seleccionado: ${env.SERVER} | Rama: ${env.BRANCH}"
                }
            }
        }

       
 
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