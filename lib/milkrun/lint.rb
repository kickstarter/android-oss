require_relative 'report'

module Milkrun
  class Lint < Report
    attr_reader :variant

    def initialize(variant: "externalRelease")
      @variant = variant
    end

    def report_path
      File.join(Milkrun.app_dir, "build", "outputs", "lint-results-#{variant.camelize(:lower)}.html")
    end

    private

    def command
      "gradlew #{task}"
    end

    def task
      "lint#{variant.camelize}"
    end
  end
end
