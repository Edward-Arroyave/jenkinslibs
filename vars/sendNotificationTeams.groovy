// File: sendNotificationTeams.groovy

def call(Map config) {
    // Duración de la build
    def durationMillis = currentBuild.duration ?: 0
    def totalSeconds = (durationMillis / 1000).toInteger()

    def seconds = totalSeconds % 60
    def totalMinutes = totalSeconds / 60
    def minutes = totalMinutes % 60
    def hours = totalMinutes / 60

    def durationText = ""
    if (hours > 0) { durationText += "${hours}h " }
    if (minutes > 0) { durationText += "${minutes}m " }
    durationText += "${seconds}s"

    // Determinar color y emoji según resultado
    def status = currentBuild.currentResult ?: "FAILURE"
    def color = "FF0000"    // rojo por defecto
    def emoji = "❌"
    def statusText = "Build Failed"

    if (status == "SUCCESS") {
        color = "00FF00"      // verde
        emoji = "✅"
        statusText = "Build Succeeded"
    } else if (status == "UNSTABLE") {
        color = "FFFF00"      // amarillo
        emoji = "⚠️"
        statusText = "Build Unstable"
    }

    // Enviar notificación a Teams
    office365ConnectorSend(
        webhookUrl: config.webhookUrl,
        status: status,
        message: "${emoji} ${statusText}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        adaptiveCards: true,
        color: color,
        factDefinitions: [
            [name: "Build triggered by", template: env.BUILD_USER],
            [name: "Commit Author", template: env.COMMIT_AUTHOR],
            [name: "Commit Message", template: env.COMMIT_MESSAGE],
            [name: "Commit Hash", template: env.COMMIT_HASH],
            [name: "Build Number", template: env.BUILD_NUMBER],
            [name: "Remarks", template: "Started by user ${env.BUILD_USER}"],
            [name: "Duration", template: durationText]
        ]
    )

    echo "📢 Notificación enviada: ${statusText} (${durationText})"
}
