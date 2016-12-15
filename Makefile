bootstrap: dependencies secrets
	./script/bootstrap

bootstrap-circle: dependencies secrets

dependencies: submodules secrets

submodules:
	git submodule sync --recursive
	git submodule update --init --recursive || true
	git submodule foreach git checkout $(sha1)

secrets:
	-rm -rf vendor/native-secrets
	-git clone https://github.com/kickstarter/native-secrets1 vendor/native-secrets
	if [ ! -d vendor/native-secrets ]; \
	then \
		cp app/src/main/java/com/kickstarter/libs/utils/Secrets.java.example app/src/main/java/com/kickstarter/libs/utils/Secrets.java
		cp config/koala_endpoint.xml.example app/src/debug/res/values/koala_endpoint.xml
		cp config/koala_endpoint.xml.example app/src/main/res/values/koala_endpoint.xml
		cp -rf config/WebViewJavascriptInterface.java app/src/main/java/com/kickstarter/libs/WebViewJavascriptInterface.java
		cp -rf config/KSWebViewClient.java app/src/main/java/com/kickstarter/services/KSWebViewClient.java
	fi

.PHONY: bootstrap bootstrap-circle dependencies secrets
