module Milkrun
  class VersionCode
    # Bump the version code using the current timestamp.
    #
    # Returns the new version code String.
    def bump
      version_code = Time.now.strftime("%y%m%d%H%M").to_i
      Milkrun.say "Bumping version code to #{version_code} ✊"
      File.open(path, "w") do |f|
        f.puts(version_code)
      end
      version_code
    end

    # Returns the current version code String.
    def read
      File.read(path).chomp
    end

    protected

    def path
      Milkrun.app_dir + 'version_code.txt'
    end
  end
end
