module Milkrun
  class VersionCode
    # Bump the version code using the current timestamp.
    #
    # Returns the new version code String.
    def bump
      version = Time.now.strftime("%y%m%d%H%M").to_i
      Milkrun.say "Bumping version to #{version} ✊"
      File.open(path, "w") do |f|
        f.puts(version)
      end
      version
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
