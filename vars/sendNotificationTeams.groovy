def call(Map config) {

    // Obtener duración real del build en milisegundos
    def durationMillis = currentBuild.duration ?: (currentBuild.getTimeInMillis() - currentBuild.getStartTimeInMillis())

    // Convertir a H.M.S
    def totalSeconds = durationMillis / 1000.0
    def hours = Math.floor(totalSeconds / 3600).toInteger()
    def minutes = Math.floor((totalSeconds - (hours * 3600)) / 60).toInteger()
    def seconds = totalSeconds - (hours * 3600) - (minutes * 60)

    def durationText = ""
    if (hours > 0) { durationText += "${hours}h " }
    if (minutes > 0) { durationText += "${minutes}m " }
    durationText += String.format("%.1f", seconds) + "s"

    // Determinar color y emoji según resultado
    def status = currentBuild.currentResult ?: "FAILURE"
    def color = "FF0000"
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
    wrap([$class: 'BuildUser']) {
        office365ConnectorSend(
            status: status,
            message: "${emoji} ${statusText}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            adaptiveCards: true,
            color: color,
            factDefinitions: [
                [name: "📌 Estado Final", template: "**${statusText} ${emoji}**"],
                [name: "👤 Usuario ejecutor", template: "_${env.BUILD_USER}_"],
                [name: "🌍 Entorno", template: "**${config.ENVIRONMENT ?: 'No definido'}**"],
                [name: "👨‍💻 Autor del Commit", template: "${env.COMMIT_AUTHOR ?: '-'}"],
                [name: "📝 Commit", template: "${env.COMMIT_MESSAGE ?: '-'}"],
                [name: "🔗 Hash del Commit", template: "`${env.COMMIT_HASH ?: '-'} `"],
                [name: "⏱️ Duración", template: "` ${durationText} `"]
            ]
        )
    }

    echo "📢 Notificación enviada: ${statusText} (${durationText})"
}
