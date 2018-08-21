module Milkrun
  class FabricCredentialsExtractor

    def properties
      lines = File.readlines("../app/fabric.properties")
      properties = lines.map { |line| line.strip.split('=') }.to_h
    end

    def api_key
      properties["apiKey"]
    end

    def api_secret
      properties["apiSecret"]
    end
  end
end
