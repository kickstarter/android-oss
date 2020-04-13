module Milkrun
  class FirebaseCredentialsExtractor

    def properties
      lines = File.readlines("../app/firebase.properties")
      properties = lines.map { |line| line.strip.split('=') }.to_h
    end

    def external_debug
      properties["external_debug"]
    end

    def external_release
      properties["external_release"]
    end

    def internal_debug
      properties["internal_debug"]
    end

    def internal_release
      properties["internal_release"]
    end
  end
end
