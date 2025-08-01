def call(Map config) {
    if (!config.repoPath) error "Falta el parámetro obligatorio: repoPath"
    if (!config.server) error "Falta el parámetro obligatorio: server"
    if (!config.distDir) error "Falta el parámetro obligatorio: distDir"

    echo "🚀 Desplegando al servidor FTP: ${config.server}"
    echo "📂 Carpeta de distribución: ${config.distDir}"
    dir(config.repoPath) {
        ftpPublisher(
            alwaysPublishFromMaster: false,
            continueOnError: false,
            failOnError: false,
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
                            removePrefix: config.distDir,
                            sourceFiles: "${config.distDir}/**/*"
                        ]
                    ],
                    usePromotionTimestamp: false,
                    useWorkspaceInPromotion: false,
                    verbose: true
                ]
            ]
        )
    }
    echo "✅ Despliegue completado"
}
