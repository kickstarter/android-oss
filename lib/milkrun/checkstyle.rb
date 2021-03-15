require_relative 'report'

module Milkrun
  class Checkstyle < Report
    def report_path
      File.join(Milkrun.app_dir, "build", "reports", "checkstyle", "checkstyle.html", "ktlint.html")
    end

    private

    def command
      "gradlew checkstyle"
    end
  end
end
