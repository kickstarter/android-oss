BRANCH ?= master
COMMIT ?= $(CIRCLE_SHA1)

bootstrap: dependencies secrets
	./script/bootstrap

bootstrap-circle: dependencies secrets
	./script/bootstrap_configs

dependencies: submodules secrets

submodules:
	git submodule sync --recursive
	git submodule update --init --recursive || true
	git submodule foreach git checkout $(sha1)

secrets:
	# Copy java secrets over. Fallback to example secrets if they don't exist.
	-@rm -rf vendor/native-secrets
	-@git clone git@github.com:kickstarter/native-secrets.git vendor/native-secrets 2>/dev/null || echo '(Skipping secrets.)'

	cp vendor/native-secrets/android/Secrets.java app/src/main/java/com/kickstarter/libs/utils/Secrets.java \
		|| cp app/src/main/java/com/kickstarter/libs/utils/Secrets.java.example app/src/main/java/com/kickstarter/libs/utils/Secrets.java

	cp vendor/native-secrets/ruby/secrets.rb lib/milkrun/secrets.rb || true
	cp vendor/native-secrets/fonts/ss-kickstarter.otf app/src/main/assets/fonts/ss-kickstarter.otf || true

  # Copy crashlytics over. Fallback to examples if they don't exist
	cp vendor/native-secrets/android/fabric.properties app/fabric.properties || cp config/fabric.properties.example app/fabric.properties

  # Copy slack_webhook over.
	cp vendor/native-secrets/android/slack.properties app/slack.properties || true

	# Copy web client over.
	cp -rf vendor/native-secrets/android/WebViewJavascriptInterface.java app/src/main/java/com/kickstarter/libs/WebViewJavascriptInterface.java \
		|| cp -rf config/WebViewJavascriptInterface.java app/src/main/java/com/kickstarter/libs/WebViewJavascriptInterface.java
	cp -rf vendor/native-secrets/android/KSWebViewClient.java app/src/main/java/com/kickstarter/services/KSWebViewClient.java \
		|| cp -rf config/KSWebViewClient.java app/src/main/java/com/kickstarter/services/KSWebViewClient.java
	mkdir -p app/src/main/assets/www/
	cp vendor/native-secrets/android/WebViewJavascript.html app/src/main/assets/www/WebViewJavascript.html || true

.PHONY: bootstrap bootstrap-circle dependencies secrets

sync_oss_to_private:
	@echo "Syncing oss to private..."
	@git checkout oss $(BRANCH)
	@git pull oss $(BRANCH)
	@git push private $(BRANCH)

	@echo "private and oss remotes are now synced!"

sync_private_to_oss:
	@echo "Syncing private to oss..."
	@git checkout private $(BRANCH)
	@git pull private $(BRANCH)
	@git push oss $(BRANCH)

	@echo "private and oss remotes are now synced!"


alpha:
	@echo "Adding remotes..."
	@git remote add oss https://github.com/kickstarter/android-oss
	@git remote add private https://github.com/kickstarter/android-private

	@echo "Deploying private/alpha-$(COMMIT)..."

	@git branch -f alpha
	@git push -f private alpha
	@git branch -d alpha

	@echo "Deploy has been kicked off to CircleCI!"
