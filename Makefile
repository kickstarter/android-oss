bootstrap: dependencies secrets
	./script/bootstrap

bootstrap-circle: dependencies secrets

dependencies: submodules

submodules:
	git submodule sync --recursive
	git submodule update --init --recursive || true
	git submodule foreach git checkout $(sha1)

secrets:
	# Copy java secrets over, and fallback to the example secrets if they don't exist.
	cp vendor/native-secrets/android/Secrets.java app/src/main/java/com/kickstarter/libs/utils/Secrets.java \
		|| cp app/src/main/java/com/kickstarter/libs/utils/Secrets.java.example app/src/main/java/com/kickstarter/libs/utils/Secrets.java

	cp vendor/native-secrets/ruby/secrets.rb lib/milkrun/secrets.rb || true
	cp vendor/native-secrets/fonts/ss-kickstarter.otf app/src/main/assets/fonts/ss-kickstarter.otf || true

	# Copy koala endpoint configs over, and fallback to examples if they don't exist.
	mkdir -p app/src/debug/res/values/
	mkdir -p app/src/main/res/values/
	cp vendor/native-secrets/android/koala_endpoint_debug.xml app/src/debug/res/values/koala_endpoint.xml \
		|| cp config/koala_endpoint.xml.example app/src/debug/res/values/koala_endpoint.xml
	cp vendor/native-secrets/android/koala_endpoint.xml app/src/main/res/values/koala_endpoint.xml \
		|| cp config/koala_endpoint.xml.example app/src/main/res/values/koala_endpoint.xml

.PHONY: bootstrap bootstrap-circle dependencies secrets
