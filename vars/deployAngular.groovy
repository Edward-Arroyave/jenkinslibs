def call(Map config) {
    if (!config.repoPath) error "Falta el parámetro obligatorio: repoPath"
    if (!config.server) error "Falta el parámetro obligatorio: server"

    def repoPath = config.repoPath
    def server = config.server

    echo "🚀 Desplegando al servidor FTP: ${server}"
    dir(repoPath) {
        ftpPublisher(
            alwaysPublishFromMaster: false,
            continueOnError: false,
            failOnError: false,
            publishers: [
                [
                    configName: server,
                    transfers: [
                        [
                            asciiMode: false,
                            cleanRemote: false,
                            excludes: '',
                            flatten: false,
                            makeEmptyDirs: false,
                            noDefaultExcludes: false,
                            patternSeparator: '[, ]+',
                            removePrefix: 'dist/browser',
                            sourceFiles: 'dist/browser/**/*'
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
