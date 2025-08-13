// File: sendNotificationTeams.groovy

def call(Map config) {

   

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
            [name: "Duration", template: "${currentBuild.getTimeInMillis()}"],
            [name: "ChangesGithub", template: "${currentBuild.getChangeSets()}"],
        ]
    )

}
