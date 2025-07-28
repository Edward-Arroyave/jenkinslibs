def call(Map config) {
    if (!config.repoPath) error "Falta el parámetro obligatorio: repoPath"
    if (!config.distPath) error "Falta el parámetro obligatorio: distPath"

    def repoPath = config.repoPath
    def distPath = config.distPath

    echo "⚙️ Compilando Angular..."
    dir(repoPath) {
      sh 'node --max-old-space-size=9096 ./node_modules/.bin/ng build --aot --configuration production --optimization'
    }
    echo "✅ Compilación completada"

    echo "📂 Verificando carpeta: ${distPath}"
    if (!fileExists(distPath)) {
        error "❌ ERROR: No se encontró la carpeta compilada en ${distPath}"
    }
    echo "✅ Carpeta compilada encontrada"
}
