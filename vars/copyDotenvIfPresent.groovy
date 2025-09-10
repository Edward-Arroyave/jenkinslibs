def call(Map config) {
    if (!config.REPO_PATH) {
        error "🚫 Falta el parámetro obligatorio: REPO_PATH"
    }

    echo "📁 [INFO] Ruta del repositorio recibida: '${config.REPO_PATH}'"

    if (!config.FILE_ENV?.trim()) {
        echo "⚠️ [ADVERTENCIA] No se recibió el parámetro FILE_ENV. Se omite la copia/actualización del archivo .env."
        return
    }

    def envFilePath = "${config.REPO_PATH}/.env"

    try {
        // Verificar si ya existe un .env en el repo
        def exists = sh(script: "[ -f '${envFilePath}' ] && echo 'yes' || echo 'no'", returnStdout: true).trim()

        if (exists == "yes") {
            echo "ℹ️ [INFO] El archivo .env ya existe en '${envFilePath}'. No se sobreescribirá."
        } else {
            echo "📦 [INFO] Copiando archivo .env desde credencial '${config.FILE_ENV}'..."
            withCredentials([file(credentialsId: config.FILE_ENV, variable: 'ENV_SECRET_PATH')]) {
                sh """
                    cp "\$ENV_SECRET_PATH" "${envFilePath}"
                """
            }
            echo "✅ [ÉXITO] Archivo .env copiado correctamente."
        }

        // Validar actualización de NG_APP_VERSION
        if (config.NG_APP_VERSION?.trim()) {
            echo "🛠️ [ACTUALIZACIÓN] NG_APP_VERSION recibido: '${config.NG_APP_VERSION}'. Actualizando archivo .env..."

            sh """
                if grep -qE '^\\s*NG_APP_VERSION\\s*=' "${envFilePath}"; then
                    sed -i -E 's/^\\s*NG_APP_VERSION\\s*=.*/NG_APP_VERSION=${config.NG_APP_VERSION}/' "${envFilePath}"
                else
                    echo 'NG_APP_VERSION=${config.NG_APP_VERSION}' >> "${envFilePath}"
                fi
                echo '🔎 [VERIFICACIÓN] NG_APP_VERSION actual en el .env:'
                grep -E '^\\s*NG_APP_VERSION\\s*=' "${envFilePath}" || echo 'NG_APP_VERSION no encontrado'
            """

            echo "✅ [ACTUALIZACIÓN] NG_APP_VERSION actualizado correctamente."
        } else {
            echo "ℹ️ [INFO] No se recibió NG_APP_VERSION, se mantiene el archivo tal cual."
        }

    } catch (e) {
        error "❌ [ERROR] Fallo durante la copia o modificación del archivo .env. Detalle: ${e.getMessage()}"
    }
}
