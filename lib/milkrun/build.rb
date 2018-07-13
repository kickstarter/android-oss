module Milkrun
  class Build
    attr_reader :audience, :build_type

    def initialize(audience:, build_type:)
      @audience, @build_type = audience, build_type
      @assembled = false
    end

    # Cleans and assembles a build.
    #
    # Returns the file path String to the build package.
    def compile
      Milkrun.say "Cleaning and assembling a new #{task} build"
      `./gradlew clean assemble#{task}`
      @assembled = true
      Milkrun.say "Package built to #{path}"
      path
    end

    protected

    def assembled?
      @assembled
    end

    def components
      [audience, 'pre21', build_type]
    end

    def path
      raise 'Build has not been compiled yet!' unless assembled?

      File.join(Milkrun.app_dir, "build/outputs/apk/#{components[0] + components[1]}/#{components[2]}/app-#{components.join('-')}.apk")
    end

    def task
      components.join
    end
  end
end
