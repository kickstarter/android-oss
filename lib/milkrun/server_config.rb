require 'net/http'
require 'uri'
require 'json'

module Milkrun
  class ServerConfig
    def initialize
    end

    def refresh
      response = Net::HTTP.get_response(url)

      if !response.is_a?(Net::HTTPSuccess)
        Milkrun.say "Response from API: #{response.body}"
        raise "Couldn't download config from server!"
      end

      json = JSON.parse(response.body)
      config = JSON.pretty_generate(json)

      File.open(self.class.config_path, "w") do |file|
        file.write(config)
      end

      Milkrun.say "Updated config from server"
    end

    def self.config_path
      File.expand_path(File.join(File.dirname(__FILE__), "../../app/src/main/assets/json/server-config.json"))
    end

    private

    def url
      URI.parse("https://***REMOVED***/v1/app/android/config?client_id=***REMOVED***&all_locales=true")
    end
    
    # For testing
    def local_url
      URI.parse("http://api.ksr.dev/v1/app/android/config?client_id=***REMOVED***&all_locales=true")
    end
  end
end
