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
	-git clone https://github.com/kickstarter/native-secrets vendor/native-secrets

	# TODO: temp bootstrap-circle workaround. Consolidate the cp services commands.
	cp config/google-services.example.json app/src/externalPre21Release/google-services.json

	# Copy java secrets over. Fallback to example secrets if they don't exist.
	cp vendor/native-secrets/android/Secrets.java app/src/main/java/com/kickstarter/libs/utils/Secrets.java \
		|| cp app/src/main/java/com/kickstarter/libs/utils/Secrets.java.example app/src/main/java/com/kickstarter/libs/utils/Secrets.java

	cp vendor/native-secrets/ruby/secrets.rb lib/milkrun/secrets.rb || true
	cp vendor/native-secrets/fonts/ss-kickstarter.otf app/src/main/assets/fonts/ss-kickstarter.otf || true

	# Copy kola configs over. Fallback to examples if they don't exist.
	mkdir -p app/src/debug/res/values/
	mkdir -p app/src/main/res/values/
	cp vendor/native-secrets/android/koala_endpoint_debug.xml app/src/debug/res/values/koala_endpoint.xml \
		|| cp config/koala_endpoint.xml.example app/src/debug/res/values/koala_endpoint.xml
	cp vendor/native-secrets/android/koala_endpoint.xml app/src/main/res/values/koala_endpoint.xml \
		|| cp config/koala_endpoint.xml.example app/src/main/res/values/koala_endpoint.xml

	# Copy web client over.
	cp -rf vendor/native-secrets/android/WebViewJavascriptInterface.java app/src/main/java/com/kickstarter/libs/WebViewJavascriptInterface.java \
		|| cp -rf config/WebViewJavascriptInterface.java app/src/main/java/com/kickstarter/libs/WebViewJavascriptInterface.java
	cp -rf vendor/native-secrets/android/KSWebViewClient.java app/src/main/java/com/kickstarter/services/KSWebViewClient.java \
		|| cp -rf config/KSWebViewClient.java app/src/main/java/com/kickstarter/services/KSWebViewClient.java

.PHONY: bootstrap bootstrap-circle dependencies secrets
