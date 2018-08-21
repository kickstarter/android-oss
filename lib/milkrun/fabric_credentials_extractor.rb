module Milkrun
  class FabricCredentialsExtractor

    def creds
       lines = File.readlines("../app/fabric.properties")
            cred = lines.map { |line| line.strip.split('=') }.to_h
            key = cred["apiKey"]
            puts key
    end
  end
end
