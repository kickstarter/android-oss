module Milkrun
  class VersionCode
    attr_reader :audience

    def initialize(audience:)
      @audience = audience
    end

    # Bump the version code incrementing in one the previous version_code.
    #
    # Warning: The greatest value Google Play allows for versionCode is 2100000000.
    # see https://developer.android.com/studio/publish/versioning
    # Before 2021 the code version format was y%m%d%H%M -> as example 2012311005 = 31 December 2020 10:05
    # it stopped being a valid format on 2021
    #
    # Returns the new version code String.
    def bump
      version_code = File.read(path).chomp.to_i+1
      Milkrun.say "Bumping #{audience} version code to #{version_code} ✊"
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
      File.join(Milkrun.app_dir, [audience, 'version_code.txt'].join('_'))
    end
  end
end
