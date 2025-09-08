def call(String url, String siteName = "Default Site") {
    stage("🔎 Validar estado http") {
        script {
            // Escribir script bash temporal
            writeFile file: 'checkApi.sh', text: '''
                #!/bin/bash
                url="$1"
                status=$(curl -s -o /dev/null -w "%{http_code}" "$url")
                if [ -z "$status" ]; then
                    echo "STATUS:0"
                else
                    echo "STATUS:$status"
                fi
            '''.stripIndent()

            // Dar permisos de ejecución
            sh "chmod +x checkApi.sh"

            // Ejecutar y capturar salida
            def output = sh(
                script: "./checkApi.sh '${url}'",
                returnStdout: true
            ).trim()

            echo "📡 Output Bash: ${output}"

            def statusLine = output.readLines().find { it.startsWith("STATUS:") }
            def statusCode = (statusLine ? statusLine.replace("STATUS:", "").trim() : "0").toInteger()

            echo "📡 Respuesta de ${siteName}: código ${statusCode}"

            if (statusCode >= 500) {
                error("❌ Error de servidor en ${siteName} (${statusCode}).")
            } else if (statusCode == 0) {
                error("❌ No se pudo conectar a ${siteName}")
            } else if (statusCode >= 400) {
                echo "⚠️ Error de cliente (${statusCode}) en ${siteName}. Ignorado."
            } else {
                echo "✅ API ${siteName} respondió correctamente (${statusCode})"
            }
        }
    }
}
