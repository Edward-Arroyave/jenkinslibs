// File name: sendSuccessNotification.groovy

def call (Map config) {
    def messageText = "✅ Successful deployment of *${config.productName}* at ${new Date().format("HH:mm:ss")}"

    office365ConnectorSend(
        webhookUrl: config.webhookUrl,
        status: 'Success',
        message: messageText,
        adaptiveCards: true,
        factDefinitions: [
            [name: "Commit Author", template: config.deployUser],
            [name: "Commit Message", template: config.commitMessage],
            [name: "Commit Hash", template: config.commitHash],
            [name: "Build", template: config.buildNumber],
            [name: "Build URL", template: config.buildUrl]
        ]
    )
}
