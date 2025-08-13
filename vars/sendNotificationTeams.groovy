// File name: sendSuccessNotification.groovy

def call (Map config) {
    
    office365ConnectorSend(
        status: 'Failure',
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
