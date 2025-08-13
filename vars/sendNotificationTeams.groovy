// File name: sendSuccessNotification.groovy

def call (Map config) {
    def messageText = "✅ Successful deployment of *${config.productName}* at ${new Date().format("HH:mm:ss")}"

    office365ConnectorSend(
        webhookUrl: config.webhookUrl,
        status: 'Success',
        message: messageText,
        adaptiveCards: true,
        factDefinitions: [
            [name: "Build triggered by", template: env.BUILD_USER],
            [name: "Commit Author", env.COMMIT_AUTHOR],
            [name: "Commit Message", template: env.COMMIT_MESSAGE],
            [name: "Commit Hash", template: env.COMMIT_HASH],
            [name: "Build", template: env.BUILD_NUMBER],
        ]
        potentialAction: [
        [
            "@type": "OpenUri",
            name: "View Build",
            targets: [
                [os: "default", uri: env.BUILD_URL]
            ]
        ]
    ]
    )
}
