pipeline {
    agent any

    options {
      timeout(time: 60, unit: 'MINUTES')
    }

    environment {
      MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
      stage('Environment') {
        steps {
          script {
            runCommand('java -version')
            runCommand('mvn -version')
          }
        }
      }

      stage('Build & Test') {
        steps {
          script {
            runCommand('mvn -B clean verify')
          }
        }
        post {
          always {
            junit allowEmptyResults: false, testResults: '**/target/surefire-reports/TEST-*.xml'
          }
        }
      }

      stage('PMD') {
        steps {
          script {
            runCommand('mvn -B pmd:pmd')
          }
        }
      }

      stage('JaCoCo') {
        steps {
          script {
            runCommand('mvn -B jacoco:report')
          }
        }
      }

      stage('Javadoc') {
        steps {
          script {
            runCommand('mvn -B javadoc:javadoc -Dmaven.javadoc.failOnError=false -Ddoclint=none')
          }
        }
      }

      stage('Site Documentation') {
        steps {
          script {
            runCommand('mvn -B site')
          }
        }
      }
    }

    post {
      always {
        archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/*.jar,**/target/*.war,**/target/site/**'
      }
    }
  }

  void runCommand(String command) {
    if (isUnix()) {
      sh command
    } else {
      bat command
    }
  }