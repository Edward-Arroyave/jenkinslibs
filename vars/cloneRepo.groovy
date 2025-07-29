def call(Map config) {

    if (!config.branch || !config.repoPath || !config.repoUrl) {
        error("❌ cloneRepo: 'branch', 'repoPath', and 'repoUrl' parameters are required.")
    }

    echo "📦 Cloning repository (shallow clone):"
    echo "   🟢 Branch: ${config.branch}"
    echo "   📁 Path: ${config.repoPath}"
    echo "   🔗 URL: ${config.repoUrl}"

    dir(config.repoPath) {
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${config.branch}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: 'CloneOption', depth: 1, noTags: false, reference: '', shallow: true]
            ],
            userRemoteConfigs: [[
                url: config.repoUrl,
                credentialsId: 'GITHUB'
            ]]
        ])
    }

    echo "✅ Repository successfully shallow-cloned at: ${config.repoPath}"
}
