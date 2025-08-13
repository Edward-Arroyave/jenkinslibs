// File: sendNotificationTeams.groovy

def call(Map config) {

    // Duración de la build en milisegundos
    def durationMillis = currentBuild.duration ?: 0
    def durationText = ""

    if (durationMillis > 0) {
        // Convertir manualmente sin usar mod()
        def totalSeconds = (durationMillis / 1000).toInteger()
        def hours = totalSeconds / 3600
        def minutes = (totalSeconds - (hours * 3600)) / 60
        def seconds = totalSeconds - (hours * 3600) - (minutes * 60)

        if (hours > 0) { durationText += "${hours}h " }
        if (minutes > 0) { durationText += "${minutes}m " }
        durationText += "${seconds}s"
    } else {
        // Si la duración es cero o menor, mostrar valor en milisegundos
        durationText = "${durationMillis} ms"
    }

    // Determinar color y emoji según resultado
    def status = currentBuild.currentResult ?: "FAILURE"
    def color = "FF0000"    // rojo por defecto
    def emoji = "❌"
    def statusText = "Build Failed"

    if (status == "SUCCESS") {
        color = "00FF00"
        emoji = "✅"
        statusText = "Build Succeeded"
    } else if (status == "UNSTABLE") {
        color = "FFFF00"
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
            [name: "Build triggered by", template: "${env.BUILD_USER ?: 'Unknown'}"],
            [name: "Commit Author", template: "${env.COMMIT_AUTHOR ?: 'Unknown'}"],
            [name: "Commit Message", template: "${env.COMMIT_MESSAGE ?: 'No message'}"],
            [name: "Commit Hash", template: "${env.COMMIT_HASH ?: 'N/A'}"],
            [name: "Build Number", template: "${env.BUILD_NUMBER ?: 'N/A'}"],
            [name: "Remarks", template: "Started by user ${env.BUILD_USER ?: 'Unknown'}"],
            [name: "Duration", template: durationText],
        ]
    )

    echo "📢 Notificación enviada: ${statusText} (${durationText})"
}
