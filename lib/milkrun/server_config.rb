require 'net/http'
require 'uri'
require 'json'

module Milkrun
  class ServerConfig
    def initialize
    end

    def update
      response = Net::HTTP.get_response(url)

      if !response.is_a?(Net::HTTPSuccess)
        Milkrun.say "Response from API: #{response.body}"
        raise "Couldn't download config from server!"
      end

      json = JSON.parse(response.body)
      config = JSON.pretty_generate(json)

      File.open(config_path, "w") do |file|
        file.write(config)
      end

      Milkrun.say "Updated config from server"
    end

    private

    def url
      URI.parse("https://***REMOVED***/v1/app/android/config?client_id=***REMOVED***&all_locales=true")
    end

    def config_path
      File.expand_path(File.join(File.dirname(__FILE__), "../../app/src/main/assets/json/server-config.json"))
    end
  end
end
