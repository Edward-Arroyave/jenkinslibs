/**
 * Pipeline step para desplegar archivos a un servidor FTP usando ftpPublisher.
 *
 * @param config Map con configuración requerida:
 *   - repoPath (String): Ruta local del repositorio donde se encuentra la carpeta de distribución.
 *   - server (String): Nombre o configuración del servidor FTP definido en Jenkins.
 *   - distDir (String): Directorio relativo dentro de repoPath que contiene los archivos a subir.
 *
 * Requisitos:
 *   - El plugin FTP Publisher debe estar instalado en Jenkins.
 *   - El servidor FTP debe estar configurado en Jenkins con el nombre especificado en config.server.
 */
def call(Map config) {
    // Validar parámetros obligatorios
    if (!config.repoPath) error "Falta el parámetro obligatorio: repoPath"
    if (!config.server) error "Falta el parámetro obligatorio: server"
    if (!config.distDir) error "Falta el parámetro obligatorio: distDir"

    // Normalizar separadores de ruta
    def normalizedDistDir = config.distDir.replace("\\", "/").replaceAll('^\\./', '')
    def fullDistPath = "${config.repoPath}/${normalizedDistDir}".replace("\\", "/")

    // Validar que el directorio existe
    if (!fileExists(fullDistPath)) {
        error "❌ El directorio de distribución no existe: ${fullDistPath}"
    }

    // Mensajes informativos
    echo "🚀 Desplegando al servidor FTP: ${config.server}"
    echo "📂 Carpeta de distribución (normalizada): ${normalizedDistDir}"

    // Cambiar directorio de trabajo al repoPath indicado
    dir(config.repoPath) {
        try {
            ftpPublisher(
                alwaysPublishFromMaster: false,
                continueOnError: false,
                failOnError: true, // Si hay error en FTP, falla el pipeline
                publishers: [
                    [
                        configName: config.server,
                        transfers: [
                            [
                                asciiMode: false,
                                cleanRemote: false,
                                excludes: '',
                                flatten: false,
                                makeEmptyDirs: false,
                                noDefaultExcludes: false,
                                patternSeparator: '[, ]+',
                                removePrefix: normalizedDistDir, // Prefijo exacto a eliminar
                                sourceFiles: "${normalizedDistDir}/**/*"
                            ]
                        ],
                        usePromotionTimestamp: false,
                        useWorkspaceInPromotion: false,
                        verbose: true
                    ]
                ]
            )
            echo "✅ Despliegue completado correctamente"
        } catch (err) {
            error "❌ Error en despliegue FTP: ${err.getMessage()}"
        }
    }
}
