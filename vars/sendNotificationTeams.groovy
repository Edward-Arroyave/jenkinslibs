// File name: sendSuccessNotification.groovy


    // // Definir dinámicamente el color y el emoji según el resultado
    //             def status = currentBuild.currentResult
    //             def color = "FF0000" // rojo por defecto
    //             def emoji = "❌"
    //             def statusText = "Build Failed"

    //             if (status == "SUCCESS") {
    //                 color = "00FF00" // verde
    //                 emoji = "✅"
    //                 statusText = "Build Succeeded"
    //             } else if (status == "UNSTABLE") {
    //                 color = "FFFF00" // amarillo
    //                 emoji = "⚠️"
    //                 statusText = "Build Unstable"
    //             }

def call (Map config) {

  echo "📢 Enviando notificación de éxito a Microsoft Teams ${currentBuild.duration}"
    def durationMillis = currentBuild.duration
    def seconds = (durationMillis / 1000) % 60
    def minutes = (durationMillis / 1000 / 60) % 60
    def hours = durationMillis / 1000 / 60 / 60

    def durationText = ""
    if (hours > 0) {
    durationText += "${hours.toInteger()}h "
    }
    if (minutes > 0) {
    durationText += "${minutes.toInteger()}m "
    }
    durationText += "${seconds.toInteger()}s"
    
    office365ConnectorSend(
        status: currentBuild.currentResult,
        message: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        adaptiveCards: true,
        color:"FF0000",
        factDefinitions: [
            [name: "Build triggered by", template: env.BUILD_USER],
            [name: "Commit Author", template:env.COMMIT_AUTHOR],
            [name: "Commit Message", template: env.COMMIT_MESSAGE],
            [name: "Commit Hash", template: env.COMMIT_HASH],
            [name: "Build", template: env.BUILD_NUMBER],
            [name: "Remarks", template: env.currentBuild],
            [name: "DeployTime", template: durationText],
        ]


    )
}
