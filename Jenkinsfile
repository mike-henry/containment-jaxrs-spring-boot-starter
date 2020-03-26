pipeline {
    agent any
    tools {
        maven 'maven_3'
    }
    environment {
        PROJECT_NAME = "containment-jaxrs-spring-boot-starter"
        GRADLE_PROPERTIES = './gradle.properties'
    }

    stages {
        stage('Initialise') {
            steps {
              configFileProvider([configFile(fileId: 'ce4190e5-99fe-411b-82ce-0fb8d9b123a1', variable: 'GRADLE_PROPERTY_FILE')]){
                echo '.Initialising..'

                sh '''
                 echo "PATH = ${PATH}"
                 echo "M2_HOME = ${M2_HOME}"
                 echo "MAVEN_HOME = ${MAVEN_HOME}"
                 echo "JAVA_HOME = ${JAVA_HOME}"

                 cp  ${GRADLE_PROPERTY_FILE} ${GRADLE_PROPERTIES}
                 '''
              }
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'

                sh './gradlew clean build'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'

                 sh './gradlew test'
            }
        }
        stage('Package') {
            steps {
                echo 'Packaging..'

                sh './gradlew assemble'
            }
        }


        stage('Deploy') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying.. library'

                sh './gradlew publish'
            }
        }
    }
}