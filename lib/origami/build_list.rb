module Origami
  class BuildList
    # Returns Array list of builds from S3.
    def fetch
      return @builds if defined? @builds

      object = Origami.s3_client.get_object(bucket: Origami.bucket, key: 'builds.yaml')
      @builds = YAML::load(object.body.read)
    end
  end
end
