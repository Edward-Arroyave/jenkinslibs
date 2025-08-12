este es el flujo que realiza el despliegue:
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

    // Mensajes informativos del despliegue
    echo "🚀 Desplegando al servidor FTP: ${config.server}"
    echo "📂 Carpeta de distribución: ${config.distDir}"

    // Cambiar directorio de trabajo al repoPath indicado
    dir(config.repoPath) {
        // Ejecutar la publicación FTP con configuración detallada
        ftpPublisher(
            alwaysPublishFromMaster: false, // No publicar siempre desde master
            continueOnError: false,          // No continuar si hay errores
            failOnError: false,              // No fallar el build si hay error en FTP (puedes ajustar)
            publishers: [
                [
                    configName: config.server, // Nombre del servidor FTP configurado en Jenkins
                    transfers: [
                        [
                            asciiMode: false,         // No usar modo ASCII, subir binario
                            cleanRemote: false,       // No limpiar carpeta remota antes de subir
                            excludes: '',             // No excluir archivos
                            flatten: false,           // Mantener estructura de carpetas
                            makeEmptyDirs: false,     // No crear carpetas vacías en remoto
                            noDefaultExcludes: false, // Usar exclusiones por defecto
                            patternSeparator: '[, ]+', // Separador para patrones de archivos
                            removePrefix: config.distDir, // Remover este prefijo de la ruta al subir
                            sourceFiles: "${config.distDir}/**/*" // Archivos a subir
                        ]
                    ],
                    usePromotionTimestamp: false, // No usar timestamp de promoción
                    useWorkspaceInPromotion: false, // No usar el workspace en promoción
                    verbose: true              // Mostrar logs detallados de la transferencia
                ]
            ]
        )
    }

    // Mensaje de éxito
    echo "✅ Despliegue completado"
}