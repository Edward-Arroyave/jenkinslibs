/**
 * Copia un archivo .env desde las credenciales de Jenkins al repositorio local
 * y opcionalmente actualiza la variable NG_APP_VERSION dentro del archivo .env.
 *
 * @param config Map con configuración esperada:
 *   - REPO_PATH (String): Ruta local donde se ubicará el archivo .env
 *   - FILE_ENV (String, opcional): ID de la credencial tipo archivo en Jenkins que contiene el .env
 *   - NG_APP_VERSION (String, opcional): Valor para actualizar la variable NG_APP_VERSION en el .env
 *
 * Requisitos:
 *   - El parámetro REPO_PATH es obligatorio.
 *   - Si FILE_ENV no se proporciona o está vacío, la función termina sin hacer nada.
 *   - Si NG_APP_VERSION no se proporciona, el archivo .env se copia sin modificaciones.
 */
def call(Map config) {
    // Validar que se haya recibido la ruta del repositorio
    if (!config.REPO_PATH) {
        error "🚫 Falta el parámetro obligatorio: REPO_PATH"
    }

    echo "📁 [INFO] Ruta del repositorio recibida: '${config.REPO_PATH}'"

    // Verificar si se recibió el ID de la credencial del archivo .env
    if (!config.FILE_ENV?.trim()) {
        echo "⚠️ [ADVERTENCIA] No se recibió el parámetro FILE_ENV. Se omite completamente la copia del archivo .env."
        return // Terminar ejecución porque no hay archivo .env que copiar
    }

    echo "🔐 [INFO] Se recibió FILE_ENV: '${config.FILE_ENV}'. Procediendo a copiar el archivo .env desde las credenciales."

    try {
        // Usar las credenciales tipo archivo para acceder al .env
        withCredentials([file(credentialsId: config.FILE_ENV, variable: 'ENV_SECRET_PATH')]) {
            sh """
                echo "📦 Copiando archivo .env desde la credencial..."
                cp \$ENV_SECRET_PATH ${config.REPO_PATH}/.env
            """
        }

        echo "✅ [ÉXITO] Archivo .env copiado correctamente a: ${config.REPO_PATH}/.env"

        // Verificar si se proporcionó valor para NG_APP_VERSION
        if (!config.NG_APP_VERSION?.trim()) {
            echo "ℹ️ [INFO] No se recibió el parámetro NG_APP_VERSION. Se mantiene el valor existente en el .env."
            return // No hay necesidad de modificar el archivo .env
        }

        echo "🛠️ [ACTUALIZACIÓN] NG_APP_VERSION recibido: '${config.NG_APP_VERSION}'. Actualizando archivo .env..."

        // Actualizar o agregar la variable NG_APP_VERSION en el archivo .env
        sh """
            sed -i -E 's/^\\s*NG_APP_VERSION\\s*=.*/NG_APP_VERSION=${config.NG_APP_VERSION}/' ${config.REPO_PATH}/.env || echo 'NG_APP_VERSION=${config.NG_APP_VERSION}' >> ${config.REPO_PATH}/.env

            echo '🔎 [VERIFICACIÓN] NG_APP_VERSION actual en el .env:'
            grep -E '^\\s*NG_APP_VERSION\\s*=' ${config.REPO_PATH}/.env || echo 'NG_APP_VERSION no encontrado'
        """

        echo "✅ [ACTUALIZACIÓN] NG_APP_VERSION actualizado correctamente."

    } catch (e) {
        // Capturar y reportar cualquier error en la copia o modificación del archivo
        error "❌ [ERROR] Fallo durante la copia o modificación del archivo .env desde '${config.FILE_ENV}'. Detalle: ${e.getMessage()}"
    }
}
