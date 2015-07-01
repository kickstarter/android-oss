module Origami
  class Changelog
    attr_reader :variant, :version

    def initialize(variant:, version:)
      @variant, @version = variant, version
    end

    # Capture and publish the changelog for a package to a list in S3.
    def publish
      Origami.say "Publishing changelog for #{variant} package with version #{version}"

      build = {
        'build' => version,
        'changelog' => changelog,
        'variant' => variant
      }
      body = (current_builds.select{|b| b[:build] != version} + [build]).to_yaml

      object = Origami.s3_client.put_object(body: body, bucket: Origami.bucket, key: 'builds.yaml')

      Origami.say "Changelog published"
    end

    protected

    def current_builds
      @current_builds ||= BuildList.new.fetch
    end

    def changelog
      return @changelog if defined? @changelog

      previous_changelog = current_builds.last['changelog']
      file_name = '.CHANGELOG.tmp'
      File.open(file_name, 'w') do |f|
        f.write("\n\n# Previous release notes, anything commented out will not appear:\n\n")
        f.write(previous_changelog
          .split("\n")
          .map {|line| "# #{line}"}
          .join("\n")
        )
      end

      # Note: Only tested with vim
      system(ENV['EDITOR'], file_name)
      @changelog = strip_commented_lines(File.read(file_name)).strip
      raise "Must provide release notes" if changelog.length == 0
      File.delete(file_name)

      @changelog
    end

    def strip_commented_lines(str)
      str.split("\n").select {|line| line.strip[0] != '#'}.join("\n")
    end
  end
end
