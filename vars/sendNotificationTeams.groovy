// File: sendNotificationTeams.groovy

def call(Map config) {

        def startMillis = currentBuild.getStartTimeInMillis()
        def endMillis = currentBuild.getTimeInMillis()
        def durationMillis = endMillis - startMillis

        // Convertir a horas, minutos y segundos
        def totalSeconds = (durationMillis / 1000).toInteger()
        def seconds = totalSeconds - ((totalSeconds / 60).toInteger() * 60)
        def totalMinutes = (totalSeconds / 60).toInteger()
        def minutes = totalMinutes - ((totalMinutes / 60).toInteger() * 60)
        def hours = (totalMinutes / 60).toInteger()

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
        status: status,
        message: "${emoji} ${statusText}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        adaptiveCards: true,
        color: color,
        factDefinitions: [
            [name: "Build triggered by", template: "${env.BUILD_USER}"],
            [name: "Commit Author", template: "${env.COMMIT_AUTHOR }"],
            [name: "Commit Message", template: "${env.COMMIT_MESSAGE}"],
            [name: "Commit Hash", template: "${env.COMMIT_HASH}"],
            [name: "Build Number", template: "${env.BUILD_NUMBER}"],
            [name: "Remarks", template: "Started by user ${env.BUILD_USER}"],
            [name: "Duration", template: durationText],
         
        ]
    )

}
