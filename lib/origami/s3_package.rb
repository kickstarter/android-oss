module Origami
  class S3Package
    attr_reader :variant, :version, :file_path

    def initialize(variant:, version:, file_path:)
      @variant, @version, @file_path = variant, version, file_path
    end

    # Uploads a build to S3.
    def upload
      Origami.say "Uploading package to #{Origami.bucket} bucket"

      File.open(file_path, 'rb') do |file|
        Origami.s3_client.put_object(
          body: file,
          bucket: Origami.bucket,
          content_type: "application/vnd.android.package-archive",
          key: package_key
        )
      end

      Origami.say "Package uploaded to s3://#{Origami.bucket}/#{package_key}"
    end

    protected

    def package_key
      "#{version}/#{version}.apk"
    end
  end
end
