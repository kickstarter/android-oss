module Milkrun
  class VersionName
    # Prompt to enter a new version name, e.g. 0.1.0. The User can choose not to
    # modify the version name.
    #
    # Returns the version name String.
    def prompt
      input = Milkrun.prompt("Enter a version name, or hit return to use existing value (#{read})", label: "Version Name")
      value = input.empty? ? read : input
      raise "Must use semver format, e.g.: 0.1.0" unless value.match(/\d+\.\d+\.\d+/)
      File.open(path, "w") do |f|
        f.puts(value)
      end
      value
    end

    # Returns the current version name String.
    def read
      File.read(path).chomp
    end

    protected

    def path
      Milkrun.app_dir + 'version_name.txt'
    end
  end
end
