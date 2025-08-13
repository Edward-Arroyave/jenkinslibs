def call(Map config) {

   // Calcular duración en milisegundos
def startMillis = currentBuild.getStartTimeInMillis()
def endMillis = currentBuild.getTimeInMillis()
def durationMillis = endMillis - startMillis

// Convertir a segundos como double
def totalSeconds = durationMillis / 1000.0

// Calcular horas, minutos y segundos sin usar %
def hours = Math.floor(totalSeconds / 3600).toInteger()
def minutes = Math.floor((totalSeconds - (hours * 3600)) / 60).toInteger()
def seconds = totalSeconds - (hours * 3600) - (minutes * 60)

// Formatear como H.M.S con un decimal en segundos
def durationText = ""
if (hours > 0) { durationText += "${hours}h " }
if (minutes > 0) { durationText += "${minutes}m " }
durationText += String.format("%.1f", seconds) + "s"

echo "Duración calculada: ${durationText}"



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
    office365ConnectorSend(
        status: status,
        message: "${emoji} ${statusText}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        adaptiveCards: true,
        color: color,
        factDefinitions: [
            [name: "Build triggered by", template: "${env.BUILD_USER}"],
            [name: "Commit Author", template: "${env.COMMIT_AUTHOR}"],
            [name: "Commit Message", template: "${env.COMMIT_MESSAGE}"],
            [name: "Commit Hash", template: "${env.COMMIT_HASH}"],
            [name: "Build Number", template: "${env.BUILD_NUMBER}"],
            [name: "Remarks", template: "Started by user ${env.BUILD_USER}"],
            [name: "Duration", template: durationText],
        ]
    )

    echo "📢 Notificación enviada: ${statusText} (${durationText})"
}
