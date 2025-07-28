def call(Map config) {
    // Validaciones básicas
    if (!config.branch || !config.repoPath || !config.repoUrl) {
        error("❌ cloneRepo: 'branch', 'repoPath', and 'repoUrl' parameters are required.")
    }


    stage('Clone Repository') {
        steps {
            echo "📦 Cloning repository:"
            echo "   🟢 Branch: ${config.branch}"
            echo "   📁 Path: ${config.repoPath}"
            echo "   🔗 URL: ${config.repoUrl}"

            dir(config.repoPath) {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${config.branch}"]],
                    userRemoteConfigs: [[
                        url: config.repoUrl,
                        credentialsId: 'GITHUB'
                    ]]
                ])
            }

            echo "✅ Repository successfully cloned at: ${config.repoPath}"
        }
    }
}
