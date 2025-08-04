def call(Map config) {
    if (!config.REPO_PATH) {
        error "🚫 Falta el parámetro obligatorio: REPO_PATH"
    }

    echo "📁 [INFO] Ruta del repositorio recibida: '${config.REPO_PATH}'"

    if (!config.FILE_ENV?.trim()) {
        echo "⚠️ [ADVERTENCIA] No se recibió el parámetro FILE_ENV. Se omite completamente la copia del archivo .env."
        return
    }

    echo "🔐 [INFO] Se recibió FILE_ENV: '${config.FILE_ENV}'. Procediendo a copiar el archivo .env desde las credenciales."

    try {
        // Copiar archivo .env desde Jenkins Credentials
        withCredentials([file(credentialsId: config.FILE_ENV, variable: 'ENV_SECRET_PATH')]) {
            sh """
                echo "📦 Copiando archivo .env desde la credencial..."
                cp \$ENV_SECRET_PATH ${config.REPO_PATH}/.env
            """
        }

        echo "✅ [ÉXITO] Archivo .env copiado correctamente a: ${config.REPO_PATH}/.env"

        // Validar si se recibió NG_APP_VERSION
        if (!config.NG_APP_VERSION?.trim()) {
            echo "ℹ️ [INFO] No se recibió el parámetro NG_APP_VERSION. Se mantiene el valor existente en el .env."
            return
        }

        echo "🛠️ [ACTUALIZACIÓN] NG_APP_VERSION recibido: '${config.NG_APP_VERSION}'. Actualizando archivo .env..."

        // Modificar o agregar la variable NG_APP_VERSION
        sh """
            sed -i 's/^NG_APP_VERSION=.*/NG_APP_VERSION=${config.NG_APP_VERSION}/' ${config.REPO_PATH}/.env || echo "NG_APP_VERSION=${config.NG_APP_VERSION}" >> ${config.REPO_PATH}/.env

            echo '🔎 [VERIFICACIÓN] NG_APP_VERSION actual en el .env:'
            grep '^NG_APP_VERSION=' ${config.REPO_PATH}/.env || echo 'NG_APP_VERSION no encontrado'
        """

        echo "✅ [ACTUALIZACIÓN] NG_APP_VERSION actualizado correctamente."

    } catch (e) {
        error "❌ [ERROR] Fallo durante la copia o modificación del archivo .env desde '${config.FILE_ENV}'. Detalle: ${e.getMessage()}"
    }
}
