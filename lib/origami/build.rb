module Origami
  class Build
    attr_reader :variant

    def initialize(variant)
      @variant = variant
      @assembled = false
    end

    # Cleans and assembles a build for the variant.
    #
    # Returns the file path String to the build package.
    def compile
      Origami.say "Cleaning and assembling a new #{@variant} build"
      `./gradlew clean assemble#{@variant}`
      @assembled = true
      Origami.say "Package built to #{path}"
      path
    end

    protected

    def assembled?
      @assembled
    end

    def path
      raise 'Build has not been compiled yet!' unless assembled?

      Origami.app_dir + "build/outputs/apk/app-#{variant.underscore.dasherize}.apk"
    end
  end
end
