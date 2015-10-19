module Milkrun
  class Git
    def check
      raise "Must be on master" if `git rev-parse --abbrev-ref HEAD`.chomp != "master"
      raise "Working directory must be clean" if !system("git diff-index --quiet HEAD")
    end
  end
end
