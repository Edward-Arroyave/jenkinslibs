def call(Map config) {
    if (!config.REPO_PATH) error "Falta el parámetro obligatorio: REPO_PATH"

    echo "🔍 Verificando si se recibió el parámetro FILE_ENV: '${config.FILE_ENV}'"
    echo "📁 Ruta del repositorio: '${config.REPO_PATH}'"

    if (config.FILE_ENV?.trim()) {
        echo "🔐 Se recibió FILE_ENV con valor: '${config.FILE_ENV}', se intentará copiar el archivo .env."

        try {
            withCredentials([file(credentialsId: config.FILE_ENV, variable: 'ENV_SECRET_PATH')]) {
                sh """
                    echo "📦 Copiando archivo .env desde la credencial..."
                    cp \$ENV_SECRET_PATH ${config.REPO_PATH}/.env



                    echo "✅ .env agregeado correctamente a ${config.REPO_PATH}/.env"
                """
            }
        } catch (e) {
            error "❌ No se pudo encontrar o copiar el archivo .env desde la credencial '${config.FILE_ENV}'. Error: ${e.getMessage()}"
        }

    } else {
        echo "⚠️ No se recibió FILE_ENV. Se omite la copia del archivo .env."
    }
}
