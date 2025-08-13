def call (Map config) {

    echo "📢 Enviando notificación de éxito a Microsoft Teams"

    def durationMillis = currentBuild.duration
    def totalSeconds = (durationMillis / 1000).toInteger()
    def seconds = totalSeconds % 60
    def minutes = (totalSeconds / 60) % 60
    def hours = (totalSeconds / 3600)

    def durationText = ""
    if (hours > 0) { durationText += "${hours}h " }
    if (minutes > 0) { durationText += "${minutes}m " }
    durationText += "${seconds}s"

    office365ConnectorSend(
        status: currentBuild.currentResult,
        message: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        adaptiveCards: true,
        color: "FF0000",
        factDefinitions: [
            [name: "Build triggered by", template: env.BUILD_USER],
            [name: "Commit Author", template: env.COMMIT_AUTHOR],
            [name: "Commit Message", template: env.COMMIT_MESSAGE],
            [name: "Commit Hash", template: env.COMMIT_HASH],
            [name: "Build", template: env.BUILD_NUMBER],
            [name: "Remarks", template: currentBuild.fullDisplayName],
            [name: "DeployTime", template: durationText]
        ]
    )
}
