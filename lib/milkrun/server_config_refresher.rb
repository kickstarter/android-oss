require 'net/http'
require 'uri'
require 'json'
require_relative 'secrets'

module Milkrun
  class ServerConfigRefresher
    attr_reader :local

    def initialize(local: false)
      @local = local
    end

    def refresh
      response = Net::HTTP.get_response(url)

      if !response.is_a?(Net::HTTPSuccess)
        Milkrun.say "Response from API: #{response.body}"
        raise "Couldn't download config from server!"
      end

      json = JSON.parse(response.body)
      config = JSON.pretty_generate(json)
      File.write(path, config)

      Milkrun.say "Updated config from server"
    end

    def path
      File.expand_path(File.join(Milkrun.assets_dir, "json/server-config.json"))
    end

    private

    def url
      local ?
        URI.parse("http://api.ksr.dev/v1/app/android/config?client_id=#{Secrets::Api::Client::LOCAL}&all_locales=true") :
        URI.parse("https://#{Secrets::Api::Endpoint::PRODUCTION}/v1/app/android/config?client_id=#{Secrets::Api::Client::PRODUCTION}&all_locales=true")
    end
  end
end
