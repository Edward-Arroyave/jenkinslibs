def call(Map config) {
    // Validación de parámetros requeridos
    if (!config.repoPath) {
        error "Falta el parámetro obligatorio: repoPath"
    }
    if (!config.distPath) {
        error "Falta el parámetro obligatorio: distPath"
    }

    def repoPath = config.repoPath
    def distPath = config.distPath

    pipeline {
        agent any

        stages {
            stage('Instalar dependencias') {
                steps {
                    echo "📥 Instalando dependencias..."
                    dir(repoPath) {
                        sh 'npm install -f'
                    }
                    echo "✅ Dependencias instaladas"
                }
            }

            stage('Compilar Angular') {
                steps {
                    echo "⚙️ Compilando Angular..."
                    dir(repoPath) {
                        sh 'npx --max-old-space-size=9096 ng build --aot --configuration production --optimization'
                    }
                    echo "✅ Compilación completada"
                }
            }

            stage('Verificar carpeta compilada') {
                steps {
                    script {
                        echo "📂 Verificando carpeta: ${distPath}"
                        if (!fileExists(distPath)) {
                            error "❌ ERROR: No se encontró la carpeta compilada en ${distPath}"
                        }
                        echo "✅ Carpeta compilada encontrada"
                    }
                }
            }
        }
    }
}
