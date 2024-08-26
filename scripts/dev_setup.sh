#!/bin/bash

install_macos_dependencies() {
  brew install openjdk@11
  export JAVA_HOME=/usr/local/opt/openjdk@11
  brew install maven
  brew install node
  npm install -g grunt-cli
  brew install tesseract
  brew install ffmpeg
  brew install mediainfo
}

install_ubuntu_debian_dependencies() {
  sudo apt update
  sudo apt install -y openjdk-11-jdk maven nodejs npm tesseract-ocr ffmpeg mediainfo
  sudo npm install -g grunt-cli
}

install_fedora_dependencies() {
  sudo dnf install -y java-11-openjdk-devel maven nodejs npm tesseract ffmpeg mediainfo
  sudo npm install -g grunt-cli
}

echo "📥 Installing dependencies"
if [[ "$OSTYPE" == "darwin"* ]]; then
  install_macos_dependencies
elif [[ -f /etc/debian_version ]]; then
  install_ubuntu_debian_dependencies
elif [[ -f /etc/fedora-release ]]; then
  install_fedora_dependencies
else
  echo "❔ Unsupported OS. Feel free to contribute!"
  exit 1
fi

echo "✅ Dependencies installed."

mvn clean -DskipTests install

echo "You can start the server with 'mvn jetty:run' and then access it at http://localhost:8080/docs-web/src/"
