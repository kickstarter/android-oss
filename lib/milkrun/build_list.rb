module Milkrun
  class BuildList
    # Returns Array list of builds from S3.
    def fetch
      return @builds if defined? @builds

      object = Milkrun.s3_client.get_object(bucket: Milkrun.bucket, key: 'builds.yaml')
      @builds = YAML::load(object.body.read)
    end
  end
end
