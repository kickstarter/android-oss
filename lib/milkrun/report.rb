module Milkrun
  class Report
    attr_reader :result

    def report_path
      raise NotImplementedError
    end

    def run
      Milkrun.say "Running #{command}"
      @result = system(File.join(Milkrun.project_dir, command))
    end

    protected

    def command
      raise NotImplementedError
    end
  end
end
