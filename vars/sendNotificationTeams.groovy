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
            [name: "Remarks", template: env.currentBuild]
        ]


    )
}
