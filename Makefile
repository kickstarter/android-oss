bootstrap: dependencies secrets
	./script/bootstrap

bootstrap-circle: dependencies secrets

dependencies: submodules

submodules:
	git submodule sync --recursive
	git submodule update --init --recursive

secrets:
	# Copy java secrets over, and fallback to the example secrets if they don't exist.
	cp vendor/native-secrets/android/Secrets.java app/src/main/java/com/kickstarter/libs/utils/Secrets.java 2>/dev/null \
	  || cp app/src/main/java/com/kickstarter/libs/utils/Secrets.java.example app/src/main/java/com/kickstarter/libs/utils/Secrets.java

	cp vendor/native-secrets/ruby/secrets.rb lib/milkrun/secrets.rb || true
	cp vendor/native-secrets/fonts/ss-kickstarter.otf app/src/main/assets/fonts/ss-kickstarter.otf || true

.PHONY: bootstrap bootstrap-circle dependencies secrets
